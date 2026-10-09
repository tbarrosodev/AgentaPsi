package br.edu.agendapsi.data.model

data class AgendamentoRascunho(
    val id: Long? = null,
    val pacienteId: Long? = null,
    val profissionalId: Long? = null,
    val diaEpoch: Long? = null,
    val inicioMinutos: Int? = null,
    val duracaoMinutos: Int = 50,
    val modalidade: Modalidade = Modalidade.PRESENCIAL
)
