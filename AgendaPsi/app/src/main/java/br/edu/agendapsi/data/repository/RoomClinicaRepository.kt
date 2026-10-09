package br.edu.agendapsi.data.repository

import androidx.room.withTransaction
import br.edu.agendapsi.data.local.*
import br.edu.agendapsi.data.model.*
import br.edu.agendapsi.data.rules.RegrasAgenda
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

class RoomClinicaRepository(private val db: ClinicaDatabase, private val clock: Clock) : ClinicaRepository {
    private val pacientes = db.pacienteDao()
    private val profissionais = db.profissionalDao()
    private val agendamentos = db.agendamentoDao()

    override suspend fun inicializar() {
        db.withTransaction {
            if (profissionais.contar() != 0) return@withTransaction
            val ana = profissionais.inserir(ProfissionalEntity(nome = "Ana Costa"))
            val bruno = profissionais.inserir(ProfissionalEntity(nome = "Bruno Lima"))
            val ids = listOf("Marina Alves", "João Silva", "Clara Souza", "João Silva").map {
                pacientes.inserir(PacienteEntity(nome = it))
            }
            val agora = LocalDateTime.now(clock)
            val dia = agora.toLocalDate()
            listOf(8, 9, 10, 11, 13, 14, 16, 17).forEachIndexed { i, hora ->
                val encerrado = !agora.isBefore(dia.atTime(hora, 50))
                val status = when {
                    i == 2 -> StatusAgendamento.CANCELADO
                    encerrado && i == 1 -> StatusAgendamento.FALTOU
                    encerrado -> StatusAgendamento.CONCLUIDO
                    i % 2 == 0 -> StatusAgendamento.CONFIRMADO
                    else -> StatusAgendamento.AGENDADO
                }
                agendamentos.inserir(AgendamentoEntity(pacienteId = ids[i % ids.size],
                    profissionalId = if (i % 2 == 0) ana else bruno, diaEpoch = dia.toEpochDay(),
                    inicioMinutos = hora * 60, duracaoMinutos = 50,
                    modalidade = if (i % 2 == 0) Modalidade.PRESENCIAL else Modalidade.ONLINE, status = status))
            }
        }
    }

    // Todas as entradas aguardam a carga inicial. Uma falha reverte a transação e permite tentar de novo.
    override fun observarAgenda(dia: Long, profissionalId: Long?): Flow<List<AgendamentoResumo>> =
        agendamentos.observarAgenda(dia, profissionalId).onStart { inicializar() }
    override fun observarPacientes(): Flow<List<PacienteEntity>> = pacientes.observarTodos().onStart { inicializar() }
    override fun observarProfissionais(): Flow<List<ProfissionalEntity>> = profissionais.observarTodos().onStart { inicializar() }
    override fun observarAgendamento(id: Long): Flow<AgendamentoResumo?> = agendamentos.observarDetalhe(id).onStart { inicializar() }
    override suspend fun obterPaciente(id: Long): PacienteEntity? { inicializar(); return pacientes.obter(id) }
    override suspend fun obterAgendamento(id: Long): AgendamentoEntity? { inicializar(); return agendamentos.obter(id) }

    override suspend fun salvarPaciente(id: Long?, nome: String, telefone: String?): ResultadoOperacao {
        val nomeLimpo = nome.trim()
        if (nomeLimpo.length !in 2..100) return ResultadoOperacao.Erro("Informe um nome entre 2 e 100 caracteres")
        val entrada = telefone.orEmpty().trim()
        if (entrada.any { it !in "0123456789 ()-+." }) return ResultadoOperacao.Erro("Telefone inválido")
        val digitos = entrada.filter { it in '0'..'9' }
        if (entrada.isNotEmpty() && digitos.length !in 10..11) return ResultadoOperacao.Erro("Use DDD e telefone com 10 ou 11 dígitos")
        inicializar()
        return db.withTransaction {
            val anterior = id?.let { pacientes.obter(it) }
            if (id != null && anterior == null) return@withTransaction ResultadoOperacao.Erro("Paciente não encontrado")
            val contato = digitos.takeIf { it.isNotEmpty() }
            val valor = anterior?.copy(nome = nomeLimpo, telefone = contato) ?: PacienteEntity(nome = nomeLimpo, telefone = contato)
            val salvo = if (id == null) pacientes.inserir(valor) else { check(pacientes.atualizar(valor) == 1); id }
            ResultadoOperacao.Sucesso(salvo)
        }
    }

    override suspend fun salvarAgendamento(r: AgendamentoRascunho): ResultadoOperacao {
        inicializar()
        return db.withTransaction {
            RegrasAgenda.validar(r, LocalDateTime.now(clock))?.let { return@withTransaction ResultadoOperacao.Erro(it) }
            val pacienteId = requireNotNull(r.pacienteId)
            val profissionalId = requireNotNull(r.profissionalId)
            val dia = requireNotNull(r.diaEpoch)
            val inicio = requireNotNull(r.inicioMinutos)
            val anterior = r.id?.let { agendamentos.obter(it) }
            if (r.id != null && anterior == null) return@withTransaction ResultadoOperacao.Erro("Agendamento não encontrado")
            if (anterior != null && !anterior.status.pendente) return@withTransaction ResultadoOperacao.Erro("Esse atendimento já foi encerrado")
            if (pacientes.obter(pacienteId)?.ativo != true || profissionais.obter(profissionalId) == null) {
                return@withTransaction ResultadoOperacao.Erro("Paciente ou profissional indisponível")
            }
            if (agendamentos.contarConflitos(dia, profissionalId, pacienteId, inicio, inicio + r.duracaoMinutos, r.id ?: 0) > 0) {
                return@withTransaction ResultadoOperacao.Erro("Paciente ou profissional já tem esse horário")
            }
            val valor = AgendamentoEntity(r.id ?: 0, pacienteId, profissionalId, dia, inicio, r.duracaoMinutos, r.modalidade)
            val salvo = if (r.id == null) agendamentos.inserir(valor) else { check(agendamentos.atualizar(valor) == 1); r.id }
            ResultadoOperacao.Sucesso(salvo)
        }
    }

    override suspend fun alterarStatus(id: Long, novo: StatusAgendamento): ResultadoOperacao {
        inicializar()
        return db.withTransaction {
            val anterior = agendamentos.obter(id) ?: return@withTransaction ResultadoOperacao.Erro("Agendamento não encontrado")
            val fim = LocalDate.ofEpochDay(anterior.diaEpoch).atStartOfDay().plusMinutes((anterior.inicioMinutos + anterior.duracaoMinutos).toLong())
            if (!RegrasAgenda.podeAlterarStatus(anterior.status, novo, fim, LocalDateTime.now(clock))) {
                return@withTransaction ResultadoOperacao.Erro("Mudança de situação não permitida neste horário ou estado")
            }
            check(agendamentos.atualizar(anterior.copy(status = novo)) == 1)
            ResultadoOperacao.Sucesso(id)
        }
    }

    override suspend fun definirPacienteAtivo(id: Long, ativo: Boolean): ResultadoOperacao {
        inicializar()
        return db.withTransaction {
            val paciente = pacientes.obter(id) ?: return@withTransaction ResultadoOperacao.Erro("Paciente não encontrado")
            if (!ativo && agendamentos.contarPendencias(id) > 0) {
                return@withTransaction ResultadoOperacao.Erro("Resolva os agendamentos pendentes antes de arquivar o paciente")
            }
            check(pacientes.atualizar(paciente.copy(ativo = ativo)) == 1)
            ResultadoOperacao.Sucesso(id)
        }
    }
}
