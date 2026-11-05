package com.example.modoguardian.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent

// Colores del botón del Modo Guardián (valores ARGB completos de 8 dígitos)
private val ColorActivo = Color(0xFF4CAF50)
private val ColorInactivo = Color(0xFF757575)

// muestra quién inició sesión y qué puede hacer
@Composable
fun TarjetaRol(
    rol: Rol,
    email: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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

    Card(modifier = modifier.fillMaxWidth()) {
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
    onReintentar: () -> Unit = {}
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Eventos de seguridad",
                style = MaterialTheme.typography.titleMedium
            )
            when {
                cargando -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                        if (indice > 0) HorizontalDivider()
                        Text(
                            text = evento.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = evento.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}