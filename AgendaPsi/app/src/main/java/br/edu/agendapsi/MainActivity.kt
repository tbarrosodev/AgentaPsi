package br.edu.agendapsi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import br.edu.agendapsi.ui.inicio.*
import br.edu.agendapsi.ui.theme.AgendaPsiTheme
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AgendaPsiTheme { InicioDemo() } }
    }
}

@Composable
private fun InicioDemo() {
    val fuso = remember { ZoneId.of("America/Sao_Paulo") }
    var agora by remember { mutableStateOf(LocalDateTime.now(fuso)) }
    var profissionalId by rememberSaveable { mutableStateOf<Long?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // Atualiza imediatamente ao voltar ao aplicativo e a cada minuto em primeiro plano.
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                agora = LocalDateTime.now(fuso)
                delay(60_000)
            }
        }
    }
    val atendimentos = remember(agora.toLocalDate()) { atendimentosDemo(agora.toLocalDate()) }
    val resumo = resumirDia(atendimentos, agora, profissionalId)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun proximaTela(nome: String) {
        scope.launch { snackbar.showSnackbar("$nome será implementado na próxima etapa.") }
    }
    InicioScreen(
        dia = agora.toLocalDate(), profissionais = profissionaisDemo,
        profissionalSelecionado = profissionalId, state = InicioState.Disponivel(resumo),
        onProfissional = { profissionalId = it },
        onAgenda = { proximaTela("Agenda") }, onPacientes = { proximaTela("Pacientes") },
        onNovoAgendamento = { proximaTela("Novo agendamento") },
        onAtendimento = { proximaTela("Detalhes do agendamento #$it") },
        onTentarNovamente = { agora = LocalDateTime.now(fuso) },
        snackbarHostState = snackbar
    )
}
