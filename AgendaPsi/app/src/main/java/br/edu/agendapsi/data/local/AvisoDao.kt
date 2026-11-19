package br.edu.agendapsi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AvisoDao {

    @Query("SELECT * FROM avisos ORDER BY id")
    suspend fun listarTodos(): List<AvisoEntity>

    @Query("SELECT COUNT(*) FROM avisos")
    suspend fun contar(): Int

    @Insert
    suspend fun inserirTodos(avisos: List<AvisoEntity>)
}
