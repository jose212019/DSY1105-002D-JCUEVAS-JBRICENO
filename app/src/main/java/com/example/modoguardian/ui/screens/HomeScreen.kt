@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.example.modoguardian.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.components.EncabezadoHome
import com.example.modoguardian.ui.components.ModoGuardianHero
import com.example.modoguardian.ui.components.ResumenEventos
import com.example.modoguardian.ui.components.TarjetaEventos
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian.viewmodel.EstadoViewModel
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel

// Home conectado a los ViewModels: observa el rol, el Modo Guardián y los eventos de la API
@Composable
fun HomeScreen(
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    estadoViewModel: EstadoViewModel = viewModel()
) {
    val estadoLogin by loginViewModel.estado.collectAsState()
    val estadoActivo by estadoViewModel.activo.collectAsState()
    val mostrarMensaje by estadoViewModel.mostrarMensaje.collectAsState()
    val eventos by mainViewModel.events.collectAsState()
    val cargandoEventos by mainViewModel.cargandoEventos.collectAsState()
    val errorEventos by mainViewModel.errorEventos.collectAsState()
    val mensaje by mainViewModel.mensaje.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mainViewModel.limpiarMensaje()
        }
    }

    val rol = estadoLogin.rol

    if (rol == null) {
        LaunchedEffect(Unit) {
            mainViewModel.navigateTo(Screen.Login, limpiarPila = true)
        }
    } else {
        HomeContenido(
            rol = rol,
            email = estadoLogin.email,
            eventos = eventos,
            estadoActivo = estadoActivo,
            mostrarMensaje = mostrarMensaje,
            widthSizeClass = windowSizeClass.widthSizeClass,
            onAlternarEstado = estadoViewModel::alternarEstado,
            onCerrarSesion = loginViewModel::cerrarSesion,
            onIrAPosts = { mainViewModel.navigateTo(Screen.Posts) },
            cargandoEventos = cargandoEventos,
            errorEventos = errorEventos,
            onReintentarEventos = mainViewModel::cargarEventos,
            onCrearEvento = mainViewModel::crearEvento,
            onEliminarEvento = { mainViewModel.eliminarEvento(it.id) },
            onAlternarResuelto = mainViewModel::alternarResuelto
        )
    }
}

// Contenido sin ViewModel: Compact usa una columna, Medium y Expanded usan dos columnas
@Composable
fun HomeContenido(
    rol: Rol,
    email: String,
    eventos: List<SecurityEvent>,
    estadoActivo: Boolean?,
    mostrarMensaje: Boolean,
    widthSizeClass: WindowWidthSizeClass,
    onAlternarEstado: () -> Unit,
    onCerrarSesion: () -> Unit,
    onIrAPosts: () -> Unit,
    cargandoEventos: Boolean = false,
    errorEventos: String? = null,
    onReintentarEventos: () -> Unit = {},
    onCrearEvento: (String, String) -> Unit = { _, _ -> },
    onEliminarEvento: (SecurityEvent) -> Unit = {},
    onAlternarResuelto: (SecurityEvent) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        val modifierBase = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)

        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            Column(
                modifier = modifierBase.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EncabezadoHome(
                    rol = rol,
                    email = email,
                    onIrAPosts = onIrAPosts,
                    onCerrarSesion = onCerrarSesion
                )
                if (rol.puedeCambiarModo) {
                    ModoGuardianHero(
                        estadoActivo = estadoActivo,
                        mostrarMensaje = mostrarMensaje,
                        onAlternarEstado = onAlternarEstado
                    )
                }
                ResumenEventos(eventos = eventos)
                TarjetaEventos(
                    eventos = eventos,
                    cargando = cargandoEventos,
                    error = errorEventos,
                    onReintentar = onReintentarEventos,
                    puedeGestionar = rol.puedeGestionarEventos,
                    onCrear = onCrearEvento,
                    onEliminar = onEliminarEvento,
                    onAlternarResuelto = onAlternarResuelto
                )
            }
        } else {
            Row(
                modifier = modifierBase,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EncabezadoHome(
                        rol = rol,
                        email = email,
                        onIrAPosts = onIrAPosts,
                        onCerrarSesion = onCerrarSesion
                    )
                    if (rol.puedeCambiarModo) {
                        ModoGuardianHero(
                            estadoActivo = estadoActivo,
                            mostrarMensaje = mostrarMensaje,
                            onAlternarEstado = onAlternarEstado
                        )
                    }
                    ResumenEventos(eventos = eventos)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    TarjetaEventos(
                        eventos = eventos,
                        cargando = cargandoEventos,
                        error = errorEventos,
                        onReintentar = onReintentarEventos,
                        puedeGestionar = rol.puedeGestionarEventos,
                        onCrear = onCrearEvento,
                        onEliminar = onEliminarEvento,
                        onAlternarResuelto = onAlternarResuelto
                    )
                }
            }
        }
    }
}

private val eventosDePrueba = listOf(
    SecurityEvent(id = "1", title = "Movimiento detectado", description = "Actividad en el perímetro norte."),
    SecurityEvent(id = "2", title = "Puerta abierta", description = "Almacén central, fuera de horario.", isResolved = true)
)

@Preview(name = "Home Compact - Admin", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun HomeCompactPreview() {
    ModoGuardianTheme {
        HomeContenido(
            rol = Rol.ADMIN,
            email = "admin@guardian.test",
            eventos = eventosDePrueba,
            estadoActivo = true,
            mostrarMensaje = false,
            widthSizeClass = WindowWidthSizeClass.Compact,
            onAlternarEstado = {},
            onCerrarSesion = {},
            onIrAPosts = {}
        )
    }
}

@Preview(name = "Home Expanded - Operador", widthDp = 1100, heightDp = 800, showBackground = true)
@Composable
private fun HomeExpandedPreview() {
    ModoGuardianTheme {
        HomeContenido(
            rol = Rol.OPERADOR,
            email = "operador@guardian.test",
            eventos = eventosDePrueba,
            estadoActivo = false,
            mostrarMensaje = false,
            widthSizeClass = WindowWidthSizeClass.Expanded,
            onAlternarEstado = {},
            onCerrarSesion = {},
            onIrAPosts = {}
        )
    }
}