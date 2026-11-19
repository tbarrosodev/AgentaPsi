package br.edu.agendapsi.ui.avisos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.agendapsi.AgendaPsiApplication
import br.edu.agendapsi.data.local.AvisoEntity
import br.edu.agendapsi.data.local.avisosDeExemplo
import br.edu.agendapsi.ui.components.MarcaAgendaPsi
import br.edu.agendapsi.ui.theme.AgendaPsiTheme

@Composable
fun AvisosScreen(onVoltar: () -> Unit) {
    val context = LocalContext.current

    val avisos = remember { mutableStateListOf<AvisoEntity>() }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val app = context.applicationContext as AgendaPsiApplication
            val dao = app.container.db.avisoDao()

            if (dao.contar() == 0) {
                dao.inserirTodos(avisosDeExemplo)
            }

            avisos.addAll(dao.listarTodos())
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
            Text("Avisos da clínica", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = onVoltar, shape = MaterialTheme.shapes.small) {
                Text("Voltar")
            }

            when {
                carregando -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Carregando avisos…")
                }
                erro -> Text("Não foi possível carregar os avisos.")
                avisos.isEmpty() -> Text("Nenhum aviso no momento.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(avisos) { aviso ->
                        AvisoCard(aviso)
                    }
                }
            }
        }
    }
}

@Composable
fun AvisoCard(aviso: AvisoEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                aviso.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                aviso.data,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(aviso.mensagem, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvisoCardPreview() {
    AgendaPsiTheme {
        AvisoCard(avisosDeExemplo.first())
    }
}
