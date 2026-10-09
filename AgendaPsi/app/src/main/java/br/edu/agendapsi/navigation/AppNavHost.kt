package br.edu.agendapsi.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.edu.agendapsi.ui.agenda.*
import br.edu.agendapsi.ui.components.*
import br.edu.agendapsi.ui.inicio.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val fuso = remember { ZoneId.of("America/Sao_Paulo") }
    var agora by remember { mutableStateOf(LocalDateTime.now(fuso)) }
    var profissionalId by rememberSaveable { mutableStateOf<Long?>(null) }
    var diaAgenda by rememberSaveable { mutableStateOf(agora.toLocalDate().toEpochDay()) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                agora = LocalDateTime.now(fuso)
                delay(60_000)
            }
        }
    }
    // Mesma amostra para as duas telas; trocar a data não inventa novos registros.
    val registros = remember(agora.toLocalDate()) { atendimentosDemo(agora.toLocalDate()) }
    fun abrirPrincipal(destino: Any) {
        nav.navigate(destino) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val onInicio = { abrirPrincipal(Inicio) }
    val onAgenda = { abrirPrincipal(Agenda) }
    val onPacientes = { abrirPrincipal(Pacientes) }

    NavHost(navController = nav, startDestination = Inicio) {
        composable<Inicio> {
            InicioScreen(
                dia = agora.toLocalDate(), profissionais = profissionaisDemo,
                profissionalSelecionado = profissionalId,
                state = InicioState.Disponivel(resumirDia(registros, agora, profissionalId)),
                onProfissional = { profissionalId = it }, onAgenda = onAgenda, onPacientes = onPacientes,
                onNovoAgendamento = {
                    nav.navigate(AgendamentoForm(diaInicial = agora.toLocalDate().toEpochDay(), profissionalInicial = profissionalId))
                },
                onAtendimento = { nav.navigate(AgendamentoDetalhe(it)) },
                onTentarNovamente = { agora = LocalDateTime.now(fuso) },
                onAgendaDoDia = { diaAgenda = agora.toLocalDate().toEpochDay(); onAgenda() }
            )
        }
        composable<Agenda> {
            val dia = LocalDate.ofEpochDay(diaAgenda)
            AgendaScreen(
                dia = dia, profissionais = profissionaisDemo, profissionalSelecionado = profissionalId,
                state = AgendaState.Disponivel(atendimentosDoDia(registros, dia, profissionalId)),
                onDia = { diaAgenda = it.toEpochDay() }, onProfissional = { profissionalId = it },
                onInicio = onInicio, onPacientes = onPacientes,
                onNovoAgendamento = { data, profissional ->
                    nav.navigate(AgendamentoForm(diaInicial = data.toEpochDay(), profissionalInicial = profissional))
                },
                onAtendimento = { nav.navigate(AgendamentoDetalhe(it)) },
                onTentarNovamente = { agora = LocalDateTime.now(fuso) }
            )
        }
        // Substituir estes conteúdos pelas telas dos colegas, preservando as rotas.
        composable<Pacientes> {
            DestinoEmDesenvolvimento("Pacientes", "Esta tela será integrada pelo grupo.",
                onVoltar = { nav.popBackStack() },
                barra = { BarraPrincipal(DestinoPrincipal.PACIENTES, onInicio, onAgenda, {}) })
        }
        composable<AgendamentoForm> { entrada ->
            val rota = entrada.toRoute<AgendamentoForm>()
            val data = rota.diaInicial?.let { LocalDate.ofEpochDay(it) }
                ?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "A selecionar"
            val profissional = profissionaisDemo.find { it.id == rota.profissionalInicial }?.nome ?: "A selecionar"
            DestinoEmDesenvolvimento("Novo agendamento",
                "O formulário será integrado pelo grupo.\n\nData: $data\nProfissional: $profissional",
                onVoltar = { nav.popBackStack() })
        }
        composable<AgendamentoDetalhe> { entrada ->
            val rota = entrada.toRoute<AgendamentoDetalhe>()
            DestinoEmDesenvolvimento("Detalhes do agendamento",
                "A tela de detalhes será integrada pelo grupo.\n\nAgendamento #${rota.id}",
                onVoltar = { nav.popBackStack() })
        }
        composable<PacienteForm> {
            DestinoEmDesenvolvimento("Cadastro de paciente", "O formulário será integrado pelo grupo.",
                onVoltar = { nav.popBackStack() })
        }
    }
}

@Composable
private fun DestinoEmDesenvolvimento(
    titulo: String, mensagem: String, onVoltar: () -> Unit,
    barra: @Composable () -> Unit = {}
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = barra) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MarcaAgendaPsi()
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            Text(mensagem)
            OutlinedButton(onClick = onVoltar, shape = MaterialTheme.shapes.small) { Text("Voltar") }
        }
    }
}
