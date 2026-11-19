package br.edu.agendapsi.ui.feriados

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.agendapsi.data.remote.Feriado
import br.edu.agendapsi.data.remote.RetrofitHelper
import br.edu.agendapsi.ui.components.MarcaAgendaPsi
import br.edu.agendapsi.ui.theme.AgendaPsiTheme
import java.time.LocalDate

@Composable
fun FeriadosScreen(onVoltar: () -> Unit) {
    val feriados = remember { mutableStateListOf<Feriado>() }
    var ano by remember { mutableIntStateOf(LocalDate.now().year) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf(false) }
    var tentativa by remember { mutableIntStateOf(0) }

    LaunchedEffect(ano, tentativa) {
        carregando = true
        erro = false
        try {
            val resposta = RetrofitHelper().feriadosApi.listarFeriados(ano)
            feriados.clear()
            feriados.addAll(resposta)
        } catch (e: Exception) {
            erro = true
        }
        carregando = false
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MarcaAgendaPsi()
            Text("Feriados nacionais", style = MaterialTheme.typography.titleLarge)
            Text(
                "Fonte: BrasilAPI",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onVoltar, shape = MaterialTheme.shapes.small) {
                Text("Voltar")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { ano-- }, enabled = !carregando) {
                    Text("‹ ${ano - 1}")
                }
                Text("$ano", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = { ano++ }, enabled = !carregando) {
                    Text("${ano + 1} ›")
                }
            }

            when {
                carregando -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Buscando feriados…")
                }
                erro -> {
                    Text("Não foi possível buscar os feriados. Verifique a internet.")
                    Button(onClick = { tentativa++ }, shape = MaterialTheme.shapes.small) {
                        Text("Tentar novamente")
                    }
                }
                feriados.isEmpty() -> Text("Nenhum feriado encontrado para $ano.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(feriados) { feriado ->
                        FeriadoCard(feriado)
                    }
                }
            }
        }
    }
}

@Composable
fun FeriadoCard(feriado: Feriado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                formatarData(feriado.date),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (feriado.weekday != null) {
                Text(
                    feriado.weekday,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(feriado.name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

fun formatarData(dataDaApi: String): String {
    val partes = dataDaApi.split("-")
    if (partes.size != 3) return dataDaApi
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}

@Preview(showBackground = true)
@Composable
private fun FeriadoCardPreview() {
    AgendaPsiTheme {
        FeriadoCard(
            Feriado(
                date = "2026-11-20",
                name = "Dia Nacional de Zumbi e da Consciência Negra",
                weekday = "sexta-feira"
            )
        )
    }
}
