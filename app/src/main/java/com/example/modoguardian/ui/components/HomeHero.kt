package com.example.modoguardian.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.ui.theme.BordeSutil
import com.example.modoguardian.ui.theme.VerdeBrillo

@Composable
fun EncabezadoHome(
    rol: Rol,
    email: String,
    onIrAPosts: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nombre = email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "Guardián" }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                .padding(3.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.take(1),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, $nombre",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Rol: ${rol.etiqueta}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onIrAPosts) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = "Posts",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onCerrarSesion) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Cerrar sesión",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModoGuardianHero(
    estadoActivo: Boolean?,
    mostrarMensaje: Boolean,
    onAlternarEstado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activo = estadoActivo == true
    val colorPrimario = MaterialTheme.colorScheme.primary

    val colorGlow by animateColorAsState(
        targetValue = if (activo) colorPrimario.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = 600),
        label = "colorGlowModo"
    )
    val colorIcono by animateColorAsState(
        targetValue = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 400),
        label = "colorIconoModo"
    )

    val transicion = rememberInfiniteTransition(label = "pulsoModo")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "escalaModo"
    )
    val opacidad by transicion.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "opacidadModo"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (activo) colorPrimario.copy(alpha = 0.25f) else BordeSutil
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(colorGlow, Color.Transparent),
                        radius = 480f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (activo) colorPrimario.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (activo) colorPrimario.copy(alpha = 0.35f) else BordeSutil
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activo) colorPrimario else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        )
                        Text(
                            text = "MODO GUARDIÁN",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (activo) colorPrimario else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (estadoActivo == null) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                    Text(
                        text = "Cargando estado...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Box(
                        modifier = Modifier.size(204.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Anillo exterior estático translúcido
                        Box(
                            modifier = Modifier
                                .size(192.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activo) colorPrimario.copy(alpha = 0.08f) else Color.Transparent
                                )
                        )
                        // Anillo medio translúcido
                        Box(
                            modifier = Modifier
                                .size(168.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activo) colorPrimario.copy(alpha = 0.16f) else Color.Transparent
                                )
                        )
                        // Pulso animado
                        Box(
                            modifier = Modifier
                                .size(144.dp)
                                .graphicsLayer {
                                    scaleX = escala
                                    scaleY = escala
                                    alpha = if (activo) opacidad else 0f
                                }
                                .border(2.dp, colorPrimario, CircleShape)
                        )
                        // Botón principal con degradado
                        Surface(
                            onClick = onAlternarEstado,
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = if (activo) {
                                BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f))
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            },
                            modifier = Modifier.size(144.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (activo) {
                                            Brush.verticalGradient(
                                                colors = listOf(VerdeBrillo, colorPrimario)
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PowerSettingsNew,
                                    contentDescription = if (activo) "Desactivar Modo Guardián" else "Activar Modo Guardián",
                                    tint = colorIcono,
                                    modifier = Modifier.size(58.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (activo) "Estado: ACTIVADO" else "Estado: DESACTIVADO",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (activo) colorPrimario else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (activo) "Toca el botón para desactivar" else "Toca el botón para activar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (mostrarMensaje) {
                    Text(
                        text = "Estado actualizado",
                        color = colorPrimario,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ResumenEventos(
    eventos: List<SecurityEvent>,
    modifier: Modifier = Modifier
) {
    val pendientes = eventos.count { !it.isResolved }
    val resueltos = eventos.count { it.isResolved }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ContadorEventos(
            etiqueta = "Pendientes",
            valor = pendientes,
            color = if (pendientes > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        ContadorEventos(
            etiqueta = "Resueltos",
            valor = resueltos,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        ContadorEventos(
            etiqueta = "Total",
            valor = eventos.size,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ContadorEventos(
    etiqueta: String,
    valor: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BordeSutil)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
