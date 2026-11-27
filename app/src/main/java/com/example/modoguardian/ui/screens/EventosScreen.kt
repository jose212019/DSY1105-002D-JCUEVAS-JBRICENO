@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.modoguardian.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.ui.theme.BordeSutil
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel

private enum class FiltroEventos(val etiqueta: String) {
    TODOS("Todos"),
    PENDIENTES("Pendientes"),
    RESUELTOS("Resueltos"),
    ALTA("Alta")
}

@Composable
fun EventosScreen(
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel
) {
    val estadoLogin by loginViewModel.estado.collectAsState()
    val eventos by mainViewModel.events.collectAsState()
    val cargando by mainViewModel.cargandoEventos.collectAsState()
    val error by mainViewModel.errorEventos.collectAsState()
    val mensaje by mainViewModel.mensaje.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mainViewModel.limpiarMensaje()
        }
    }

    EventosContenido(
        eventos = eventos,
        cargando = cargando,
        error = error,
        puedeGestionar = estadoLogin.rol?.puedeGestionarEventos == true,
        onReintentar = mainViewModel::cargarEventos,
        onCrear = mainViewModel::crearEvento,
        onEliminar = { mainViewModel.eliminarEvento(it.id) },
        onAlternarResuelto = mainViewModel::alternarResuelto
    )
}

