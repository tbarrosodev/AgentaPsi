package br.edu.agendapsi.navigation

import kotlinx.serialization.Serializable

// Mesmos seis contratos de navegação definidos na seção 23 do documento.
@Serializable data object Inicio
@Serializable data object Agenda
@Serializable data object Pacientes
@Serializable data class AgendamentoForm(
    val id: Long? = null,
    val diaInicial: Long? = null,
    val profissionalInicial: Long? = null
)
@Serializable data class AgendamentoDetalhe(val id: Long)
@Serializable data class PacienteForm(val id: Long? = null)
