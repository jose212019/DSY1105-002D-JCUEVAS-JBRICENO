package com.example.modoguardian.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.EstadoCamara
import com.example.modoguardian.ui.theme.BordeSutil
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.CamarasViewModel
import com.example.modoguardian.viewmodel.MainViewModel

@Composable
fun CamarasScreen(
    camarasViewModel: CamarasViewModel,
    mainViewModel: MainViewModel,
    onAbrirCamara: (String) -> Unit
) {
    val camaras by camarasViewModel.camaras.collectAsState()
    val cargando by camarasViewModel.cargando.collectAsState()
    val error by camarasViewModel.error.collectAsState()
    val eventos by mainViewModel.events.collectAsState()
    val pendientesPorCamara = eventos.filter { !it.isResolved }.groupingBy { it.camara }.eachCount()

    CamarasContenido(
        camaras = camaras,
        pendientesPorCamara = pendientesPorCamara,
        cargando = cargando,
        error = error,
        onReintentar = camarasViewModel::cargarCamaras,
        onAbrirCamara = onAbrirCamara
    )
}

@Composable
fun CamarasContenido(
    camaras: List<Camara>,
    pendientesPorCamara: Map<String, Int>,
    cargando: Boolean,
    error: String?,
    onReintentar: () -> Unit,
    onAbrirCamara: (String) -> Unit
) {
    val enLinea = camaras.count { it.estado == EstadoCamara.EN_LINEA }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Cámaras", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "$enLinea de ${camaras.size} en línea",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            text = "Monitoreo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            when {
                cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onReintentar) {
                            Text(text = "Reintentar")
                        }
                    }
                }

                camaras.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay cámaras registradas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = camaras, key = { it.id }) { camara ->
                            CamaraCard(
                                camara = camara,
                                pendientes = pendientesPorCamara[camara.codigo] ?: 0,
                                onClick = { onAbrirCamara(camara.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun colorEstadoCamara(estado: EstadoCamara): Color {
    return when (estado) {
        EstadoCamara.EN_LINEA -> MaterialTheme.colorScheme.primary
        EstadoCamara.MANTENCION -> MaterialTheme.colorScheme.secondary
        EstadoCamara.SIN_CONEXION -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun CamaraCard(
    camara: Camara,
    pendientes: Int,
    onClick: () -> Unit
) {
    val activa = camara.estado == EstadoCamara.EN_LINEA
    val colorEstado = colorEstadoCamara(camara.estado)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (activa) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else BordeSutil
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (activa) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (activa) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            } else {
                                BordeSutil
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activa) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                        contentDescription = null,
                        tint = if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (camara.grabando) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "REC",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (pendientes > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = pendientes.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, BordeSutil),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = camara.codigo,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = camara.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(colorEstado, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = camara.estado.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorEstado
                    )
                }
                Text(
                    text = camara.zona,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(name = "Cámaras", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun CamarasPreview() {
    ModoGuardianTheme {
        CamarasContenido(
            camaras = listOf(
                Camara("1", "Entrada Principal", "Zona Sur", EstadoCamara.EN_LINEA, true),
                Camara("3", "Perímetro Norte", "Zona Norte", EstadoCamara.EN_LINEA, true),
                Camara("6", "Perímetro Este", "Zona Este", EstadoCamara.MANTENCION, false),
                Camara("7", "Bodega A", "Zona Sur", EstadoCamara.SIN_CONEXION, false)
            ),
            pendientesPorCamara = mapOf("CAM-03" to 1, "CAM-06" to 1),
            cargando = false,
            error = null,
            onReintentar = {},
            onAbrirCamara = {}
        )
    }
}
