package br.edu.agendapsi.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.edu.agendapsi.data.repository.ClinicaRepository
import br.edu.agendapsi.ui.agenda.*
import br.edu.agendapsi.ui.components.*
import br.edu.agendapsi.ui.inicio.*
import java.time.LocalDate
import java.time.Clock
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun AppNavHost(repository: ClinicaRepository, clock: Clock) {
    val nav = rememberNavController()
    val inicioVm: InicioViewModel = viewModel(factory = remember(repository, clock) { InicioViewModel.factory(repository, clock) })
    val agendaVm: AgendaViewModel = viewModel(factory = remember(repository, clock) { AgendaViewModel.factory(repository, clock) })
    val inicio by inicioVm.state.collectAsStateWithLifecycle()
    val agenda by agendaVm.state.collectAsStateWithLifecycle()
    val onProfissional: (Long?) -> Unit = {
        inicioVm.selecionarProfissional(it)
        agendaVm.selecionarProfissional(it)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, inicioVm) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                inicioVm.atualizarRelogio()
                delay(60_000)
            }
        }
    }
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
                dia = inicio.dia, profissionais = inicio.profissionais,
                profissionalSelecionado = inicio.profissionalId,
                state = inicio.conteudo,
                onProfissional = onProfissional, onAgenda = onAgenda, onPacientes = onPacientes,
                onNovoAgendamento = {
                    nav.navigate(AgendamentoForm(diaInicial = inicio.dia.toEpochDay(), profissionalInicial = inicio.profissionalId))
                },
                onAtendimento = { nav.navigate(AgendamentoDetalhe(it)) },
                onTentarNovamente = inicioVm::tentarNovamente,
                onAgendaDoDia = { agendaVm.selecionarDia(LocalDate.now(clock)); onAgenda() }
            )
        }
        composable<Agenda> {
            AgendaScreen(
                dia = agenda.dia, profissionais = agenda.profissionais, profissionalSelecionado = agenda.profissionalId,
                state = agenda.conteudo,
                onDia = agendaVm::selecionarDia, onProfissional = onProfissional,
                onInicio = onInicio, onPacientes = onPacientes,
                onNovoAgendamento = { data, profissional ->
                    nav.navigate(AgendamentoForm(diaInicial = data.toEpochDay(), profissionalInicial = profissional))
                },
                onAtendimento = { nav.navigate(AgendamentoDetalhe(it)) },
                onTentarNovamente = agendaVm::tentarNovamente
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
            val profissional = inicio.profissionais.find { it.id == rota.profissionalInicial }?.nome ?: "A selecionar"
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
