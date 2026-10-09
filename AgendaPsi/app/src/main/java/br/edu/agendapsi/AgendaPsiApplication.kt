package br.edu.agendapsi

import android.app.Application

class AgendaPsiApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
