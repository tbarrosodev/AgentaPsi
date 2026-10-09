package br.edu.agendapsi.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.agendapsi.ui.components.*
import br.edu.agendapsi.ui.inicio.Profissional
import br.edu.agendapsi.ui.inicio.atendimentosDemo
import br.edu.agendapsi.ui.inicio.profissionaisDemo
import br.edu.agendapsi.ui.theme.AgendaPsiTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoDia = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
private val formatoCurto = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun AgendaScreen(
    dia: LocalDate,
    profissionais: List<Profissional>,
    profissionalSelecionado: Long?,
    state: AgendaState,
    onDia: (LocalDate) -> Unit,
    onProfissional: (Long?) -> Unit,
    onInicio: () -> Unit,
    onPacientes: () -> Unit,
    onNovoAgendamento: (LocalDate, Long?) -> Unit,
    onAtendimento: (Long) -> Unit,
    onTentarNovamente: () -> Unit
) {
    var seletorAberto by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BarraPrincipal(DestinoPrincipal.AGENDA, onInicio, {}, onPacientes) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MarcaAgendaPsi()
                    Text(dia.format(formatoDia).replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onDia(dia.minusDays(1)) },
                        enabled = dia > LocalDate.of(1, 1, 1),
                        modifier = Modifier.semantics { contentDescription = "Dia anterior" }) {
                        Text("‹", style = MaterialTheme.typography.headlineSmall)
                    }
                    OutlinedButton(onClick = { seletorAberto = true },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .semantics { contentDescription = "Selecionar data: ${dia.format(formatoCurto)}" }) {
                        Text(dia.format(formatoCurto))
                    }
                    IconButton(onClick = { onDia(dia.plusDays(1)) },
                        enabled = dia < LocalDate.of(9999, 12, 31),
                        modifier = Modifier.semantics { contentDescription = "Dia seguinte" }) {
                        Text("›", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            item { FiltroProfissional(profissionais, profissionalSelecionado, onProfissional) }
            item {
                Button(onClick = { onNovoAgendamento(dia, profissionalSelecionado) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("+  Novo agendamento")
                }
            }
            item {
                Text("Atendimentos do dia", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() })
            }
            when (state) {
                AgendaState.Carregando -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Carregando os atendimentos…")
                    }
                }
                AgendaState.Erro -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Não foi possível carregar a agenda.")
                        OutlinedButton(onClick = onTentarNovamente, shape = MaterialTheme.shapes.small) {
                            Text("Tentar novamente")
                        }
                    }
                }
                is AgendaState.Disponivel -> {
                    if (state.atendimentos.isEmpty()) {
                        item {
                            Card(shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Text("Nenhum atendimento para esta data e profissional. Escolha outra data ou use Novo agendamento.",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp))
                            }
                        }
                    }
                    items(state.atendimentos, key = { it.id }) { atendimento ->
                        AtendimentoCard(atendimento) { onAtendimento(atendimento.id) }
                    }
                }
            }
        }
    }
    if (seletorAberto) {
        SeletorDataAgenda(dia,
            onConfirmar = { onDia(it); seletorAberto = false },
            onCancelar = { seletorAberto = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDataAgenda(dia: LocalDate, onConfirmar: (LocalDate) -> Unit, onCancelar: () -> Unit) {
    // Estado temporário do diálogo: Cancelar não modifica a data da Agenda.
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = dia.paraMillisDoSeletor(), yearRange = 1..9999
    )
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(enabled = picker.selectedDateMillis != null,
                onClick = { picker.selectedDateMillis?.let { onConfirmar(dataDoSeletor(it)) } }) {
                Text("Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    ) {
        DatePicker(state = picker, modifier = Modifier.verticalScroll(rememberScrollState()))
    }
}

@Preview(name = "Conteúdo", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun AgendaPreview() { PreviewAgenda() }

@Preview(name = "Vazio", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun AgendaVaziaPreview() { PreviewAgenda(AgendaState.Disponivel(emptyList())) }

@Preview(name = "Erro", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun AgendaErroPreview() { PreviewAgenda(AgendaState.Erro) }

@Preview(name = "Carregando", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1f)
@Composable
private fun AgendaCarregandoPreview() { PreviewAgenda(AgendaState.Carregando) }

@Composable
private fun PreviewAgenda(estado: AgendaState? = null) {
    val dia = LocalDate.of(2026, 10, 7)
    AgendaPsiTheme {
        AgendaScreen(dia, profissionaisDemo, null,
            estado ?: AgendaState.Disponivel(atendimentosDoDia(atendimentosDemo(dia), dia, null)),
            {}, {}, {}, {}, { _, _ -> }, {}, {})
    }
}
