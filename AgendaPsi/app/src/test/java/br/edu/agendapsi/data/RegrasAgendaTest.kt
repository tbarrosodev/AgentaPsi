package br.edu.agendapsi.data

import br.edu.agendapsi.data.model.AgendamentoRascunho
import br.edu.agendapsi.data.model.StatusAgendamento
import br.edu.agendapsi.data.model.StatusAgendamento.*
import br.edu.agendapsi.data.rules.RegrasAgenda
import java.time.LocalDateTime
import org.junit.Assert.*
import org.junit.Test

class RegrasAgendaTest {
    private val agora = LocalDateTime.of(2026, 10, 9, 10, 30, 45)
    private val rascunho = AgendamentoRascunho(pacienteId = 1, profissionalId = 1,
        diaEpoch = agora.toLocalDate().toEpochDay(), inicioMinutos = 630)

    @Test fun exigeCamposObrigatorios() {
        listOf(rascunho.copy(pacienteId = null), rascunho.copy(profissionalId = null),
            rascunho.copy(diaEpoch = null), rascunho.copy(inicioMinutos = null)).forEach {
            assertNotNull(RegrasAgenda.validar(it, agora))
        }
    }

    @Test fun permiteMinutoAtualMasNaoPassado() {
        assertNull(RegrasAgenda.validar(rascunho, agora))
        assertNotNull(RegrasAgenda.validar(rascunho.copy(inicioMinutos = 629), agora))
        assertNotNull(RegrasAgenda.validar(rascunho.copy(diaEpoch = rascunho.diaEpoch!! - 1), agora))
    }

    @Test fun respeitaInicioEFimDoExpedienteEDuracoes() {
        val amanha = rascunho.copy(diaEpoch = rascunho.diaEpoch!! + 1)
        assertNull(RegrasAgenda.validar(amanha.copy(inicioMinutos = 480), agora))
        assertNull(RegrasAgenda.validar(amanha.copy(inicioMinutos = 1020, duracaoMinutos = 60), agora))
        assertNull(RegrasAgenda.validar(amanha.copy(inicioMinutos = 1050, duracaoMinutos = 30), agora))
        listOf(amanha.copy(inicioMinutos = 479), amanha.copy(inicioMinutos = 1031),
            amanha.copy(duracaoMinutos = 45), amanha.copy(inicioMinutos = Int.MAX_VALUE),
            amanha.copy(diaEpoch = Long.MAX_VALUE)).forEach {
            assertNotNull(RegrasAgenda.validar(it, agora))
        }
    }

    @Test fun concluirOuMarcarFaltaSomenteAPartirDoFim() {
        for (estado in listOf(AGENDADO, CONFIRMADO)) {
            for (novo in listOf(CONCLUIDO, FALTOU)) {
                assertFalse(RegrasAgenda.podeAlterarStatus(estado, novo, agora, agora.minusSeconds(1)))
                assertTrue(RegrasAgenda.podeAlterarStatus(estado, novo, agora, agora))
            }
        }
    }

    @Test fun encerradosSaoImutaveisEConfirmacaoNaoPodeSerRevertida() {
        for (estado in listOf(CONCLUIDO, FALTOU, CANCELADO)) {
            for (novo in StatusAgendamento.entries) assertFalse(RegrasAgenda.podeAlterarStatus(estado, novo, agora, agora.plusDays(1)))
        }
        assertTrue(RegrasAgenda.podeAlterarStatus(AGENDADO, CONFIRMADO, agora, agora))
        assertFalse(RegrasAgenda.podeAlterarStatus(CONFIRMADO, AGENDADO, agora, agora))
        assertTrue(RegrasAgenda.podeAlterarStatus(CONFIRMADO, CANCELADO, agora, agora.minusHours(1)))
    }
}
