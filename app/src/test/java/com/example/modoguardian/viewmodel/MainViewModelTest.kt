package com.example.modoguardian.viewmodel

import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.TipoAccion
import com.example.modoguardian.repository.CargaEventos
import com.example.modoguardian.repository.SecurityRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Pruebas de MainViewModel: carga con copia local, creación, eliminación y resolución de eventos
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest : StringSpec({

    val eventosIniciales = listOf(
        SecurityEvent(id = "1", title = "Evento 1", description = "Descripción 1"),
        SecurityEvent(id = "2", title = "Evento 2", description = "Descripción 2")
    )

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    fun repoConEventos(): SecurityRepository {
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } returns CargaEventos(eventosIniciales, desdeCache = false)
        return repo
    }

    "al iniciar debe cargar los eventos desde el repositorio" {
        val viewModel = MainViewModel(repoConEventos())

        viewModel.events.value shouldContainExactly eventosIniciales
        viewModel.cargandoEventos.value shouldBe false
        viewModel.errorEventos.value.shouldBeNull()
        viewModel.mostrandoDatosGuardados.value shouldBe false
    }

    "debe publicar un error si falla la carga de eventos" {
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } throws RuntimeException("sin red")

        val viewModel = MainViewModel(repo)

        viewModel.events.value shouldHaveSize 0
        viewModel.errorEventos.value shouldBe "No se pudieron cargar los eventos. Revisa tu conexión."
        viewModel.cargandoEventos.value shouldBe false
    }

    "cargarEventos() debe limpiar el error al reintentar con éxito" {
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } throws RuntimeException("sin red") andThen CargaEventos(eventosIniciales, false)

        val viewModel = MainViewModel(repo)
        viewModel.errorEventos.value shouldBe "No se pudieron cargar los eventos. Revisa tu conexión."

        viewModel.cargarEventos()

        viewModel.errorEventos.value.shouldBeNull()
        viewModel.events.value shouldContainExactly eventosIniciales
    }

    "debe avisar que muestra datos guardados cuando vienen de la copia local" {
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } returns CargaEventos(eventosIniciales, desdeCache = true)

        val viewModel = MainViewModel(repo)

        viewModel.events.value shouldContainExactly eventosIniciales
        viewModel.mostrandoDatosGuardados.value shouldBe true
        viewModel.errorEventos.value.shouldBeNull()
    }

    "una recarga silenciosa que falla conserva los eventos y activa el aviso de datos guardados" {
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } returns CargaEventos(eventosIniciales, false) andThenThrows RuntimeException("sin red")

        val viewModel = MainViewModel(repo)
        viewModel.cargarEventos(silencioso = true)

        viewModel.events.value shouldContainExactly eventosIniciales
        viewModel.mostrandoDatosGuardados.value shouldBe true
        viewModel.errorEventos.value.shouldBeNull()
    }

    "una recarga con éxito apaga el aviso de datos guardados" {
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } returns CargaEventos(eventosIniciales, true) andThen CargaEventos(eventosIniciales, false)

        val viewModel = MainViewModel(repo)
        viewModel.mostrandoDatosGuardados.value shouldBe true

        viewModel.cargarEventos()

        viewModel.mostrandoDatosGuardados.value shouldBe false
    }

    "crearEvento() debe agregar el evento creado por la API al inicio de la lista" {
        val repo = repoConEventos()
        coEvery { repo.createEvent("Nuevo", "Detalle", any(), any()) } returns
            SecurityEvent(id = "16", title = "Nuevo", description = "Detalle")

        val viewModel = MainViewModel(repo)
        viewModel.crearEvento("  Nuevo ", " Detalle ")

        viewModel.events.value shouldHaveSize 3
        viewModel.events.value.first().id shouldBe "16"
        viewModel.events.value.first().title shouldBe "Nuevo"
        viewModel.mensaje.value shouldBe "Evento creado"
    }

    "crearEvento() debe publicar un mensaje de error si la API falla" {
        val repo = repoConEventos()
        coEvery { repo.createEvent(any(), any(), any(), any()) } throws RuntimeException("sin red")

        val viewModel = MainViewModel(repo)
        viewModel.crearEvento("Nuevo", "Detalle")

        viewModel.events.value shouldHaveSize 2
        viewModel.mensaje.value shouldBe "No se pudo crear el evento"
    }

    "eliminarEvento() debe quitar el evento cuando la API responde con éxito" {
        val repo = repoConEventos()
        coEvery { repo.deleteEvent("1") } returns true

        val viewModel = MainViewModel(repo)
        viewModel.eliminarEvento("1")

        viewModel.events.value.map { it.id } shouldContainExactly listOf("2")
        viewModel.mensaje.value shouldBe "Evento eliminado"
    }

    "eliminarEvento() no debe quitar el evento si la API responde con error" {
        val repo = repoConEventos()
        coEvery { repo.deleteEvent("1") } returns false

        val viewModel = MainViewModel(repo)
        viewModel.eliminarEvento("1")

        viewModel.events.value shouldHaveSize 2
        viewModel.mensaje.value shouldBe "No se pudo eliminar el evento"
    }

    "alternarResuelto() debe marcar el evento como resuelto y guardarlo en la API" {
        val repo = repoConEventos()
        coEvery { repo.updateEvent(any()) } returns eventosIniciales[0]

        val viewModel = MainViewModel(repo)
        viewModel.alternarResuelto(eventosIniciales[0])

        viewModel.events.value.first { it.id == "1" }.isResolved shouldBe true
        viewModel.mensaje.value shouldBe "Evento actualizado"
        coVerify(exactly = 1) { repo.updateEvent(any()) }
    }

    "alternarResuelto() debe reabrir un evento que ya estaba resuelto" {
        val resuelto = SecurityEvent(id = "1", title = "Evento 1", description = "Descripción 1", isResolved = true)
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } returns CargaEventos(listOf(resuelto), false)
        coEvery { repo.updateEvent(any()) } returns resuelto

        val viewModel = MainViewModel(repo)
        viewModel.alternarResuelto(resuelto)

        viewModel.events.value.first().isResolved shouldBe false
    }

    "alternarResuelto() no debe cambiar el evento si la API falla" {
        val repo = repoConEventos()
        coEvery { repo.updateEvent(any()) } throws RuntimeException("sin red")

        val viewModel = MainViewModel(repo)
        viewModel.alternarResuelto(eventosIniciales[0])

        viewModel.events.value.first { it.id == "1" }.isResolved shouldBe false
        viewModel.mensaje.value shouldBe "No se pudo actualizar el evento"
    }

    "crearEvento() debe registrar la acción CREADO con el usuario y su rol" {
        val repo = repoConEventos()
        val creado = SecurityEvent(id = "16", title = "Nuevo", description = "Detalle")
        coEvery { repo.createEvent("Nuevo", "Detalle", any(), any()) } returns creado
        coEvery { repo.registrarAccion(any(), any(), any(), any()) } returns Unit

        val viewModel = MainViewModel(repo)
        viewModel.establecerUsuario("admin@guardian.test", Rol.ADMIN)
        viewModel.crearEvento("Nuevo", "Detalle")

        coVerify(exactly = 1) { repo.registrarAccion(TipoAccion.CREADO, creado, "admin@guardian.test", "ADMIN") }
    }

    "eliminarEvento() debe registrar la acción ELIMINADO con el evento que se borró" {
        val repo = repoConEventos()
        coEvery { repo.deleteEvent("1") } returns true
        coEvery { repo.registrarAccion(any(), any(), any(), any()) } returns Unit

        val viewModel = MainViewModel(repo)
        viewModel.establecerUsuario("supervisor@guardian.test", Rol.SUPERVISOR)
        viewModel.eliminarEvento("1")

        coVerify(exactly = 1) { repo.registrarAccion(TipoAccion.ELIMINADO, eventosIniciales[0], "supervisor@guardian.test", "SUPERVISOR") }
    }

    "alternarResuelto() debe registrar RESUELTO cuando el evento pasa a resuelto" {
        val repo = repoConEventos()
        coEvery { repo.updateEvent(any()) } returns eventosIniciales[0]
        coEvery { repo.registrarAccion(any(), any(), any(), any()) } returns Unit

        val viewModel = MainViewModel(repo)
        viewModel.establecerUsuario("admin@guardian.test", Rol.ADMIN)
        viewModel.alternarResuelto(eventosIniciales[0])

        coVerify(exactly = 1) {
            repo.registrarAccion(TipoAccion.RESUELTO, eventosIniciales[0].copy(isResolved = true), "admin@guardian.test", "ADMIN")
        }
    }

    "alternarResuelto() debe registrar REABIERTO cuando el evento vuelve a pendiente" {
        val resuelto = SecurityEvent(id = "1", title = "Evento 1", description = "Descripción 1", isResolved = true)
        val repo = mockk<SecurityRepository>()
        coEvery { repo.getSecurityEvents() } returns CargaEventos(listOf(resuelto), false)
        coEvery { repo.updateEvent(any()) } returns resuelto
        coEvery { repo.registrarAccion(any(), any(), any(), any()) } returns Unit

        val viewModel = MainViewModel(repo)
        viewModel.establecerUsuario("admin@guardian.test", Rol.ADMIN)
        viewModel.alternarResuelto(resuelto)

        coVerify(exactly = 1) {
            repo.registrarAccion(TipoAccion.REABIERTO, resuelto.copy(isResolved = false), "admin@guardian.test", "ADMIN")
        }
    }

    "si falla el registro de la acción, la acción principal igual se realiza" {
        val repo = repoConEventos()
        coEvery { repo.deleteEvent("1") } returns true
        coEvery { repo.registrarAccion(any(), any(), any(), any()) } throws RuntimeException("disco lleno")

        val viewModel = MainViewModel(repo)
        viewModel.eliminarEvento("1")

        viewModel.events.value.map { it.id } shouldContainExactly listOf("2")
        viewModel.mensaje.value shouldBe "Evento eliminado"
    }

    "limpiarMensaje() debe dejar el mensaje en null" {
        val repo = repoConEventos()
        coEvery { repo.deleteEvent("1") } returns true

        val viewModel = MainViewModel(repo)
        viewModel.eliminarEvento("1")
        viewModel.limpiarMensaje()

        viewModel.mensaje.value.shouldBeNull()
    }
})