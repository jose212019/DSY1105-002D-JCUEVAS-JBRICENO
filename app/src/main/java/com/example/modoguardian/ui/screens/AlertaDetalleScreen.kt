@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.modoguardian.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel
import kotlinx.coroutines.delay

private const val NUMERO_CENTRAL = "133"
private const val SEGUNDOS_RESPUESTA = 120

// Detalle de una alerta conectado a los ViewModels: confirma la alerta y abre el marcador para llamar a Central
@Composable
fun AlertaDetalleScreen(
    idAlerta: String?,
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    onVolver: () -> Unit
) {
    val estadoLogin by loginViewModel.estado.collectAsState()
    val eventos by mainViewModel.events.collectAsState()
    val mensaje by mainViewModel.mensaje.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mainViewModel.limpiarMensaje()
        }
    }

    val evento = eventos.firstOrNull { it.id == idAlerta }

    AlertaDetalleContenido(
        evento = evento,
        puedeGestionar = estadoLogin.rol?.puedeGestionarEventos == true,
        onVolver = onVolver,
        onConfirmar = { evento?.let { mainViewModel.alternarResuelto(it) } },
        onLlamarCentral = {
            try {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$NUMERO_CENTRAL")))
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@Composable
fun AlertaDetalleContenido(
    evento: SecurityEvent?,
    puedeGestionar: Boolean,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit,
    onLlamarCentral: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var segundos by remember { mutableIntStateOf(SEGUNDOS_RESPUESTA) }
    val resuelta = evento?.isResolved == true

    LaunchedEffect(resuelta) {
        if (!resuelta) {
            segundos = SEGUNDOS_RESPUESTA
            while (segundos > 0) {
                delay(1000)
                segundos -= 1
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Text(
                    text = "Alerta crítica",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = if (resuelta) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (resuelta) "RESUELTA" else "CRÍTICO",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (resuelta) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (evento == null) {
                Text(
                    text = "No se encontró esta alerta. Puede que ya haya sido eliminada.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (resuelta) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = evento.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (resuelta) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = evento.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (resuelta) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                        )
                        if (evento.camara.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (resuelta) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = evento.camara,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (resuelta) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "DETALLES DEL EVENTO",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilaDetalle(etiqueta = "Código", valor = "EV-${evento.id}")
                        FilaDetalle(
                            etiqueta = "Severidad",
                            valor = evento.severidad.etiqueta,
                            colorValor = if (evento.severidad == Severidad.ALTA) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        FilaDetalle(
                            etiqueta = "Estado",
                            valor = if (resuelta) "Resuelta" else "Pendiente",
                            colorValor = if (resuelta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        FilaDetalle(etiqueta = "Cámara", valor = evento.camara.ifBlank { "Sin cámara asignada" })
                    }
                }

                Text(
                    text = "TIEMPO DE RESPUESTA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (resuelta) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(72.dp)
                            )
                            Text(text = "Alerta resuelta", style = MaterialTheme.typography.titleMedium)
                        } else {
                            Box(
                                modifier = Modifier.size(132.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { segundos / SEGUNDOS_RESPUESTA.toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 8.dp,
                                    color = if (segundos > 30) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d:%02d", segundos / 60, segundos % 60),
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                            Text(
                                text = if (segundos > 0) "Confirma la alerta antes de que termine el tiempo" else "Tiempo agotado: requiere atención inmediata",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (segundos > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = "ACCIONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (puedeGestionar) {
                    if (resuelta) {
                        OutlinedButton(
                            onClick = onConfirmar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Reabrir alerta")
                        }
                    } else {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirmar()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Confirmar alerta")
                        }
                    }
                } else {
                    Text(
                        text = "Solo lectura: tu rol no puede confirmar alertas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onLlamarCentral,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Llamar a Central")
                }
            }
        }
    }
}

@Composable
private fun FilaDetalle(
    etiqueta: String,
    valor: String,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            color = colorValor
        )
    }
}

@Preview(name = "Detalle de alerta", widthDp = 360, heightDp = 900, showBackground = true)
@Composable
private fun AlertaDetallePreview() {
    ModoGuardianTheme {
        AlertaDetalleContenido(
            evento = SecurityEvent(
                id = "1",
                title = "Movimiento no autorizado",
                description = "Perímetro norte: el sensor PIR-04 detectó presencia fuera del horario permitido.",
                severidad = Severidad.ALTA,
                camara = "CAM-03"
            ),
            puedeGestionar = true,
            onVolver = {},
            onConfirmar = {},
            onLlamarCentral = {}
        )
    }
}