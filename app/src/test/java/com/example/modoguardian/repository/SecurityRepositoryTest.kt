package com.example.modoguardian.repository

import com.example.modoguardian.data.local.AccionDao
import com.example.modoguardian.data.local.AccionEntity
import com.example.modoguardian.data.local.EventoDao
import com.example.modoguardian.data.local.EventoEntity
import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.TipoAccion
import com.example.modoguardian.model.Severidad
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Pruebas de SecurityRepository con un ApiService y un EventoDao simulados con MockK
class SecurityRepositoryTest : StringSpec({

    "getSecurityEvents() debe devolver todos los posts convertidos a eventos" {
        val posts = (1..15).map { Post(userId = 1, id = it, title = "Título $it", body = "Cuerpo $it") }
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns posts

        val carga = SecurityRepository(api).getSecurityEvents()

        carga.desdeCache shouldBe false
        carga.eventos shouldHaveSize 15
        carga.eventos.first().id shouldBe "1"
        carga.eventos.first().title shouldBe "Título 1"
        carga.eventos.first().description shouldBe "Cuerpo 1"
        carga.eventos.last().id shouldBe "15"
    }

    "getSecurityEvents() debe leer severidad, resuelto y cámara de la API" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns listOf(
            Post(userId = 1, id = 1, title = "Movimiento", body = "Detalle", severidad = "alta", resuelto = true, camara = "CAM-03")
        )

        val evento = SecurityRepository(api).getSecurityEvents().eventos.first()

        evento.severidad shouldBe Severidad.ALTA
        evento.isResolved shouldBe true
        evento.camara shouldBe "CAM-03"
    }

    "getSecurityEvents() debe usar valores por defecto si la API no trae los campos nuevos" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns listOf(
            Post(userId = 1, id = 1, title = "Antiguo", body = "Sin campos")
        )

        val evento = SecurityRepository(api).getSecurityEvents().eventos.first()

        evento.severidad shouldBe Severidad.BAJA
        evento.isResolved shouldBe false
        evento.camara shouldBe ""
    }

    "si la API funciona, guarda los eventos en la base local" {
        val api = mockk<ApiService>()
        val dao = mockk<EventoDao>()
        coEvery { api.getPosts() } returns listOf(Post(userId = 1, id = 1, title = "A", body = "B"))
        coEvery { dao.reemplazarTodos(any()) } returns Unit

        val carga = SecurityRepository(api, dao).getSecurityEvents()

        carga.desdeCache shouldBe false
        coVerify(exactly = 1) { dao.reemplazarTodos(any()) }
    }

    "si la API falla y hay datos guardados, devuelve los guardados marcados como caché" {
        val api = mockk<ApiService>()
        val dao = mockk<EventoDao>()
        coEvery { api.getPosts() } throws RuntimeException("sin red")
        coEvery { dao.obtenerTodos() } returns listOf(
            EventoEntity(id = "7", titulo = "Guardado", descripcion = "Detalle", marcaTiempo = 0L, resuelto = true, severidad = "alta", camara = "CAM-03")
        )

        val carga = SecurityRepository(api, dao).getSecurityEvents()

        carga.desdeCache shouldBe true
        carga.eventos.first().id shouldBe "7"
        carga.eventos.first().severidad shouldBe Severidad.ALTA
        carga.eventos.first().isResolved shouldBe true
    }

    "si la API falla y no hay datos guardados, propaga el error" {
        val api = mockk<ApiService>()
        val dao = mockk<EventoDao>()
        coEvery { api.getPosts() } throws RuntimeException("sin red")
        coEvery { dao.obtenerTodos() } returns emptyList()

        shouldThrow<RuntimeException> {
            SecurityRepository(api, dao).getSecurityEvents()
        }
    }

    "si falla el guardado local, igual devuelve los datos de la API" {
        val api = mockk<ApiService>()
        val dao = mockk<EventoDao>()
        coEvery { api.getPosts() } returns listOf(Post(userId = 1, id = 1, title = "A", body = "B"))
        coEvery { dao.reemplazarTodos(any()) } throws RuntimeException("disco lleno")

        val carga = SecurityRepository(api, dao).getSecurityEvents()

        carga.desdeCache shouldBe false
        carga.eventos shouldHaveSize 1
    }

    "registrarAccion() guarda en la base local quién hizo qué sobre el evento" {
        val dao = mockk<AccionDao>()
        val guardada = slot<AccionEntity>()
        coEvery { dao.insertar(capture(guardada)) } returns Unit
        val evento = SecurityEvent(id = "4", title = "Puerta abierta", description = "Detalle")

        SecurityRepository(api = mockk(), accionDao = dao)
            .registrarAccion(TipoAccion.RESUELTO, evento, "admin@guardian.test", "ADMIN")

        guardada.captured.eventoId shouldBe "4"
        guardada.captured.eventoTitulo shouldBe "Puerta abierta"
        guardada.captured.tipo shouldBe "RESUELTO"
        guardada.captured.usuario shouldBe "admin@guardian.test"
        guardada.captured.rol shouldBe "ADMIN"
    }

    "observarAcciones() convierte los registros guardados en acciones" {
        val dao = mockk<AccionDao>()
        every { dao.observarTodas() } returns flowOf(
            listOf(
                AccionEntity(id = 1, eventoId = "4", eventoTitulo = "Puerta abierta", tipo = "ELIMINADO", usuario = "supervisor@guardian.test", rol = "SUPERVISOR", fecha = 1000L)
            )
        )

        val acciones = SecurityRepository(api = mockk(), accionDao = dao).observarAcciones().first()

        acciones shouldHaveSize 1
        acciones.first().tipo shouldBe TipoAccion.ELIMINADO
        acciones.first().usuario shouldBe "supervisor@guardian.test"
    }

    "sin base local, registrarAccion() no falla y observarAcciones() devuelve una lista vacía" {
        val repo = SecurityRepository(api = mockk())

        repo.registrarAccion(TipoAccion.CREADO, SecurityEvent(id = "1", title = "A", description = "B"), "x", "ADMIN")

        repo.observarAcciones().first() shouldHaveSize 0
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