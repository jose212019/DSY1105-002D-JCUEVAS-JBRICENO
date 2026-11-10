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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent

// Componentes del Inicio: encabezado con saludo, botón circular del Modo Guardián y contadores de eventos
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
                .size(48.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp)
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
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Rol: ${rol.etiqueta}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onIrAPosts) {
            Icon(imageVector = Icons.Filled.Description, contentDescription = "Posts")
        }
        IconButton(onClick = onCerrarSesion) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
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

    val colorBoton by animateColorAsState(
        targetValue = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 500),
        label = "colorBotonModo"
    )
    val colorIcono by animateColorAsState(
        targetValue = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 500),
        label = "colorIconoModo"
    )

    val transicion = rememberInfiniteTransition(label = "pulsoModo")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "escalaModo"
    )
    val opacidad by transicion.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "opacidadModo"
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MODO GUARDIÁN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (estadoActivo == null) {
                CircularProgressIndicator()
                Text(text = "Cargando estado...")
            } else {
                Box(
                    modifier = Modifier.size(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                scaleX = escala
                                scaleY = escala
                                alpha = if (activo) opacidad else 0f
                            }
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Surface(
                        onClick = onAlternarEstado,
                        shape = CircleShape,
                        color = colorBoton,
                        border = if (activo) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.size(150.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PowerSettingsNew,
                                contentDescription = if (activo) "Desactivar Modo Guardián" else "Activar Modo Guardián",
                                tint = colorIcono,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (activo) "Estado: ACTIVADO" else "Estado: DESACTIVADO",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
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