package br.edu.agendapsi.data.local

import androidx.room.*
import br.edu.agendapsi.data.model.AgendamentoResumo
import kotlinx.coroutines.flow.Flow

private const val RESUMO = """
    SELECT a.id, a.pacienteId, p.nome AS pacienteNome, p.telefone AS pacienteTelefone,
        a.profissionalId, f.nome AS profissionalNome, a.diaEpoch,
        a.inicioMinutos, a.duracaoMinutos, a.modalidade, a.status
    FROM agendamentos a
    JOIN pacientes p ON p.id = a.pacienteId
    JOIN profissionais f ON f.id = a.profissionalId
"""

@Dao
interface AgendamentoDao {
    @Query(RESUMO + " WHERE a.diaEpoch = :dia AND (:profissionalId IS NULL OR a.profissionalId = :profissionalId) ORDER BY a.inicioMinutos, a.id")
    fun observarAgenda(dia: Long, profissionalId: Long?): Flow<List<AgendamentoResumo>>
    @Query(RESUMO + " WHERE a.id = :id")
    fun observarDetalhe(id: Long): Flow<AgendamentoResumo?>
    @Query("SELECT * FROM agendamentos WHERE id = :id")
    suspend fun obter(id: Long): AgendamentoEntity?
    @Query("""
        SELECT COUNT(*) FROM agendamentos
        WHERE diaEpoch = :dia AND id != :ignorarId AND status != 'CANCELADO'
            AND (profissionalId = :profissionalId OR pacienteId = :pacienteId)
            AND inicioMinutos < :novoFim AND :novoInicio < inicioMinutos + duracaoMinutos
    """)
    suspend fun contarConflitos(dia: Long, profissionalId: Long, pacienteId: Long,
        novoInicio: Int, novoFim: Int, ignorarId: Long): Int
    @Query("SELECT COUNT(*) FROM agendamentos WHERE pacienteId = :pacienteId AND status IN ('AGENDADO', 'CONFIRMADO')")
    suspend fun contarPendencias(pacienteId: Long): Int
    @Insert suspend fun inserir(valor: AgendamentoEntity): Long
    @Update suspend fun atualizar(valor: AgendamentoEntity): Int
}
