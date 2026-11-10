package com.example.modoguardian.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Tema de ModoGuardian: oscuro por defecto, con colores propios en vez del color dinámico del sistema
private val EsquemaOscuro = darkColorScheme(
    primary = VerdeGuardian,
    onPrimary = Color(0xFF00201A),
    primaryContainer = VerdeProfundo,
    onPrimaryContainer = Color(0xFFB9F6E4),
    secondary = Ambar,
    onSecondary = Color(0xFF3A2500),
    secondaryContainer = Color(0xFF4A3410),
    onSecondaryContainer = Color(0xFFFFE0B0),
    tertiary = Color(0xFF7DB8FF),
    onTertiary = Color(0xFF00213F),
    background = Noche,
    onBackground = TextoClaro,
    surface = NocheSuperficie,
    onSurface = TextoClaro,
    surfaceVariant = NocheElevada,
    onSurfaceVariant = TextoSuave,
    surfaceContainerLowest = Noche,
    surfaceContainerLow = NocheSuperficie,
    surfaceContainer = NocheSuperficie,
    surfaceContainerHigh = NocheElevada,
    surfaceContainerHighest = NocheElevada,
    outline = NocheBorde,
    outlineVariant = NocheBorde,
    error = RojoAlerta,
    onError = Color(0xFF3B0A0A),
    errorContainer = Color(0xFF4A1A1A),
    onErrorContainer = Color(0xFFFFD6D6)
)

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF0B8A6E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFF5EA),
    onPrimaryContainer = Color(0xFF00382D),
    secondary = Color(0xFF9A5B00),
    onSecondary = Color.White,
    background = Color(0xFFF3F6FB),
    onBackground = Color(0xFF0B1220),
    surface = Color.White,
    onSurface = Color(0xFF0B1220),
    surfaceVariant = Color(0xFFE3EAF5),
    onSurfaceVariant = Color(0xFF4A5875),
    outline = Color(0xFFB4C0D6),
    error = Color(0xFFD64545)
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ModoGuardianTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}