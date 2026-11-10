package com.example.modoguardian.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.modoguardian.navigation.Screen

// Barra de navegación inferior con las cinco pestañas principales de la app
private data class DestinoBarra(val pantalla: Screen, val etiqueta: String, val icono: ImageVector)

private val destinos = listOf(
    DestinoBarra(Screen.Home, "Inicio", Icons.Filled.Home),
    DestinoBarra(Screen.Cameras, "Cámaras", Icons.Filled.Videocam),
    DestinoBarra(Screen.Events, "Eventos", Icons.Filled.Checklist),
    DestinoBarra(Screen.Alerts, "Alertas", Icons.Filled.Notifications),
    DestinoBarra(Screen.Profile, "Perfil", Icons.Filled.Person)
)

@Composable
fun BarraNavegacion(rutaActual: String?, onSeleccionar: (Screen) -> Unit) {
    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.pantalla.route,
                onClick = { onSeleccionar(destino.pantalla) },
                icon = { Icon(imageVector = destino.icono, contentDescription = destino.etiqueta) },
                label = { Text(text = destino.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}