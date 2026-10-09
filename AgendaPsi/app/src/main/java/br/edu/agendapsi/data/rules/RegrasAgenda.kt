package br.edu.agendapsi.data.rules

import br.edu.agendapsi.data.model.AgendamentoRascunho
import br.edu.agendapsi.data.model.StatusAgendamento
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime

object RegrasAgenda {
    fun validar(r: AgendamentoRascunho, agora: LocalDateTime): String? {
        if (r.pacienteId == null) return "Selecione o paciente"
        if (r.profissionalId == null) return "Selecione o profissional"
        val dia = r.diaEpoch ?: return "Escolha a data"
        val inicio = r.inicioMinutos ?: return "Escolha o horário"
        try { LocalDate.ofEpochDay(dia) } catch (_: DateTimeException) { return "Data inválida" }
        if (r.duracaoMinutos !in setOf(30, 50, 60)) return "Duração inválida"
        if (inicio !in 480..1080) return "Horário fora do expediente"
        if (inicio + r.duracaoMinutos > 1080) return "O atendimento termina após 18h"
        val hoje = agora.toLocalDate().toEpochDay()
        if (dia < hoje || (dia == hoje && inicio < agora.hour * 60 + agora.minute)) {
            return "Escolha um horário presente ou futuro"
        }
        return null
    }

    fun podeAlterarStatus(atual: StatusAgendamento, novo: StatusAgendamento,
        fim: LocalDateTime, agora: LocalDateTime): Boolean = when {
        !atual.pendente -> false
        novo == StatusAgendamento.CANCELADO -> true
        atual == StatusAgendamento.AGENDADO && novo == StatusAgendamento.CONFIRMADO -> true
        novo == StatusAgendamento.CONCLUIDO || novo == StatusAgendamento.FALTOU -> !agora.isBefore(fim)
        else -> false
    }
}
