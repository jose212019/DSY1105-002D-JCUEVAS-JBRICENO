package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.model.SecurityEvent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Pruebas de SecurityRepository con un ApiService simulado con MockK
class SecurityRepositoryTest : StringSpec({

    "getSecurityEvents() debe devolver solo los primeros 10 posts convertidos a eventos" {
        val posts = (1..15).map { Post(userId = 1, id = it, title = "Título $it", body = "Cuerpo $it") }
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns posts

        val eventos = SecurityRepository(api).getSecurityEvents()

        eventos shouldHaveSize 10
        eventos.first().id shouldBe "1"
        eventos.first().title shouldBe "Título 1"
        eventos.first().description shouldBe "Cuerpo 1"
        eventos.last().id shouldBe "10"
    }

    "createEvent() debe enviar el evento a la API y devolver el evento creado" {
        val api = mockk<ApiService>()
        coEvery {
            api.createPost(Post(userId = 1, id = 0, title = "Intruso", body = "Puerta forzada"))
        } returns Post(userId = 1, id = 101, title = "Intruso", body = "Puerta forzada")

        val creado = SecurityRepository(api).createEvent("Intruso", "Puerta forzada")

        creado.id shouldBe "101"
        creado.title shouldBe "Intruso"
        creado.description shouldBe "Puerta forzada"
    }

    "updateEvent() debe actualizar usando el id del evento" {
        val evento = SecurityEvent(id = "5", title = "Alarma", description = "Sensor activado")
        val api = mockk<ApiService>()
        coEvery {
            api.updatePost(5, Post(userId = 1, id = 5, title = "Alarma", body = "Sensor activado"))
        } returns Post(userId = 1, id = 5, title = "Alarma", body = "Sensor activado")

        val actualizado = SecurityRepository(api).updateEvent(evento)

        actualizado.id shouldBe "5"
        actualizado.title shouldBe "Alarma"
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