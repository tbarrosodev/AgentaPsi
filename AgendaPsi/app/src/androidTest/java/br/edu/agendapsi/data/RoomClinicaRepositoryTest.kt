package br.edu.agendapsi.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.edu.agendapsi.data.local.*
import br.edu.agendapsi.data.model.*
import br.edu.agendapsi.data.repository.RoomClinicaRepository
import java.time.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomClinicaRepositoryTest {
    private lateinit var db: ClinicaDatabase
    private lateinit var repository: RoomClinicaRepository
    private val dia = LocalDate.of(2026, 10, 10)
    private val fuso = ZoneId.of("America/Sao_Paulo")
    private val clock = Clock.fixed(dia.minusDays(1).atTime(7, 0).atZone(fuso).toInstant(), fuso)
    private lateinit var contexto: Context

    @Before fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(contexto, ClinicaDatabase::class.java).build()
        repository = RoomClinicaRepository(db, clock)
        runBlocking { repository.inicializar() }
    }

    @After fun fechar() { db.close() }

    private fun id(resultado: ResultadoOperacao): Long {
        assertTrue("Esperava sucesso, recebeu $resultado", resultado is ResultadoOperacao.Sucesso)
        return (resultado as ResultadoOperacao.Sucesso).id
    }

    private fun rascunho(paciente: Long = 1, profissional: Long = 1, inicio: Int = 600, duracao: Int = 50) =
        AgendamentoRascunho(pacienteId = paciente, profissionalId = profissional,
            diaEpoch = dia.toEpochDay(), inicioMinutos = inicio, duracaoMinutos = duracao)

    @Test fun cadastroNormalizaDadosPermiteHomonimosEPreservaArquivamento() = runBlocking<Unit> {
        val paciente = id(repository.salvarPaciente(null, "  Nome Teste  ", "(11) 99999-1234"))
        assertEquals("Nome Teste", repository.obterPaciente(paciente)!!.nome)
        assertEquals("11999991234", repository.obterPaciente(paciente)!!.telefone)
        val homonimo = id(repository.salvarPaciente(null, "Nome Teste", ""))
        assertNotEquals(paciente, homonimo)
        assertNull(repository.obterPaciente(homonimo)!!.telefone)
        assertTrue(repository.salvarPaciente(null, "A", null) is ResultadoOperacao.Erro)
        assertTrue(repository.salvarPaciente(null, "Teste", "1199999999x") is ResultadoOperacao.Erro)
        assertTrue(repository.salvarPaciente(null, "Teste", "123") is ResultadoOperacao.Erro)
        id(repository.definirPacienteAtivo(paciente, false))
        id(repository.salvarPaciente(paciente, "Nome Editado", null))
        assertFalse(repository.obterPaciente(paciente)!!.ativo)
        assertTrue(repository.salvarAgendamento(rascunho(paciente = paciente)) is ResultadoOperacao.Erro)
        id(repository.definirPacienteAtivo(paciente, true))
        id(repository.salvarAgendamento(rascunho(paciente = paciente)))
    }

    @Test fun sobreposicaoConsideraPacienteOuProfissionalEPermiteHorariosAdjacentes() = runBlocking<Unit> {
        id(repository.salvarAgendamento(rascunho()))
        assertTrue(repository.salvarAgendamento(rascunho(paciente = 2, inicio = 620)) is ResultadoOperacao.Erro)
        assertTrue(repository.salvarAgendamento(rascunho(profissional = 2, inicio = 620)) is ResultadoOperacao.Erro)
        assertTrue(repository.salvarAgendamento(rascunho(inicio = 590, duracao = 60)) is ResultadoOperacao.Erro)
        id(repository.salvarAgendamento(rascunho(paciente = 2, profissional = 2, inicio = 620)))
        id(repository.salvarAgendamento(rascunho(inicio = 650)))
    }

    @Test fun remarcarIgnoraProprioIdVoltaParaAgendadoECancelarLiberaHorario() = runBlocking<Unit> {
        val agendamento = id(repository.salvarAgendamento(rascunho()))
        id(repository.alterarStatus(agendamento, StatusAgendamento.CONFIRMADO))
        id(repository.salvarAgendamento(rascunho().copy(id = agendamento)))
        assertEquals(StatusAgendamento.AGENDADO, repository.obterAgendamento(agendamento)!!.status)
        assertTrue(repository.alterarStatus(agendamento, StatusAgendamento.CONCLUIDO) is ResultadoOperacao.Erro)
        id(repository.alterarStatus(agendamento, StatusAgendamento.CANCELADO))
        id(repository.salvarAgendamento(rascunho()))
        assertTrue(repository.salvarAgendamento(rascunho().copy(id = agendamento)) is ResultadoOperacao.Erro)
        assertTrue(repository.alterarStatus(agendamento, StatusAgendamento.AGENDADO) is ResultadoOperacao.Erro)
    }

    @Test fun concluirDepoisDoFimPreservaHorarioETornaRegistroImutavel() = runBlocking<Unit> {
        val agendamento = id(repository.salvarAgendamento(rascunho()))
        val depois = RoomClinicaRepository(db, Clock.fixed(dia.atTime(10, 50).atZone(fuso).toInstant(), fuso))
        id(depois.alterarStatus(agendamento, StatusAgendamento.CONCLUIDO))
        val registro = depois.obterAgendamento(agendamento)!!
        assertEquals(600, registro.inicioMinutos)
        assertEquals(50, registro.duracaoMinutos)
        assertEquals(StatusAgendamento.CONCLUIDO, registro.status)
        assertTrue(depois.alterarStatus(agendamento, StatusAgendamento.CANCELADO) is ResultadoOperacao.Erro)
    }

    @Test fun arquivamentoBloqueiaPendenciasMesmoDeDiasPassados() = runBlocking<Unit> {
        val paciente = id(repository.salvarPaciente(null, "Paciente com pendência", null))
        val agendamento = id(repository.salvarAgendamento(rascunho(paciente = paciente)))
        val depois = RoomClinicaRepository(db, Clock.fixed(dia.plusDays(2).atStartOfDay(fuso).toInstant(), fuso))
        assertTrue(depois.definirPacienteAtivo(paciente, false) is ResultadoOperacao.Erro)
        id(depois.alterarStatus(agendamento, StatusAgendamento.FALTOU))
        id(depois.definirPacienteAtivo(paciente, false))
        assertFalse(depois.obterPaciente(paciente)!!.ativo)
    }

    @Test fun consultaObservadaAtualizaNomesEStatusEIncluiCancelados() = runBlocking<Unit> {
        val agendamento = id(repository.salvarAgendamento(rascunho()))
        val proximaAtualizacao = async {
            withTimeout(10_000) {
                repository.observarAgenda(dia.toEpochDay(), null).first { lista ->
                    lista.any { it.id == agendamento && it.pacienteNome == "Nome atualizado" && it.status == StatusAgendamento.CANCELADO }
                }
            }
        }
        yield()
        id(repository.salvarPaciente(1, "Nome atualizado", null))
        id(repository.alterarStatus(agendamento, StatusAgendamento.CANCELADO))
        assertEquals(1, proximaAtualizacao.await().size)
        assertTrue(repository.observarAgenda(dia.toEpochDay(), 2).first().isEmpty())
        assertEquals("Nome atualizado", repository.observarAgendamento(agendamento).first()!!.pacienteNome)
    }

    @Test fun duasGravacoesSimultaneasNaoCriamConflito() = runBlocking<Unit> {
        val resultados = listOf(async(Dispatchers.IO) { repository.salvarAgendamento(rascunho()) },
            async(Dispatchers.IO) { repository.salvarAgendamento(rascunho(paciente = 2)) }).awaitAll()
        assertEquals(1, resultados.count { it is ResultadoOperacao.Sucesso })
        assertEquals(1, resultados.count { it is ResultadoOperacao.Erro })
        assertEquals(1, repository.observarAgenda(dia.toEpochDay(), null).first().size)
    }

    @Test fun arquivoDoBancoMantemDadosAposReabrirESementeNaoSeRepete() = runBlocking<Unit> {
        val nomeTeste = "agendapsi-teste-${UUID.randomUUID()}.db"
        var persistente = Room.databaseBuilder(contexto, ClinicaDatabase::class.java, nomeTeste).build()
        try {
            var repo = RoomClinicaRepository(persistente, clock)
            repo.inicializar()
            assertEquals(4, repo.observarPacientes().first().size)
            val paciente = id(repo.salvarPaciente(null, "Persistido", "11999991234"))
            val agendamento = id(repo.salvarAgendamento(rascunho(paciente = paciente)))
            persistente.close()
            persistente = Room.databaseBuilder(contexto, ClinicaDatabase::class.java, nomeTeste).build()
            repo = RoomClinicaRepository(persistente, Clock.offset(clock, Duration.ofDays(2)))
            repo.inicializar()
            assertEquals(5, repo.observarPacientes().first().size)
            assertEquals(2, repo.observarProfissionais().first().size)
            assertEquals("11999991234", repo.obterPaciente(paciente)!!.telefone)
            assertEquals(paciente, repo.obterAgendamento(agendamento)!!.pacienteId)
            assertEquals(8, repo.observarAgenda(dia.minusDays(1).toEpochDay(), null).first().size)
            assertEquals(1, repo.observarAgenda(dia.toEpochDay(), null).first().size)
            assertTrue(repo.observarAgenda(dia.plusDays(1).toEpochDay(), null).first().isEmpty())
        } finally {
            persistente.close()
            contexto.deleteDatabase(nomeTeste)
        }
    }
}
