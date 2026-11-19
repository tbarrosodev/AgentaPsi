package br.edu.agendapsi.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PacienteEntity::class, ProfissionalEntity::class, AgendamentoEntity::class, AvisoEntity::class],
    version = 3, exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 3)])
@TypeConverters(Converters::class)
abstract class ClinicaDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun profissionalDao(): ProfissionalDao
    abstract fun agendamentoDao(): AgendamentoDao
    abstract fun avisoDao(): AvisoDao
}

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Recria a tabela dependente para manter as chaves estrangeiras válidas durante a migração.
        db.execSQL("CREATE TEMP TABLE agendamentos_v1 AS SELECT * FROM agendamentos")
        db.execSQL("DROP TABLE agendamentos")
        db.execSQL("CREATE TABLE pacientes_novos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL)")
        db.execSQL("INSERT INTO pacientes_novos (id, nome) SELECT id, nome FROM pacientes")
        db.execSQL("DROP TABLE pacientes")
        db.execSQL("ALTER TABLE pacientes_novos RENAME TO pacientes")

        db.execSQL("""
            CREATE TABLE agendamentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                pacienteId INTEGER NOT NULL,
                profissionalId INTEGER NOT NULL,
                diaEpoch INTEGER NOT NULL,
                inicioMinutos INTEGER NOT NULL,
                duracaoMinutos INTEGER NOT NULL,
                modalidade TEXT NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (pacienteId) REFERENCES pacientes(id) ON DELETE RESTRICT,
                FOREIGN KEY (profissionalId) REFERENCES profissionais(id) ON DELETE RESTRICT
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO agendamentos (id, pacienteId, profissionalId, diaEpoch, inicioMinutos, duracaoMinutos, modalidade, status)
            SELECT id, pacienteId, profissionalId, diaEpoch, inicioMinutos, duracaoMinutos, modalidade, status
            FROM agendamentos_v1
        """.trimIndent())
        db.execSQL("DROP TABLE agendamentos_v1")
        db.execSQL("CREATE INDEX index_agendamentos_pacienteId ON agendamentos (pacienteId)")
        db.execSQL("CREATE INDEX index_agendamentos_profissionalId_diaEpoch ON agendamentos (profissionalId, diaEpoch)")
    }
}
