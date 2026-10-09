package br.edu.agendapsi

import android.content.Context
import androidx.room.Room
import br.edu.agendapsi.data.local.ClinicaDatabase
import br.edu.agendapsi.data.repository.ClinicaRepository
import br.edu.agendapsi.data.repository.RoomClinicaRepository
import java.time.Clock
import java.time.ZoneId

class AppContainer(context: Context) {
    val clock: Clock = Clock.system(ZoneId.of("America/Sao_Paulo"))
    private val db = Room.databaseBuilder(context.applicationContext, ClinicaDatabase::class.java, "agendapsi.db").build()
    val repository: ClinicaRepository = RoomClinicaRepository(db, clock)
}
