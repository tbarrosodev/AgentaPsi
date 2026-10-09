package br.edu.agendapsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profissionais")
data class ProfissionalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nome: String
)
