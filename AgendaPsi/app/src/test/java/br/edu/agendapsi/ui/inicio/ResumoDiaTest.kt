package br.edu.agendapsi.ui.inicio

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class ResumoDiaTest {
    private val agora = LocalDateTime.of(2026, 10, 6, 10, 30)
    private val dados = atendimentosDemo(agora.toLocalDate())

    @Test fun totalIncluiCanceladosESomaDosIndicadores() {
        val r = resumirDia(dados, agora, null)
        assertEquals(8, r.total)
        assertEquals(5, r.pendentes)
        assertEquals(1, r.cancelados)
        assertEquals(r.total, r.pendentes + r.concluidos + r.faltas + r.cancelados)
    }

    @Test fun proximosSaoPendentesFuturosOrdenadosELimitadosATres() {
        val r = resumirDia(dados.reversed(), agora, null)
        assertEquals(listOf(4L, 5L, 6L), r.proximos.map { it.id })
        assertTrue(resumirDia(dados, agora.withHour(18), null).proximos.isEmpty())
    }

    @Test fun filtroAplicaAoResumoEAosProximos() {
        val r = resumirDia(dados, agora, 1)
        assertEquals(4, r.total)
        assertEquals(2, r.pendentes)
        assertEquals(listOf(5L, 7L), r.proximos.map { it.id })
    }

    @Test fun outroDiaEFiltroSemRegistrosRetornamVazio() {
        assertEquals(0, resumirDia(dados, agora.plusDays(1), null).total)
        assertEquals(0, resumirDia(dados, agora, 99).total)
    }

    @Test fun horarioExatoIncluidoEHorarioIniciadoExcluido() {
        val inicio = agora.withHour(11).withMinute(0)
        assertEquals(4L, resumirDia(dados, inicio, null).proximos.first().id)
        assertEquals(5L, resumirDia(dados, inicio.plusSeconds(1), null).proximos.first().id)
        assertEquals(5, resumirDia(dados, inicio.plusSeconds(1), null).pendentes)
    }
}
