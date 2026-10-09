package br.edu.agendapsi.data.local

import androidx.room.*
import br.edu.agendapsi.data.model.Modalidade
import br.edu.agendapsi.data.model.StatusAgendamento

@Entity(tableName = "agendamentos",
    foreignKeys = [
        ForeignKey(entity = PacienteEntity::class, parentColumns = ["id"], childColumns = ["pacienteId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ProfissionalEntity::class, parentColumns = ["id"], childColumns = ["profissionalId"], onDelete = ForeignKey.RESTRICT)
    ], indices = [Index("pacienteId"), Index(value = ["profissionalId", "diaEpoch"])])
data class AgendamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pacienteId: Long,
    val profissionalId: Long,
    val diaEpoch: Long,
    val inicioMinutos: Int,
    val duracaoMinutos: Int,
    val modalidade: Modalidade,
    val status: StatusAgendamento = StatusAgendamento.AGENDADO
)
