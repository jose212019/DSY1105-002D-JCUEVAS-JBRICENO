package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth
import com.example.modoguardian.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify

// Pruebas de AuthRepository con un AuthService simulado con MockK
class AuthRepositoryTest : StringSpec({

    "un inicio de sesión correcto devuelve el rol que entrega el servicio" {
        val servicio = mockk<AuthService>()
        coEvery { servicio.iniciarSesion("admin@guardian.test", "123456") } returns ResultadoAuth.Exito(Rol.ADMIN)

        val resultado = AuthRepository(servicio).autenticar("admin@guardian.test", "123456", false)

        resultado shouldBe ResultadoAuth.Exito(Rol.ADMIN)
    }

    "el correo se limpia: sin espacios y en minúsculas antes de llamar al servicio" {
        val servicio = mockk<AuthService>()
        coEvery { servicio.iniciarSesion("operador@guardian.test", "123456") } returns ResultadoAuth.Exito(Rol.OPERADOR)

        val resultado = AuthRepository(servicio).autenticar("  OPERADOR@Guardian.Test ", "123456", false)

        resultado shouldBe ResultadoAuth.Exito(Rol.OPERADOR)
        coVerify(exactly = 1) { servicio.iniciarSesion("operador@guardian.test", "123456") }
    }

    "las credenciales inválidas del servicio se propagan tal cual" {
        val servicio = mockk<AuthService>()
        coEvery { servicio.iniciarSesion(any(), any()) } returns ResultadoAuth.CredencialesInvalidas

        AuthRepository(servicio).autenticar("admin@guardian.test", "clave-mala", false) shouldBe ResultadoAuth.CredencialesInvalidas
    }

    "un error de conexión del servicio se propaga tal cual" {
        val servicio = mockk<AuthService>()
        coEvery { servicio.iniciarSesion(any(), any()) } returns ResultadoAuth.SinConexion

        AuthRepository(servicio).autenticar("admin@guardian.test", "123456", false) shouldBe ResultadoAuth.SinConexion
    }

    "con la simulación de conexión caída devuelve SinConexion y no llama al servicio" {
        val servicio = mockk<AuthService>()

        val resultado = AuthRepository(servicio).autenticar("admin@guardian.test", "123456", true)

        resultado shouldBe ResultadoAuth.SinConexion
        coVerify(exactly = 0) { servicio.iniciarSesion(any(), any()) }
    }

    "cerrarSesion() delega en el servicio" {
        val servicio = mockk<AuthService>(relaxed = true)

        AuthRepository(servicio).cerrarSesion()

        verify(exactly = 1) { servicio.cerrarSesion() }
    }
})