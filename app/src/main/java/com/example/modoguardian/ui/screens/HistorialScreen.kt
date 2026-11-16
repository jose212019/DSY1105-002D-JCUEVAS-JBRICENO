@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Accion
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.TipoAccion
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Historial de acciones conectado al ViewModel: lee de Room quién hizo qué sobre cada evento
@Composable
fun HistorialScreen(
    mainViewModel: MainViewModel,
    onVolver: () -> Unit
) {
    val flujo = remember { mainViewModel.acciones() }
    val acciones by flujo.collectAsState(initial = emptyList())

    HistorialContenido(acciones = acciones, onVolver = onVolver)
}

@Composable
fun HistorialContenido(
    acciones: List<Accion>,
    onVolver: () -> Unit
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
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Column {
                    Text(text = "Historial de acciones", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${acciones.size} registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (acciones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay acciones registradas. Cuando alguien cree, resuelva, reabra o elimine un evento, aparecerá aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = acciones, key = { it.id }) { accion ->
                        AccionItem(accion = accion)
                    }
                }
            }
        }
    }
}

@Composable
private fun iconoDeAccion(tipo: TipoAccion): ImageVector {
    return when (tipo) {
        TipoAccion.CREADO -> Icons.Filled.Add
        TipoAccion.RESUELTO -> Icons.Filled.Check
        TipoAccion.REABIERTO -> Icons.Filled.Replay
        TipoAccion.ELIMINADO -> Icons.Filled.Delete
    }
}

@Composable
private fun colorDeAccion(tipo: TipoAccion): Color {
    return when (tipo) {
        TipoAccion.CREADO -> MaterialTheme.colorScheme.primary
        TipoAccion.RESUELTO -> MaterialTheme.colorScheme.primary
        TipoAccion.REABIERTO -> MaterialTheme.colorScheme.secondary
        TipoAccion.ELIMINADO -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun AccionItem(accion: Accion) {
    val formato = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US) }
    val etiquetaRol = Rol.entries.firstOrNull { it.name == accion.rol }?.etiqueta ?: accion.rol
    val color = colorDeAccion(accion.tipo)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconoDeAccion(accion.tipo),
                        contentDescription = null,
                        tint = color
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${accion.tipo.verbo}: ${accion.eventoTitulo}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (etiquetaRol.isBlank()) accion.usuario else "${accion.usuario} · $etiquetaRol",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formato.format(Date(accion.fecha)),
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
        }
    }
}

@Preview(name = "Historial", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun HistorialPreview() {
    ModoGuardianTheme {
        HistorialContenido(
            acciones = listOf(
                Accion(3, "1", "Movimiento no autorizado", TipoAccion.RESUELTO, "supervisor@guardian.test", "SUPERVISOR", 1763200000000),
                Accion(2, "16", "Intruso en bodega", TipoAccion.CREADO, "admin@guardian.test", "ADMIN", 1763190000000),
                Accion(1, "9", "Alerta preventiva de humo", TipoAccion.ELIMINADO, "admin@guardian.test", "ADMIN", 1763180000000)
            ),
            onVolver = {}
        )
    }
}