package br.edu.agendapsi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PacienteEntity::class, ProfissionalEntity::class, AgendamentoEntity::class],
    version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ClinicaDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun profissionalDao(): ProfissionalDao
    abstract fun agendamentoDao(): AgendamentoDao
}
