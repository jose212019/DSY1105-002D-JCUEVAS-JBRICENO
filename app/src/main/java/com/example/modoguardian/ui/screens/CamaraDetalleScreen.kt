package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.EstadoCamara
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.CamarasViewModel
import com.example.modoguardian.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Detalle de una cámara conectado a los ViewModels: vista simulada, datos del dispositivo y eventos de esa cámara
@Composable
fun CamaraDetalleScreen(
    idCamara: String?,
    camarasViewModel: CamarasViewModel,
    mainViewModel: MainViewModel,
    onVolver: () -> Unit
) {
    val camaras by camarasViewModel.camaras.collectAsState()
    val eventos by mainViewModel.events.collectAsState()

    val camara = camaras.firstOrNull { it.id == idCamara }
    val eventosCamara = camara?.let { c -> eventos.filter { it.camara == c.codigo } }.orEmpty()

    CamaraDetalleContenido(
        camara = camara,
        eventosCamara = eventosCamara,
        onVolver = onVolver
    )
}

@Composable
fun CamaraDetalleContenido(
    camara: Camara?,
    eventosCamara: List<SecurityEvent>,
    onVolver: () -> Unit
) {
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val formato = remember { SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            ahora = System.currentTimeMillis()
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
                    text = camara?.nombre ?: "Cámara",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (camara != null) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = camara.estado.etiqueta,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorEstadoCamara(camara.estado),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (camara == null) {
                Text(
                    text = "No se encontró esta cámara.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val activa = camara.estado == EstadoCamara.EN_LINEA
                val verde = Color(0xFF2DE0B0)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            Brush.verticalGradient(
                                if (activa) listOf(Color(0xFF0A1F16), Color(0xFF133A29))
                                else listOf(Color(0xFF1A1F2B), Color(0xFF0E121A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (activa) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = null,
                            tint = verde.copy(alpha = 0.35f),
                            modifier = Modifier.size(56.dp)
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = camara.codigo,
                                style = MaterialTheme.typography.labelMedium,
                                color = verde
                            )
                            Text(
                                text = formato.format(Date(ahora)),
                                style = MaterialTheme.typography.labelMedium,
                                color = verde
                            )
                        }
                        if (camara.grabando) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFFF6B6B), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REC",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                        }
                        Text(
                            text = "EN VIVO · VISTA SIMULADA",
                            style = MaterialTheme.typography.labelMedium,
                            color = verde,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.VideocamOff,
                                contentDescription = null,
                                tint = Color(0xFF9AA8C4),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SIN SEÑAL",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF9AA8C4)
                            )
                        }
                    }
                }

                Text(
                    text = "ESTADO DEL DISPOSITIVO",
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
                        FilaDato(etiqueta = "Código", valor = camara.codigo)
                        FilaDato(etiqueta = "Zona", valor = camara.zona.ifBlank { "Sin zona" })
                        FilaDato(
                            etiqueta = "Estado",
                            valor = camara.estado.etiqueta,
                            colorValor = colorEstadoCamara(camara.estado)
                        )
                        FilaDato(
                            etiqueta = "Grabación",
                            valor = if (camara.grabando) "Activa" else "Detenida"
                        )
                    }
                }

                Text(
                    text = "EVENTOS DE ESTA CÁMARA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (eventosCamara.isEmpty()) {
                    Text(
                        text = "Sin eventos registrados para esta cámara.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    eventosCamara.forEach { evento ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(5.dp)
                                        .background(colorDeSeveridad(evento.severidad))
                                )
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = evento.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = evento.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (evento.isResolved) "Resuelto" else "Pendiente",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (evento.isResolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun colorDeSeveridad(severidad: Severidad): Color {
    return when (severidad) {
        Severidad.ALTA -> MaterialTheme.colorScheme.error
        Severidad.MEDIA -> MaterialTheme.colorScheme.secondary
        Severidad.BAJA -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun FilaDato(
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

@Preview(name = "Detalle de cámara", widthDp = 360, heightDp = 900, showBackground = true)
@Composable
private fun CamaraDetallePreview() {
    ModoGuardianTheme {
        CamaraDetalleContenido(
            camara = Camara("3", "Perímetro Norte", "Zona Norte", EstadoCamara.EN_LINEA, true),
            eventosCamara = listOf(
                SecurityEvent(id = "1", title = "Movimiento no autorizado", description = "El sensor PIR-04 detectó presencia fuera del horario.", severidad = Severidad.ALTA, camara = "CAM-03")
            ),
            onVolver = {}
        )
    }
}