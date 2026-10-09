package br.edu.agendapsi.ui.components

import br.edu.agendapsi.data.model.AgendamentoResumo
import br.edu.agendapsi.ui.inicio.AtendimentoInicio
import java.time.LocalDate

fun AgendamentoResumo.paraApresentacao(): AtendimentoInicio {
    val inicio = LocalDate.ofEpochDay(diaEpoch).atStartOfDay().plusMinutes(inicioMinutos.toLong())
    return AtendimentoInicio(id, pacienteId, pacienteNome, profissionalId, profissionalNome,
        inicio, inicio.plusMinutes(duracaoMinutos.toLong()), modalidade.rotulo, status)
}
