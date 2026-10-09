package br.edu.agendapsi.data.model

// JOIN traz os nomes atuais; o agendamento armazena apenas os vínculos por ID.
data class AgendamentoResumo(
    val id: Long,
    val pacienteId: Long,
    val pacienteNome: String,
    val pacienteTelefone: String?,
    val profissionalId: Long,
    val profissionalNome: String,
    val diaEpoch: Long,
    val inicioMinutos: Int,
    val duracaoMinutos: Int,
    val modalidade: Modalidade,
    val status: StatusAgendamento
)
