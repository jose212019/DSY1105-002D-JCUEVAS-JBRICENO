package com.example.modoguardian.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.ui.theme.BordeSutil

private val ColorActivo = Color(0xFF2DE0B0)
private val ColorInactivo = Color(0xFF757575)

@Composable
fun TarjetaRol(
    rol: Rol,
    email: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Rol: ${rol.etiqueta}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = rol.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Módulos disponibles:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            rol.modulos.forEach { modulo ->
                Text(
                    text = "• $modulo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun TarjetaModoGuardian(
    estadoActivo: Boolean?,
    mostrarMensaje: Boolean,
    onAlternarEstado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorBoton by animateColorAsState(
        targetValue = if (estadoActivo == true) ColorActivo else ColorInactivo,
        animationSpec = tween(durationMillis = 500),
        label = "colorBotonModo"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BordeSutil)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Modo Guardián",
                style = MaterialTheme.typography.titleMedium
            )

            if (estadoActivo == null) {
                CircularProgressIndicator()
                Text(text = "Cargando estado...")
            } else {
                Text(
                    text = if (estadoActivo) "Estado: ACTIVADO" else "Estado: DESACTIVADO",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onAlternarEstado,
                    colors = ButtonDefaults.buttonColors(containerColor = colorBoton)
                ) {
                    Text(text = if (estadoActivo) "Desactivar" else "Activar")
                }
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
fun TarjetaEventos(
    eventos: List<SecurityEvent>,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    error: String? = null,
    onReintentar: () -> Unit = {},
    puedeGestionar: Boolean = false,
    onCrear: (String, String) -> Unit = { _, _ -> },
    onEliminar: (SecurityEvent) -> Unit = {},
    onAlternarResuelto: (SecurityEvent) -> Unit = {}
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        DialogoNuevoEvento(
            onConfirmar = { titulo, descripcion ->
                onCrear(titulo, descripcion)
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BordeSutil)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Eventos de seguridad",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (puedeGestionar) {
                    FilledTonalButton(
                        onClick = { mostrarDialogo = true },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nuevo evento",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            when {
                cargando -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onReintentar) {
                        Text(text = "Reintentar")
                    }
                }

                else -> {
                    eventos.forEachIndexed { indice, evento ->
                        if (indice > 0) {
                            HorizontalDivider(color = BordeSutil)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (evento.isResolved) "(Resuelto) ${evento.title}" else evento.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (evento.isResolved) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = evento.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (puedeGestionar) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { onAlternarResuelto(evento) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (evento.isResolved) Icons.Filled.Replay else Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = if (evento.isResolved) "Reabrir" else "Marcar resuelto")
                                    }
                                    TextButton(
                                        onClick = { onEliminar(evento) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
