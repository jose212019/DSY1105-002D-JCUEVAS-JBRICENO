@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.MainViewModel

// Pestaña de Alertas conectada al ViewModel: muestra solo los eventos de severidad alta que siguen pendientes
@Composable
fun AlertasScreen(
    mainViewModel: MainViewModel,
    onAbrirAlerta: (String) -> Unit
) {
    val eventos by mainViewModel.events.collectAsState()
    val cargando by mainViewModel.cargandoEventos.collectAsState()
    val error by mainViewModel.errorEventos.collectAsState()
    val criticas = eventos.filter { it.severidad == Severidad.ALTA && !it.isResolved }

    AlertasContenido(
        criticas = criticas,
        cargando = cargando,
        error = error,
        onReintentar = mainViewModel::cargarEventos,
        onAbrirAlerta = onAbrirAlerta
    )
}

@Composable
fun AlertasContenido(
    criticas: List<SecurityEvent>,
    cargando: Boolean,
    error: String?,
    onReintentar: () -> Unit,
    onAbrirAlerta: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Alertas", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = if (criticas.isEmpty()) "Todo en orden" else "${criticas.size} críticas pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (criticas.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
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

                criticas.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Sin alertas críticas pendientes", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Cuando entre un evento de severidad alta, aparecerá aquí.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items = criticas, key = { it.id }) { alerta ->
                            AlertaItem(alerta = alerta, onClick = { onAbrirAlerta(alerta.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertaItem(
    alerta: SecurityEvent,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(MaterialTheme.colorScheme.error)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = alerta.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = alerta.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (alerta.camara.isNotBlank()) {
                        Text(
                            text = alerta.camara,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Alertas", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AlertasPreview() {
    ModoGuardianTheme {
        AlertasContenido(
            criticas = listOf(
                SecurityEvent(id = "1", title = "Movimiento no autorizado", description = "Perímetro norte: el sensor PIR-04 detectó presencia fuera del horario.", severidad = Severidad.ALTA, camara = "CAM-03"),
                SecurityEvent(id = "9", title = "Alerta preventiva de humo", description = "Sala de servidores: el detector activó una alerta.", severidad = Severidad.ALTA, camara = "CAM-08")
            ),
            cargando = false,
            error = null,
            onReintentar = {},
            onAbrirAlerta = {}
        )
    }
}