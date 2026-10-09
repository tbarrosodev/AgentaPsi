package br.edu.agendapsi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.agendapsi.navigation.AppNavHost
import br.edu.agendapsi.ui.theme.AgendaPsiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as AgendaPsiApplication).container
        setContent { AgendaPsiTheme { AppNavHost(container.repository, container.clock) } }
    }
}
