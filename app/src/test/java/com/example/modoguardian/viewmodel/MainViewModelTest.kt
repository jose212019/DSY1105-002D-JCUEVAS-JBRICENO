package com.example.modoguardian.viewmodel

import com.example.modoguardian.model.SecurityEvent
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

// Pruebas de MainViewModel: carga, creación, eliminación y resolución de eventos
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
        coEvery { repo.getSecurityEvents() } returns eventosIniciales
        return repo
    }

    "al iniciar debe cargar los eventos desde el repositorio" {
        val viewModel = MainViewModel(repoConEventos())

        viewModel.events.value shouldContainExactly eventosIniciales
        viewModel.cargandoEventos.value shouldBe false
        viewModel.errorEventos.value.shouldBeNull()
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
        coEvery { repo.getSecurityEvents() } throws RuntimeException("sin red") andThen eventosIniciales

        val viewModel = MainViewModel(repo)
        viewModel.errorEventos.value shouldBe "No se pudieron cargar los eventos. Revisa tu conexión."

        viewModel.cargarEventos()

        viewModel.errorEventos.value.shouldBeNull()
        viewModel.events.value shouldContainExactly eventosIniciales
    }

    "crearEvento() debe agregar el evento al inicio de la lista" {
        val repo = repoConEventos()
        coEvery { repo.createEvent("Nuevo", "Detalle") } returns
            SecurityEvent(id = "101", title = "Nuevo", description = "Detalle")

        val viewModel = MainViewModel(repo)
        viewModel.crearEvento("  Nuevo ", " Detalle ")

        viewModel.events.value shouldHaveSize 3
        viewModel.events.value.first().id shouldBe "101"
        viewModel.events.value.first().title shouldBe "Nuevo"
        viewModel.mensaje.value shouldBe "Evento creado"
    }

    "crearEvento() debe asignar un id único aunque la API repita el 101" {
        val repo = repoConEventos()
        coEvery { repo.createEvent(any(), any()) } returns
            SecurityEvent(id = "101", title = "Nuevo", description = "Detalle")

        val viewModel = MainViewModel(repo)
        viewModel.crearEvento("Nuevo", "Detalle")
        viewModel.crearEvento("Nuevo", "Detalle")

        viewModel.events.value.map { it.id } shouldContainExactly listOf("102", "101", "1", "2")
    }

    "crearEvento() debe publicar un mensaje de error si la API falla" {
        val repo = repoConEventos()
        coEvery { repo.createEvent(any(), any()) } throws RuntimeException("sin red")

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

    "alternarResuelto() debe marcar el evento como resuelto y llamar a la API" {
        val repo = repoConEventos()
        coEvery { repo.updateEvent(any()) } returns eventosIniciales[0]

        val viewModel = MainViewModel(repo)
        viewModel.alternarResuelto(eventosIniciales[0])

        viewModel.events.value.first { it.id == "1" }.isResolved shouldBe true
        viewModel.mensaje.value shouldBe "Evento actualizado"
        coVerify(exactly = 1) { repo.updateEvent(any()) }
    }

    "alternarResuelto() no debe llamar a la API con eventos creados localmente" {
        val repo = repoConEventos()
        coEvery { repo.createEvent(any(), any()) } returns
            SecurityEvent(id = "101", title = "Nuevo", description = "Detalle")

        val viewModel = MainViewModel(repo)
        viewModel.crearEvento("Nuevo", "Detalle")
        viewModel.alternarResuelto(viewModel.events.value.first())

        viewModel.events.value.first().isResolved shouldBe true
        coVerify(exactly = 0) { repo.updateEvent(any()) }
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