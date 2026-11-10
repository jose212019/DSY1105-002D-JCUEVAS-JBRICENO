@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.components.TarjetaEventos
import com.example.modoguardian.ui.components.TarjetaModoGuardian
import com.example.modoguardian.ui.components.TarjetaRol
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian.viewmodel.EstadoViewModel
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel

// Home conectado a los ViewModels: observa el rol del login y permite ir a Perfil
@Composable
fun HomeScreen(
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    estadoViewModel: EstadoViewModel = viewModel()
) {
    val estadoLogin by loginViewModel.estado.collectAsState()
    val estadoActivo by estadoViewModel.activo.collectAsState()
    val mostrarMensaje by estadoViewModel.mostrarMensaje.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()
    val eventos by mainViewModel.events.collectAsState()
    val cargandoEventos by mainViewModel.cargandoEventos.collectAsState()
    val errorEventos by mainViewModel.errorEventos.collectAsState()
    val mensaje by mainViewModel.mensaje.collectAsState()
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
            cargandoEventos = cargandoEventos,
            errorEventos = errorEventos,
            onReintentarEventos = mainViewModel::cargarEventos,
            
            onCrearEvento = mainViewModel::crearEvento,
            onEliminarEvento = { mainViewModel.eliminarEvento(it.id) },
            onAlternarResuelto = mainViewModel::alternarResuelto,
            estadoActivo = estadoActivo,
            mostrarMensaje = mostrarMensaje,
            widthSizeClass = windowSizeClass.widthSizeClass,
            onAlternarEstado = estadoViewModel::alternarEstado,
            onCerrarSesion = loginViewModel::cerrarSesion,
            onIrAPerfil = { mainViewModel.navigateTo(Screen.Profile) },
            onIrAPosts = { mainViewModel.navigateTo(Screen.Posts) }
        )
    }
}

// Contenido sin ViewModel: Compact usa Column, Medium/Expanded usa Row de dos columnas
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
    onIrAPerfil: () -> Unit,
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
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(text = "Modo Guardián") },
                actions = {
                    TextButton(onClick = onIrAPosts) {
                        Text(text = "Posts")
                    }
                    TextButton(onClick = onIrAPerfil) {
                        Text(text = "Perfil")
                    }
                    TextButton(onClick = onCerrarSesion) {
                        Text(text = "Cerrar sesión")
                    }
                }
            )
        }
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
                TarjetaRol(rol = rol, email = email)
                if (rol.puedeCambiarModo) {
                    TarjetaModoGuardian(
                        estadoActivo = estadoActivo,
                        mostrarMensaje = mostrarMensaje,
                        onAlternarEstado = onAlternarEstado
                    )
                }
                TarjetaEventos(eventos = eventos, cargando = cargandoEventos, error = errorEventos, onReintentar = onReintentarEventos, puedeGestionar = rol.puedeGestionarEventos, onCrear = onCrearEvento, onEliminar = onEliminarEvento, onAlternarResuelto = onAlternarResuelto)
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
                    TarjetaRol(rol = rol, email = email)
                    if (rol.puedeCambiarModo) {
                        TarjetaModoGuardian(
                            estadoActivo = estadoActivo,
                            mostrarMensaje = mostrarMensaje,
                            onAlternarEstado = onAlternarEstado
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    TarjetaEventos(eventos = eventos, cargando = cargandoEventos, error = errorEventos, onReintentar = onReintentarEventos, puedeGestionar = rol.puedeGestionarEventos, onCrear = onCrearEvento, onEliminar = onEliminarEvento, onAlternarResuelto = onAlternarResuelto)
                }
            }
        }
    }
}

private val eventosPreview = listOf(
    SecurityEvent(id = "1", title = "Movimiento detectado", description = "Actividad en el perímetro."),
    SecurityEvent(id = "2", title = "Estado del sistema", description = "Todos los módulos operando.")
)

@Preview(name = "Home Compact - Supervisor", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun HomeCompactPreview() {
    ModoGuardianTheme {
        HomeContenido(
            rol = Rol.SUPERVISOR,
            email = "supervisor@guardian.test",
            eventos = eventosPreview,
            estadoActivo = true,
            mostrarMensaje = false,
            widthSizeClass = WindowWidthSizeClass.Compact,
            onAlternarEstado = {},
            onCerrarSesion = {},
            onIrAPerfil = {},
            onIrAPosts = {}
        )
    }
}

@Preview(name = "Home Expanded - Admin", widthDp = 1100, heightDp = 800, showBackground = true)
@Composable
private fun HomeExpandedPreview() {
    ModoGuardianTheme {
        HomeContenido(
            rol = Rol.ADMIN,
            email = "admin@guardian.test",
            eventos = eventosPreview,
            estadoActivo = false,
            mostrarMensaje = false,
            widthSizeClass = WindowWidthSizeClass.Expanded,
            onAlternarEstado = {},
            onCerrarSesion = {},
            onIrAPerfil = {},
            onIrAPosts = {}
        )
    }
}