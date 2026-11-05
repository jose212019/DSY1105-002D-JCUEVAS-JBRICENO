package com.example.modoguardian.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.modoguardian.data.model.Post
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Pruebas de UI de PostScreen: lista, carga y error
class PostScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val fakePosts = listOf(
        Post(userId = 1, id = 1, title = "Título 1", body = "Contenido 1"),
        Post(userId = 2, id = 2, title = "Título 2", body = "Contenido 2")
    )

    @Test
    fun el_titulo_de_post_debe_aparecer_en_pantalla() {
        composeRule.setContent {
            ModoGuardianTheme {
                PostContenido(posts = fakePosts, cargando = false, error = null, onReintentar = {})
            }
        }

        composeRule.onNodeWithText("Título: Título 1").assertIsDisplayed()
        composeRule.onNodeWithText("Título: Título 2").assertIsDisplayed()
    }

    @Test
    fun el_cuerpo_de_cada_post_debe_aparecer_en_pantalla() {
        composeRule.setContent {
            ModoGuardianTheme {
                PostContenido(posts = fakePosts, cargando = false, error = null, onReintentar = {})
            }
        }

        composeRule.onNodeWithText("Contenido 1").assertIsDisplayed()
        composeRule.onNodeWithText("Contenido 2").assertIsDisplayed()
    }

    @Test
    fun mientras_carga_no_debe_mostrar_posts() {
        composeRule.setContent {
            ModoGuardianTheme {
                PostContenido(posts = fakePosts, cargando = true, error = null, onReintentar = {})
            }
        }

        composeRule.onNodeWithText("Título: Título 1").assertDoesNotExist()
    }

    @Test
    fun si_hay_error_debe_mostrar_el_mensaje_y_el_boton_reintentar() {
        composeRule.setContent {
            ModoGuardianTheme {
                PostContenido(
                    posts = emptyList(),
                    cargando = false,
                    error = "No se pudieron cargar los posts. Revisa tu conexión.",
                    onReintentar = {}
                )
            }
        }

        composeRule.onNodeWithText("No se pudieron cargar los posts. Revisa tu conexión.").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun al_tocar_reintentar_debe_llamar_al_callback() {
        var reintentos = 0
        composeRule.setContent {
            ModoGuardianTheme {
                PostContenido(
                    posts = emptyList(),
                    cargando = false,
                    error = "Error de red",
                    onReintentar = { reintentos++ }
                )
            }
        }

        composeRule.onNodeWithText("Reintentar").performClick()

        assertTrue(reintentos == 1)
    }
}
