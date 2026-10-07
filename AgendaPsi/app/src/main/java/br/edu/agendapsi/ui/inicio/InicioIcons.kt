package br.edu.agendapsi.ui.inicio

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object InicioIcons {
    val Home = ImageVector.Builder("Inicio", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 10f); lineTo(12f, 2f); lineTo(21f, 10f); lineTo(19f, 10f)
            lineTo(19f, 21f); lineTo(14f, 21f); lineTo(14f, 14f); lineTo(10f, 14f)
            lineTo(10f, 21f); lineTo(5f, 21f); lineTo(5f, 10f); close()
        }
    }.build()
    val Calendar = ImageVector.Builder("Agenda", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
            moveTo(3f, 4f); lineTo(21f, 4f); lineTo(21f, 22f); lineTo(3f, 22f); close()
            moveTo(5f, 9f); lineTo(19f, 9f); lineTo(19f, 20f); lineTo(5f, 20f); close()
            moveTo(6f, 2f); lineTo(8f, 2f); lineTo(8f, 4f); lineTo(6f, 4f); close()
            moveTo(16f, 2f); lineTo(18f, 2f); lineTo(18f, 4f); lineTo(16f, 4f); close()
        }
    }.build()
    val People = ImageVector.Builder("Pacientes", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 3f); curveTo(3f, 3f, 3f, 12f, 9f, 12f); curveTo(15f, 12f, 15f, 3f, 9f, 3f); close()
            moveTo(2f, 21f); lineTo(2f, 18f); curveTo(2f, 12f, 16f, 12f, 16f, 18f); lineTo(16f, 21f); close()
            moveTo(16f, 4f); curveTo(22f, 4f, 22f, 12f, 16f, 12f); lineTo(16f, 10f)
            curveTo(19f, 10f, 19f, 6f, 16f, 6f); close()
            moveTo(18f, 14f); curveTo(22f, 14f, 23f, 17f, 23f, 21f); lineTo(19f, 21f)
            lineTo(19f, 18f); close()
        }
    }.build()
    val Down = ImageVector.Builder("Expandir", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(7f, 9f); lineTo(12f, 14f); lineTo(17f, 9f); close()
        }
    }.build()
}
