package br.edu.agendapsi.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface FeriadosApi {

    @GET("feriados/v1/{ano}")
    suspend fun listarFeriados(@Path("ano") ano: Int): List<Feriado>
}
