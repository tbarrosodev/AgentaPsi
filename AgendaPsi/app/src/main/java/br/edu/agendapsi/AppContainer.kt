package br.edu.agendapsi

import android.content.Context
import androidx.room.Room
import br.edu.agendapsi.data.local.ClinicaDatabase
import br.edu.agendapsi.data.local.MIGRATION_1_2
import br.edu.agendapsi.data.remote.TarefasApi
import br.edu.agendapsi.data.repository.ClinicaRepository
import br.edu.agendapsi.data.repository.RoomClinicaRepository
import java.time.Clock
import java.time.ZoneId
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(context: Context) {
    val clock: Clock = Clock.system(ZoneId.of("America/Sao_Paulo"))
    val db = Room.databaseBuilder(context.applicationContext, ClinicaDatabase::class.java, "agendapsi.db")
        .addMigrations(MIGRATION_1_2)
        .build()
    val repository: ClinicaRepository = RoomClinicaRepository(db, clock)
    val tarefasApi: TarefasApi by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TarefasApi::class.java)
    }
}
