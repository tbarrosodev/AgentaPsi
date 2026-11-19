package br.edu.agendapsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avisos")
data class AvisoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val titulo: String,
    val mensagem: String,
    val data: String
)
