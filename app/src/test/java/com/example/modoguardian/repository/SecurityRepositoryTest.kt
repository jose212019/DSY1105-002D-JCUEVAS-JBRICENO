package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Pruebas de SecurityRepository con un ApiService simulado con MockK
class SecurityRepositoryTest : StringSpec({

    "getSecurityEvents() debe devolver todos los posts convertidos a eventos" {
        val posts = (1..15).map { Post(userId = 1, id = it, title = "Título $it", body = "Cuerpo $it") }
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns posts

        val eventos = SecurityRepository(api).getSecurityEvents()

        eventos shouldHaveSize 15
        eventos.first().id shouldBe "1"
        eventos.first().title shouldBe "Título 1"
        eventos.first().description shouldBe "Cuerpo 1"
        eventos.last().id shouldBe "15"
    }

    "getSecurityEvents() debe leer severidad, resuelto y cámara de la API" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns listOf(
            Post(userId = 1, id = 1, title = "Movimiento", body = "Detalle", severidad = "alta", resuelto = true, camara = "CAM-03")
        )

        val evento = SecurityRepository(api).getSecurityEvents().first()

        evento.severidad shouldBe Severidad.ALTA
        evento.isResolved shouldBe true
        evento.camara shouldBe "CAM-03"
    }

    "getSecurityEvents() debe usar valores por defecto si la API no trae los campos nuevos" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns listOf(
            Post(userId = 1, id = 1, title = "Antiguo", body = "Sin campos")
        )

        val evento = SecurityRepository(api).getSecurityEvents().first()

        evento.severidad shouldBe Severidad.BAJA
        evento.isResolved shouldBe false
        evento.camara shouldBe ""
    }

    "createEvent() debe enviar severidad y cámara a la API y devolver el evento creado" {
        val api = mockk<ApiService>()
        coEvery {
            api.createPost(
                Post(userId = 1, id = 0, title = "Intruso", body = "Puerta forzada", severidad = "alta", resuelto = false, camara = "CAM-01")
            )
        } returns Post(userId = 1, id = 16, title = "Intruso", body = "Puerta forzada", severidad = "alta", resuelto = false, camara = "CAM-01")

        val creado = SecurityRepository(api).createEvent("Intruso", "Puerta forzada", Severidad.ALTA, "CAM-01")

        creado.id shouldBe "16"
        creado.title shouldBe "Intruso"
        creado.severidad shouldBe Severidad.ALTA
        creado.camara shouldBe "CAM-01"
    }

    "updateEvent() debe enviar el estado resuelto usando el id del evento" {
        val evento = SecurityEvent(
            id = "5",
            title = "Alarma",
            description = "Sensor activado",
            isResolved = true,
            severidad = Severidad.MEDIA,
            camara = "CAM-02"
        )
        val api = mockk<ApiService>()
        coEvery {
            api.updatePost(
                5,
                Post(userId = 1, id = 5, title = "Alarma", body = "Sensor activado", severidad = "media", resuelto = true, camara = "CAM-02")
            )
        } returns Post(userId = 1, id = 5, title = "Alarma", body = "Sensor activado", severidad = "media", resuelto = true, camara = "CAM-02")

        val actualizado = SecurityRepository(api).updateEvent(evento)

        actualizado.id shouldBe "5"
        actualizado.isResolved shouldBe true
    }

    "deleteEvent() debe devolver true cuando la API responde con éxito" {
        val api = mockk<ApiService>()
        coEvery { api.deletePost(3) } returns Response.success(Unit)

        SecurityRepository(api).deleteEvent("3") shouldBe true
    }

    "deleteEvent() debe devolver false cuando la API responde con error" {
        val api = mockk<ApiService>()
        coEvery { api.deletePost(3) } returns Response.error(404, "".toResponseBody(null))

        SecurityRepository(api).deleteEvent("3") shouldBe false
    }
})