package br.edu.agendapsi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Paleta fixa da especificação visual do projeto, sem cores dinâmicas.
private val Cores = lightColorScheme(
    primary = Color(0xFF1E3A8A), onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEFEF), onPrimaryContainer = Color(0xFF0F2942),
    secondary = Color(0xFF0F766E), onSecondary = Color.White,
    background = Color(0xFFF8FAFC), onBackground = Color(0xFF0F172A),
    surface = Color.White, onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFB91C1C), onError = Color.White
)

@Composable
fun AgendaPsiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Cores,
        shapes = Shapes(small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(12.dp)),
        typography = Typography(), content = content
    )
}
