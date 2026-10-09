package br.edu.agendapsi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfissionalDao {
    @Query("SELECT * FROM profissionais ORDER BY nome, id")
    fun observarTodos(): Flow<List<ProfissionalEntity>>
    @Query("SELECT * FROM profissionais WHERE id = :id")
    suspend fun obter(id: Long): ProfissionalEntity?
    @Query("SELECT COUNT(*) FROM profissionais")
    suspend fun contar(): Int
    @Insert suspend fun inserir(valor: ProfissionalEntity): Long
}
