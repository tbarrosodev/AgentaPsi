package br.edu.agendapsi.ui

import androidx.lifecycle.SavedStateHandle
import br.edu.agendapsi.data.local.*
import br.edu.agendapsi.data.model.*
import br.edu.agendapsi.data.repository.ClinicaRepository
import br.edu.agendapsi.ui.agenda.*
import br.edu.agendapsi.ui.inicio.*
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = RelogioTeste()
    private val repository = RepositorioTeste()
    private val dia = LocalDate.of(2026, 10, 9)

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun finalizar() { Dispatchers.resetMain() }

    @Test fun agendaRestauraFiltrosEObservaMudancasSemReabrirTela() = runTest(dispatcher) {
        val salvo = SavedStateHandle(mapOf("diaEpoch" to dia.plusDays(1).toEpochDay(), "profissionalId" to 2L))
        val vm = AgendaViewModel(repository, clock, salvo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(dia.plusDays(1), vm.state.value.dia)
        assertEquals(2L, vm.state.value.profissionalId)
        assertTrue((vm.state.value.conteudo as AgendaState.Disponivel).atendimentos.isEmpty())

        vm.selecionarDia(dia)
        vm.selecionarProfissional(1)
        runCurrent()
        assertEquals(dia.toEpochDay(), salvo.get<Long>("diaEpoch"))
        assertEquals(1, (vm.state.value.conteudo as AgendaState.Disponivel).atendimentos.size)
        repository.registros.value = repository.registros.value.map { it.copy(pacienteNome = "Nome corrigido") }
        runCurrent()
        assertEquals("Nome corrigido", (vm.state.value.conteudo as AgendaState.Disponivel).atendimentos.single().paciente)
    }

    @Test fun falhaDeLeituraMostraErroETentarNovamenteRecupera() = runTest(dispatcher) {
        repository.falhar = true
        val vm = AgendaViewModel(repository, clock, SavedStateHandle())
        assertEquals(AgendaState.Carregando, vm.state.value.conteudo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(AgendaState.Erro, vm.state.value.conteudo)
        repository.falhar = false
        vm.tentarNovamente()
        runCurrent()
        assertTrue(vm.state.value.conteudo is AgendaState.Disponivel)
    }

    @Test fun inicioAtualizaProximosComRelogioETrocaDiaSemInventarRegistros() = runTest(dispatcher) {
        val vm = InicioViewModel(repository, clock, SavedStateHandle())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, (vm.state.value.conteudo as InicioState.Disponivel).resumo.proximos.size)
        clock.horario = dia.atTime(8, 1)
        vm.atualizarRelogio()
        runCurrent()
        val resumo = (vm.state.value.conteudo as InicioState.Disponivel).resumo
        assertTrue(resumo.proximos.isEmpty())
        assertEquals(1, resumo.pendentes)
        clock.horario = dia.plusDays(1).atStartOfDay()
        vm.atualizarRelogio()
        runCurrent()
        assertEquals(dia.plusDays(1), vm.state.value.dia)
        assertEquals(0, (vm.state.value.conteudo as InicioState.Disponivel).resumo.total)
    }

    private class RelogioTeste : Clock() {
        var horario: LocalDateTime = LocalDateTime.of(2026, 10, 9, 7, 0)
        override fun getZone(): ZoneId = ZoneId.of("America/Sao_Paulo")
        override fun withZone(zone: ZoneId): Clock = fixed(instant(), zone)
        override fun instant(): Instant = horario.atZone(zone).toInstant()
    }

    private class RepositorioTeste : ClinicaRepository {
        var falhar = false
        val registros = MutableStateFlow(listOf(AgendamentoResumo(1, 1, "Paciente", null, 1, "Ana Costa",
            LocalDate.of(2026, 10, 9).toEpochDay(), 480, 50, Modalidade.PRESENCIAL, StatusAgendamento.AGENDADO)))
        override fun observarAgenda(dia: Long, profissionalId: Long?): Flow<List<AgendamentoResumo>> = flow {
            if (falhar) error("Falha de leitura")
            emitAll(registros.map { lista -> lista.filter { it.diaEpoch == dia && (profissionalId == null || it.profissionalId == profissionalId) } })
        }
        override fun observarProfissionais() = flowOf(listOf(ProfissionalEntity(1, "Ana Costa"), ProfissionalEntity(2, "Bruno Lima")))
        override suspend fun inicializar() = Unit
        override fun observarPacientes(): Flow<List<PacienteEntity>> = error("Não usado neste teste")
        override fun observarAgendamento(id: Long): Flow<AgendamentoResumo?> = error("Não usado neste teste")
        override suspend fun obterPaciente(id: Long): PacienteEntity? = error("Não usado neste teste")
        override suspend fun obterAgendamento(id: Long): AgendamentoEntity? = error("Não usado neste teste")
        override suspend fun salvarAgendamento(r: AgendamentoRascunho): ResultadoOperacao = error("Não usado neste teste")
        override suspend fun salvarPaciente(id: Long?, nome: String, telefone: String?): ResultadoOperacao = error("Não usado neste teste")
        override suspend fun alterarStatus(id: Long, novo: StatusAgendamento): ResultadoOperacao = error("Não usado neste teste")
        override suspend fun definirPacienteAtivo(id: Long, ativo: Boolean): ResultadoOperacao = error("Não usado neste teste")
    }
}
