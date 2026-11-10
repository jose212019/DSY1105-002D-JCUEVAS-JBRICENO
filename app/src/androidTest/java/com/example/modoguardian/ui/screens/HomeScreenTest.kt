package com.example.modoguardian.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Pruebas de UI del Home: permisos por rol, navegación, carga y error de eventos
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val eventos = listOf(
        SecurityEvent(id = "1", title = "Movimiento detectado", description = "Actividad en el perímetro.")
    )

    private fun mostrarHome(
        rol: Rol,
        cargando: Boolean = false,
        error: String? = null,
        onIrAPosts: () -> Unit = {},
        onCerrarSesion: () -> Unit = {},
        onReintentar: () -> Unit = {},
        onCrearEvento: (String, String) -> Unit = { _, _ -> }
    ) {
        composeRule.setContent {
            ModoGuardianTheme {
                HomeContenido(
                    rol = rol,
                    email = "test@guardian.test",
                    eventos = eventos,
                    estadoActivo = false,
                    mostrarMensaje = false,
                    widthSizeClass = WindowWidthSizeClass.Compact,
                    onAlternarEstado = {},
                    onCerrarSesion = onCerrarSesion,
                    
                    onIrAPosts = onIrAPosts,
                    cargandoEventos = cargando,
                    errorEventos = error,
                    onReintentarEventos = onReintentar,
                    onCrearEvento = onCrearEvento
                )
            }
        }
    }

    @Test
    fun admin_ve_los_botones_de_gestion_de_eventos() {
        mostrarHome(Rol.ADMIN)

        composeRule.onNodeWithText("Nuevo evento").assertExists()
        composeRule.onNodeWithText("Marcar resuelto").assertExists()
        composeRule.onNodeWithText("Eliminar").assertExists()
    }

    @Test
    fun supervisor_ve_los_botones_de_gestion_de_eventos() {
        mostrarHome(Rol.SUPERVISOR)

        composeRule.onNodeWithText("Nuevo evento").assertExists()
        composeRule.onNodeWithText("Marcar resuelto").assertExists()
        composeRule.onNodeWithText("Eliminar").assertExists()
    }

    @Test
    fun operador_solo_ve_la_lista_sin_botones_de_gestion() {
        mostrarHome(Rol.OPERADOR)

        composeRule.onNodeWithText("Movimiento detectado").assertExists()
        composeRule.onNodeWithText("Nuevo evento").assertDoesNotExist()
        composeRule.onNodeWithText("Marcar resuelto").assertDoesNotExist()
        composeRule.onNodeWithText("Eliminar").assertDoesNotExist()
    }

    @Test
    fun admin_ve_la_tarjeta_del_modo_guardian_y_operador_no() {
        mostrarHome(Rol.ADMIN)
        composeRule.onNodeWithText("Estado: DESACTIVADO").assertExists()
    }

    @Test
    fun operador_no_ve_la_tarjeta_del_modo_guardian() {
        mostrarHome(Rol.OPERADOR)
        composeRule.onNodeWithText("Estado: DESACTIVADO").assertDoesNotExist()
    }

    @Test
    fun el_home_muestra_el_rol_del_usuario() {
        mostrarHome(Rol.SUPERVISOR)
        composeRule.onNodeWithText("Rol: Supervisor").assertExists()
    }

    @Test
    fun al_tocar_posts_debe_navegar_a_la_pantalla_de_posts() {
        var navego = false
        mostrarHome(Rol.ADMIN, onIrAPosts = { navego = true })

        composeRule.onNodeWithContentDescription("Posts").performClick()

        assertTrue(navego)
    }

    @Test
    fun al_tocar_cerrar_sesion_debe_llamar_al_callback() {
        var cerro = false
        mostrarHome(Rol.ADMIN, onCerrarSesion = { cerro = true })

        composeRule.onNodeWithContentDescription("Cerrar sesión").performClick()

        assertTrue(cerro)
    }

    @Test
    fun mientras_cargan_los_eventos_no_debe_mostrarlos() {
        mostrarHome(Rol.ADMIN, cargando = true)

        composeRule.onNodeWithText("Movimiento detectado").assertDoesNotExist()
    }

    @Test
    fun si_fallan_los_eventos_debe_mostrar_el_error_y_reintentar() {
        var reintentos = 0
        mostrarHome(
            Rol.ADMIN,
            error = "No se pudieron cargar los eventos. Revisa tu conexión.",
            onReintentar = { reintentos++ }
        )

        composeRule.onNodeWithText("No se pudieron cargar los eventos. Revisa tu conexión.").assertExists()
        composeRule.onNodeWithText("Reintentar").performScrollTo().performClick()

        assertEquals(1, reintentos)
    }

    @Test
    fun admin_puede_crear_un_evento_desde_el_dialogo() {
        var creado: Pair<String, String>? = null
        mostrarHome(Rol.ADMIN, onCrearEvento = { titulo, descripcion -> creado = titulo to descripcion })

        composeRule.onNodeWithText("Nuevo evento").performScrollTo().performClick()
        composeRule.onNodeWithText("Título").performTextInput("Intruso")
        composeRule.onNodeWithText("Descripción").performTextInput("Puerta forzada")
        composeRule.onNodeWithText("Crear").performClick()

        assertEquals("Intruso" to "Puerta forzada", creado)
    }
}
