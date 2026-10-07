package br.edu.agendapsi.ui.inicio

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class StatusAgendamento(val rotulo: String) {
    AGENDADO("Agendado"), CONFIRMADO("Confirmado"), CONCLUIDO("Concluído"),
    FALTOU("Faltou"), CANCELADO("Cancelado")
}

data class Profissional(val id: Long, val nome: String)

// Modelo de apresentação. A futura consulta Room deve projetar os dados neste formato.
data class AtendimentoInicio(
    val id: Long, val pacienteId: Long, val paciente: String,
    val profissionalId: Long, val profissional: String,
    val inicio: LocalDateTime, val fim: LocalDateTime,
    val modalidade: String, val status: StatusAgendamento
)

data class ResumoDia(
    val total: Int, val pendentes: Int, val concluidos: Int,
    val faltas: Int, val cancelados: Int, val proximos: List<AtendimentoInicio>
)

// Função pura: totais e próximos usam exatamente o mesmo dia e profissional.
fun resumirDia(
    atendimentos: List<AtendimentoInicio>, agora: LocalDateTime, profissionalId: Long?
): ResumoDia {
    val doDia = atendimentos.filter {
        it.inicio.toLocalDate() == agora.toLocalDate() &&
            (profissionalId == null || it.profissionalId == profissionalId)
    }
    val pendentes = doDia.filter {
        it.status == StatusAgendamento.AGENDADO || it.status == StatusAgendamento.CONFIRMADO
    }
    return ResumoDia(
        total = doDia.size, pendentes = pendentes.size,
        concluidos = doDia.count { it.status == StatusAgendamento.CONCLUIDO },
        faltas = doDia.count { it.status == StatusAgendamento.FALTOU },
        cancelados = doDia.count { it.status == StatusAgendamento.CANCELADO },
        proximos = pendentes.filter { !it.inicio.isBefore(agora) }
            .sortedWith(compareBy<AtendimentoInicio> { it.inicio }.thenBy { it.id }).take(3)
    )
}

sealed interface InicioState {
    data object Carregando : InicioState
    data object Erro : InicioState
    data class Disponivel(val resumo: ResumoDia) : InicioState
}

val profissionaisDemo = listOf(Profissional(1, "Ana Costa"), Profissional(2, "Bruno Lima"))

// Dados fictícios somente para esta primeira entrega; não simulam persistência.
fun atendimentosDemo(dia: LocalDate): List<AtendimentoInicio> {
    val nomes = listOf("Marina Alves", "João Silva", "Clara Souza", "Pedro Santos", "Luiza Rocha", "Rafael Melo", "Beatriz Lima", "João Silva")
    val horas = listOf(8, 9, 10, 11, 13, 14, 16, 17)
    val estados = listOf(StatusAgendamento.CONCLUIDO, StatusAgendamento.FALTOU,
        StatusAgendamento.CANCELADO, StatusAgendamento.AGENDADO,
        StatusAgendamento.CONFIRMADO, StatusAgendamento.AGENDADO,
        StatusAgendamento.CONFIRMADO, StatusAgendamento.AGENDADO)
    return nomes.mapIndexed { i, nome ->
        val profissional = profissionaisDemo[i % 2]
        val inicio = dia.atTime(LocalTime.of(horas[i], 0))
        AtendimentoInicio(i + 1L, i + 1L, nome, profissional.id, profissional.nome,
            inicio, inicio.plusMinutes(50), if (i % 2 == 0) "Presencial" else "Online", estados[i])
    }
}
