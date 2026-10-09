package br.edu.agendapsi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Query("SELECT * FROM pacientes ORDER BY nome, id")
    fun observarTodos(): Flow<List<PacienteEntity>>
    @Query("SELECT * FROM pacientes WHERE id = :id")
    suspend fun obter(id: Long): PacienteEntity?
    @Insert suspend fun inserir(paciente: PacienteEntity): Long
    @Update suspend fun atualizar(paciente: PacienteEntity): Int
}
