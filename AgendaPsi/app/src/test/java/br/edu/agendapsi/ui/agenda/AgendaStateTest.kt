package br.edu.agendapsi.ui.agenda

import br.edu.agendapsi.data.model.StatusAgendamento
import br.edu.agendapsi.ui.inicio.atendimentosDemo
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class AgendaStateTest {
    private val dia = LocalDate.of(2026, 10, 7)
    private val registros = atendimentosDemo(dia)

    @Test fun listaOrdenadaIncluiCanceladosETodosOsHorarios() {
        val resultado = atendimentosDoDia(registros.reversed(), dia, null)
        assertEquals((1L..8L).toList(), resultado.map { it.id })
        assertTrue(resultado.any { it.status == StatusAgendamento.CANCELADO })
    }

    @Test fun aplicaDiaEProfissionalAoMesmoTempo() {
        val resultado = atendimentosDoDia(registros + atendimentosDemo(dia.plusDays(1)), dia, 1L)
        assertEquals(listOf(1L, 3L, 5L, 7L), resultado.map { it.id })
    }

    @Test fun dataSemRegistrosOuProfissionalSemAtendimentosRetornaVazio() {
        assertTrue(atendimentosDoDia(registros, dia.minusDays(1), null).isEmpty())
        assertTrue(atendimentosDoDia(registros, dia, 99L).isEmpty())
    }

    @Test fun conversaoUtcPreservaODiaEscolhido() {
        val primeiroJaneiro = LocalDate.of(1970, 1, 1)
        assertEquals(0L, primeiroJaneiro.paraMillisDoSeletor())
        assertEquals(primeiroJaneiro, dataDoSeletor(0L))
        assertEquals(dia, dataDoSeletor(dia.paraMillisDoSeletor()))
        val bissexto = LocalDate.of(2028, 2, 29)
        assertEquals(bissexto, dataDoSeletor(bissexto.paraMillisDoSeletor()))
    }
}
