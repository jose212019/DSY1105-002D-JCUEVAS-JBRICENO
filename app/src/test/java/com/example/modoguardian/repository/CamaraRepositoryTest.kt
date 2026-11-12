package com.example.modoguardian.repository

import com.example.modoguardian.data.model.CamaraDto
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.model.EstadoCamara
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

// Pruebas de CamaraRepository con un ApiService simulado con MockK
class CamaraRepositoryTest : StringSpec({

    fun repoCon(vararg camaras: CamaraDto): CamaraRepository {
        val api = mockk<ApiService>()
        coEvery { api.getCamaras() } returns camaras.toList()
        return CamaraRepository(api)
    }

    "getCamaras() debe convertir cada cámara de la API" {
        val camara = repoCon(
            CamaraDto(id = 3, nombre = "Perímetro Norte", zona = "Zona Norte", estado = "En línea", grabando = true)
        ).getCamaras().first()

        camara.id shouldBe "3"
        camara.nombre shouldBe "Perímetro Norte"
        camara.zona shouldBe "Zona Norte"
        camara.estado shouldBe EstadoCamara.EN_LINEA
        camara.grabando shouldBe true
    }

    "el código de la cámara debe ser CAM- con el id de dos dígitos" {
        val camaras = repoCon(CamaraDto(id = 3), CamaraDto(id = 12)).getCamaras()

        camaras[0].codigo shouldBe "CAM-03"
        camaras[1].codigo shouldBe "CAM-12"
    }

    "debe reconocer el estado Mantención" {
        repoCon(CamaraDto(id = 6, estado = "Mantención")).getCamaras().first().estado shouldBe EstadoCamara.MANTENCION
    }

    "un estado desconocido debe quedar como sin conexión" {
        repoCon(CamaraDto(id = 7, estado = "algo raro")).getCamaras().first().estado shouldBe EstadoCamara.SIN_CONEXION
    }

    "si la API no trae campos debe usar valores por defecto" {
        val camara = repoCon(CamaraDto(id = 5)).getCamaras().first()

        camara.nombre shouldBe "Cámara 5"
        camara.zona shouldBe ""
        camara.grabando shouldBe false
    }
})