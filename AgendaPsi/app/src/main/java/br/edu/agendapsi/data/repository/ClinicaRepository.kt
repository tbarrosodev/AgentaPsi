package br.edu.agendapsi.data.repository

import br.edu.agendapsi.data.local.*
import br.edu.agendapsi.data.model.*
import kotlinx.coroutines.flow.Flow

interface ClinicaRepository {
    suspend fun inicializar()
    fun observarAgenda(dia: Long, profissionalId: Long?): Flow<List<AgendamentoResumo>>
    fun observarPacientes(): Flow<List<PacienteEntity>>
    fun observarProfissionais(): Flow<List<ProfissionalEntity>>
    fun observarAgendamento(id: Long): Flow<AgendamentoResumo?>
    suspend fun obterPaciente(id: Long): PacienteEntity?
    suspend fun obterAgendamento(id: Long): AgendamentoEntity?
    suspend fun salvarAgendamento(r: AgendamentoRascunho): ResultadoOperacao
    suspend fun salvarPaciente(id: Long?, nome: String, telefone: String?): ResultadoOperacao
    suspend fun alterarStatus(id: Long, novo: StatusAgendamento): ResultadoOperacao
    suspend fun definirPacienteAtivo(id: Long, ativo: Boolean): ResultadoOperacao
}
