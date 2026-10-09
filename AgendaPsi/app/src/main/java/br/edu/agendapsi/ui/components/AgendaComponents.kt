package br.edu.agendapsi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.agendapsi.ui.inicio.AtendimentoInicio
import br.edu.agendapsi.ui.inicio.InicioIcons
import br.edu.agendapsi.ui.inicio.Profissional
import br.edu.agendapsi.ui.inicio.StatusAgendamento
import java.time.format.DateTimeFormatter

private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun MarcaAgendaPsi() {
    Text("AgendaPsi", color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
}

enum class DestinoPrincipal { INICIO, AGENDA, PACIENTES }

@Composable
fun BarraPrincipal(
    selecionado: DestinoPrincipal, onInicio: () -> Unit,
    onAgenda: () -> Unit, onPacientes: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(selected = selecionado == DestinoPrincipal.INICIO, onClick = onInicio,
            icon = { Icon(InicioIcons.Home, null) }, label = { Text("Início") })
        NavigationBarItem(selected = selecionado == DestinoPrincipal.AGENDA, onClick = onAgenda,
            icon = { Icon(InicioIcons.Calendar, null) }, label = { Text("Agenda") })
        NavigationBarItem(selected = selecionado == DestinoPrincipal.PACIENTES, onClick = onPacientes,
            icon = { Icon(InicioIcons.People, null) }, label = { Text("Pacientes") })
    }
}

@Composable
fun FiltroProfissional(profissionais: List<Profissional>, selecionado: Long?, onSelect: (Long?) -> Unit) {
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
fun AtendimentoCard(atendimento: AtendimentoInicio, onClick: () -> Unit) {
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
