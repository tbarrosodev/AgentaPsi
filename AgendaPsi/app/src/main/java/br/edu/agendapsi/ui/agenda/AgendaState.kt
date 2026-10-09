package br.edu.agendapsi.ui.agenda

import br.edu.agendapsi.ui.inicio.AtendimentoInicio
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

sealed interface AgendaState {
    data object Carregando : AgendaState
    data object Erro : AgendaState
    data class Disponivel(val atendimentos: List<AtendimentoInicio>) : AgendaState
}

// Cancelados continuam na consulta: a Agenda também apresenta o histórico.
fun atendimentosDoDia(
    registros: List<AtendimentoInicio>, dia: LocalDate, profissionalId: Long?
): List<AtendimentoInicio> = registros.filter {
    it.inicio.toLocalDate() == dia &&
        (profissionalId == null || it.profissionalId == profissionalId)
}.sortedWith(compareBy<AtendimentoInicio> { it.inicio }.thenBy { it.id })

// O DatePicker usa meia-noite UTC; a rota usa epochDay, que representa só a data.
fun LocalDate.paraMillisDoSeletor(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun dataDoSeletor(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
