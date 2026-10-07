package br.edu.agendapsi.ui.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.agendapsi.ui.theme.AgendaPsiTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoData = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR"))
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

// A tela recebe dados e emite ações. Não conhece Activity, banco ou NavController.
@Composable
fun InicioScreen(
    dia: LocalDate,
    profissionais: List<Profissional>,
    profissionalSelecionado: Long?,
    state: InicioState,
    onProfissional: (Long?) -> Unit,
    onAgenda: () -> Unit,
    onPacientes: () -> Unit,
    onNovoAgendamento: () -> Unit,
    onAtendimento: (Long) -> Unit,
    onTentarNovamente: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Ícones vetoriais locais: sem dependência de fontes ou imagens remotas.
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = true, onClick = {},
                    icon = { Icon(InicioIcons.Home, contentDescription = null) }, label = { Text("Início") })
                NavigationBarItem(selected = false, onClick = onAgenda,
                    icon = { Icon(InicioIcons.Calendar, contentDescription = null) }, label = { Text("Agenda") })
                NavigationBarItem(selected = false, onClick = onPacientes,
                    icon = { Icon(InicioIcons.People, contentDescription = null) }, label = { Text("Pacientes") })
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AgendaPsi", color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge)
                    Text("Início", style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() })
                    Text(dia.format(formatoData).replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
            item { FiltroProfissional(profissionais, profissionalSelecionado, onProfissional) }
            when (state) {
                InicioState.Carregando -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Carregando o resumo do dia…")
                    }
                }
                InicioState.Erro -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Não foi possível carregar o resumo.")
                        OutlinedButton(onClick = onTentarNovamente, shape = MaterialTheme.shapes.small) {
                            Text("Tentar novamente")
                        }
                    }
                }
                is InicioState.Disponivel -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Total de registros do dia", color = MaterialTheme.colorScheme.onPrimary)
                                Text(state.resumo.total.toString(), style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimary)
                                Text("Inclui os atendimentos cancelados", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                    item { Indicadores(state.resumo) }
                    item {
                        Button(onClick = onNovoAgendamento, shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text("+  Novo agendamento")
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Próximos atendimentos", style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { heading() })
                            Text("Até três atendimentos pendentes de hoje", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (state.resumo.proximos.isEmpty()) {
                        item {
                            Card(shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Text(
                                    if (state.resumo.total == 0)
                                        "Nenhum atendimento hoje para este filtro. Use Novo agendamento para começar."
                                    else "Não há próximos atendimentos pendentes hoje. Consulte a Agenda para ver todos os registros.",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )
                            }
                        }
                    }
                    items(state.resumo.proximos, key = { it.id }) { atendimento ->
                        AtendimentoCard(atendimento) { onAtendimento(atendimento.id) }
                    }
                    item {
                        OutlinedButton(onClick = onAgenda, shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text("Ver agenda do dia")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroProfissional(profissionais: List<Profissional>, selecionado: Long?, onSelect: (Long?) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Profissional", style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { aberto = true }, shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(profissionais.find { it.id == selecionado }?.nome ?: "Todos os profissionais",
                    modifier = Modifier.weight(1f))
                Icon(InicioIcons.Down, contentDescription = null)
            }
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                DropdownMenuItem(text = { Text("Todos os profissionais") },
                    onClick = { aberto = false; onSelect(null) })
                profissionais.forEach { profissional ->
                    DropdownMenuItem(text = { Text(profissional.nome) },
                        onClick = { aberto = false; onSelect(profissional.id) })
                }
            }
        }
    }
}

@Composable
private fun Indicadores(resumo: ResumoDia) {
    val valores = listOf("Pendentes" to resumo.pendentes, "Concluídos" to resumo.concluidos,
        "Faltas" to resumo.faltas, "Cancelados" to resumo.cancelados)
    val escala = LocalDensity.current.fontScale
    BoxWithConstraints {
        val usarUmaColuna = maxWidth / escala < 300.dp
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // A fonte ampliada ganha uma coluna, sem alturas fixas ou texto cortado.
            if (usarUmaColuna) {
                valores.forEach { (rotulo, valor) -> Indicador(rotulo, valor, Modifier.fillMaxWidth()) }
            } else {
                valores.chunked(2).forEach { linha ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        linha.forEach { (rotulo, valor) -> Indicador(rotulo, valor, Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Indicador(rotulo: String, valor: Int, modifier: Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(rotulo, style = MaterialTheme.typography.bodyMedium)
            Text(valor.toString(), style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AtendimentoCard(atendimento: AtendimentoInicio, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${atendimento.inicio.format(formatoHora)} – ${atendimento.fim.format(formatoHora)}",
                color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Text(atendimento.paciente, style = MaterialTheme.typography.titleMedium)
            Text("ID: #${atendimento.pacienteId.toString().padStart(4, '0')}", style = MaterialTheme.typography.bodySmall)
            Text("${atendimento.profissional} • ${atendimento.modalidade}", style = MaterialTheme.typography.bodyMedium)
            StatusChip(atendimento.status)
        }
    }
}

@Composable
fun StatusChip(status: StatusAgendamento) {
    val (fundo, texto) = when (status) {
        StatusAgendamento.AGENDADO -> 0xFFE0F2FE to 0xFF0369A1
        StatusAgendamento.CONFIRMADO -> 0xFFD1FAE5 to 0xFF047857
        StatusAgendamento.CONCLUIDO -> 0xFFDCFCE7 to 0xFF15803D
        StatusAgendamento.FALTOU -> 0xFFFFEDD5 to 0xFFC2410C
        StatusAgendamento.CANCELADO -> 0xFFF1F5F9 to 0xFF5F6F85
    }
    Surface(shape = CircleShape, color = Color(fundo), contentColor = Color(texto)) {
        Text(status.rotulo, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(name = "Conteúdo", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun InicioPreview() { PreviewContent() }

@Preview(name = "Vazio", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun InicioVazioPreview() { PreviewContent(vazio = true) }

@Preview(name = "Erro", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun InicioErroPreview() { PreviewContent(estado = InicioState.Erro) }

@Preview(name = "Carregando", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun InicioCarregandoPreview() { PreviewContent(estado = InicioState.Carregando) }

@Composable
private fun PreviewContent(vazio: Boolean = false, estado: InicioState? = null) {
    val agora = LocalDateTime.of(2026, 10, 6, 10, 30)
    AgendaPsiTheme {
        InicioScreen(agora.toLocalDate(), profissionaisDemo, null,
            estado ?: InicioState.Disponivel(resumirDia(if (vazio) emptyList() else atendimentosDemo(agora.toLocalDate()), agora, null)),
            {}, {}, {}, {}, {}, {})
    }
}
