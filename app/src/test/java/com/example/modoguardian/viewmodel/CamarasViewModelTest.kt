package com.example.modoguardian.viewmodel

import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.EstadoCamara
import com.example.modoguardian.repository.CamaraRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Pruebas de CamarasViewModel con un CamaraRepository simulado con MockK
@OptIn(ExperimentalCoroutinesApi::class)
class CamarasViewModelTest : StringSpec({

    val camaras = listOf(
        Camara("1", "Entrada Principal", "Zona Sur", EstadoCamara.EN_LINEA, true),
        Camara("7", "Bodega A", "Zona Sur", EstadoCamara.SIN_CONEXION, false)
    )

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "al iniciar debe cargar las cámaras desde el repositorio" {
        val repo = mockk<CamaraRepository>()
        coEvery { repo.getCamaras() } returns camaras

        val viewModel = CamarasViewModel(repo)

        viewModel.camaras.value shouldContainExactly camaras
        viewModel.cargando.value shouldBe false
        viewModel.error.value.shouldBeNull()
    }

    "debe publicar un mensaje de error si la API falla" {
        val repo = mockk<CamaraRepository>()
        coEvery { repo.getCamaras() } throws RuntimeException("sin red")

        val viewModel = CamarasViewModel(repo)

        viewModel.camaras.value.shouldBeEmpty()
        viewModel.error.value shouldBe "No se pudieron cargar las cámaras. Revisa tu conexión."
        viewModel.cargando.value shouldBe false
    }

    "cargarCamaras() debe limpiar el error al reintentar con éxito" {
        val repo = mockk<CamaraRepository>()
        coEvery { repo.getCamaras() } throws RuntimeException("sin red") andThen camaras

        val viewModel = CamarasViewModel(repo)
        viewModel.cargarCamaras()

        viewModel.error.value.shouldBeNull()
        viewModel.camaras.value shouldContainExactly camaras
    }
})