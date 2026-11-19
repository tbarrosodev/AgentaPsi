package br.edu.agendapsi.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://brasilapi.com.br/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val feriadosApi: FeriadosApi = retrofit.create(FeriadosApi::class.java)
}