@Composable
fun EventosContenido(
    eventos: List<SecurityEvent>,
    cargando: Boolean,
    error: String?,
    puedeGestionar: Boolean,
    onReintentar: () -> Unit,
    onCrear: (String, String, Severidad, String) -> Unit,
    onEliminar: (SecurityEvent) -> Unit,
    onAlternarResuelto: (SecurityEvent) -> Unit
) {
    var filtro by rememberSaveable { mutableStateOf(FiltroEventos.TODOS) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val visibles = remember(eventos, filtro, busqueda) {
        eventos
            .filter {
                when (filtro) {
                    FiltroEventos.TODOS -> true
                    FiltroEventos.PENDIENTES -> !it.isResolved
                    FiltroEventos.RESUELTOS -> it.isResolved
                    FiltroEventos.ALTA -> it.severidad == Severidad.ALTA
                }
            }
            .filter {
                busqueda.isBlank() ||
                    it.title.contains(busqueda, ignoreCase = true) ||
                    it.description.contains(busqueda, ignoreCase = true) ||
                    it.camara.contains(busqueda, ignoreCase = true)
            }
            .sortedWith(compareBy<SecurityEvent>({ it.isResolved }, { it.severidad.ordinal }))
    }
    val pendientes = eventos.count { !it.isResolved }

    if (mostrarDialogo) {
        DialogoEvento(
            onConfirmar = { titulo, descripcion, severidad, camara ->
                onCrear(titulo, descripcion, severidad, camara)
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
        floatingActionButton = {
            if (puedeGestionar) {
                ExtendedFloatingActionButton(
                    onClick = { mostrarDialogo = true },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    text = { Text(text = "Nuevo evento", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(text = "Eventos", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "$pendientes pendientes · ${eventos.size} en total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!puedeGestionar) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tu rol es de solo lectura: puedes consultar los eventos, pero no crearlos, resolverlos ni eliminarlos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroEventos.entries.forEach { opcion ->
                        FilterChip(
                            selected = filtro == opcion,
                            onClick = { filtro = opcion },
                            shape = CircleShape,
                            label = { Text(text = opcion.etiqueta) },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filtro == opcion,
                                borderColor = BordeSutil,
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text(text = "Buscar por título, descripción o cámara") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = BordeSutil,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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

                visibles.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, BordeSutil),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.EventBusy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay eventos para este filtro.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = visibles, key = { it.id }) { evento ->
                            EventoItem(
                                evento = evento,
                                puedeGestionar = puedeGestionar,
                                onAlternarResuelto = onAlternarResuelto,
                                onEliminar = onEliminar
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun colorSeveridad(severidad: Severidad): Color {
    return when (severidad) {
        Severidad.ALTA -> MaterialTheme.colorScheme.error
        Severidad.MEDIA -> MaterialTheme.colorScheme.secondary
        Severidad.BAJA -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun EventoItem(
    evento: SecurityEvent,
    puedeGestionar: Boolean,
    onAlternarResuelto: (SecurityEvent) -> Unit,
    onEliminar: (SecurityEvent) -> Unit
) {
    val eventoActual by rememberUpdatedState(evento)
    val alternar by rememberUpdatedState(onAlternarResuelto)
    var confirmarEliminar by remember { mutableStateOf(false) }
    var verDetalle by remember { mutableStateOf(false) }
    val colorSev = colorSeveridad(evento.severidad)

    val estadoDeslizar = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            if (valor == SwipeToDismissBoxValue.StartToEnd) {
                alternar(eventoActual)
            }
            false
        }
    )

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text(text = "¿Eliminar evento?") },
            text = { Text(text = "Se borrará de forma permanente de la API.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    onEliminar(evento)
                }) {
                    Text(text = "Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (verDetalle) {
        AlertDialog(
            onDismissRequest = { verDetalle = false },
            title = { Text(text = evento.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = evento.description)
                    if (evento.camara.isNotBlank()) {
                        Text(
                            text = "Cámara: ${evento.camara}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Severidad: ${evento.severidad.etiqueta}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorSev
                    )
                    Text(
                        text = "Estado: ${if (evento.isResolved) "Resuelto" else "Pendiente"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (puedeGestionar) {
                    TextButton(onClick = {
                        verDetalle = false
                        onAlternarResuelto(evento)
                    }) {
                        Text(text = if (evento.isResolved) "Reabrir" else "Resolver")
                    }
                } else {
                    TextButton(onClick = { verDetalle = false }) {
                        Text(text = "Cerrar")
                    }
                }
            },
            dismissButton = {
                if (puedeGestionar) {
                    TextButton(onClick = { verDetalle = false }) {
                        Text(text = "Cerrar")
                    }
                }
            }
        )
    }

    SwipeToDismissBox(
        state = estadoDeslizar,
        enableDismissFromStartToEnd = puedeGestionar,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (evento.isResolved) Icons.Filled.Replay else Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (evento.isResolved) "Reabrir" else "Resolver",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) {
        Card(
            onClick = { verDetalle = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BordeSutil)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(colorSev)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = evento.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (evento.isResolved) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (evento.isResolved) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Text(
                                text = if (evento.isResolved) "Resuelto" else "Pendiente",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (evento.isResolved) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = evento.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (evento.camara.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, BordeSutil)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Videocam,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = evento.camara,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colorSev.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Severidad ${evento.severidad.etiqueta.lowercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorSev,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (puedeGestionar) {
                            TextButton(
                                onClick = { onAlternarResuelto(evento) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = if (evento.isResolved) "Reabrir" else "Resolver")
                            }
                            IconButton(
                                onClick = { confirmarEliminar = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Eliminar evento",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoEvento(
    onConfirmar: (String, String, Severidad, String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var camara by remember { mutableStateOf("") }
    var severidad by remember { mutableStateOf(Severidad.MEDIA) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(text = "Nuevo evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(text = "Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(text = "Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = camara,
                    onValueChange = { camara = it },
                    label = { Text(text = "Cámara (opcional)") },
                    placeholder = { Text(text = "CAM-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Severidad",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Severidad.entries.forEach { opcion ->
                        FilterChip(
                            selected = severidad == opcion,
                            onClick = { severidad = opcion },
                            label = { Text(text = opcion.etiqueta) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(titulo, descripcion, severidad, camara.trim().uppercase()) },
                enabled = titulo.isNotBlank()
            ) {
                Text(text = "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar")
            }
        }
    )
}

@Preview(name = "Eventos Admin", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun EventosPreview() {
    ModoGuardianTheme {
        EventosContenido(
            eventos = listOf(
                SecurityEvent(id = "1", title = "Movimiento no autorizado", description = "Perímetro norte: el sensor PIR-04 detectó presencia.", severidad = Severidad.ALTA, camara = "CAM-03"),
                SecurityEvent(id = "2", title = "Batería baja en sensor", description = "Perímetro este: solo queda un 12% de carga.", severidad = Severidad.MEDIA, camara = "CAM-06"),
                SecurityEvent(id = "3", title = "Ronda completada", description = "Perímetro sur: ruta 2 terminada.", isResolved = true, severidad = Severidad.BAJA, camara = "CAM-04")
            ),
            cargando = false,
            error = null,
            puedeGestionar = true,
            onReintentar = {},
            onCrear = { _, _, _, _ -> },
            onEliminar = {},
            onAlternarResuelto = {}
        )
    }
}
