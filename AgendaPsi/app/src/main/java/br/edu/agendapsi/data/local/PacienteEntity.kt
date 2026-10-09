package br.edu.agendapsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nome: String,
    val telefone: String? = null,
    val ativo: Boolean = true
)
