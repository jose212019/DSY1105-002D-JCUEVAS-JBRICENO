package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth
import com.example.modoguardian.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

// Pruebas de AuthRepository: login por rol, credenciales inválidas y simulación sin conexión
class AuthRepositoryTest : StringSpec({

    val repo = AuthRepository()

    "admin con credenciales correctas debe entrar con rol ADMIN" {
        runTest {
            repo.autenticar("admin@guardian.test", "123456", false) shouldBe ResultadoAuth.Exito(Rol.ADMIN)
        }
    }

    "supervisor con credenciales correctas debe entrar con rol SUPERVISOR" {
        runTest {
            repo.autenticar("supervisor@guardian.test", "123456", false) shouldBe ResultadoAuth.Exito(Rol.SUPERVISOR)
        }
    }

    "operador con credenciales correctas debe entrar con rol OPERADOR" {
        runTest {
            repo.autenticar("operador@guardian.test", "123456", false) shouldBe ResultadoAuth.Exito(Rol.OPERADOR)
        }
    }

    "el correo debe aceptarse sin importar mayúsculas ni espacios" {
        runTest {
            repo.autenticar("  ADMIN@Guardian.Test ", "123456", false) shouldBe ResultadoAuth.Exito(Rol.ADMIN)
        }
    }

    "una contraseña incorrecta debe devolver CredencialesInvalidas" {
        runTest {
            repo.autenticar("admin@guardian.test", "clave-mala", false) shouldBe ResultadoAuth.CredencialesInvalidas
        }
    }

    "un correo que no existe debe devolver CredencialesInvalidas" {
        runTest {
            repo.autenticar("otro@guardian.test", "123456", false) shouldBe ResultadoAuth.CredencialesInvalidas
        }
    }

    "con la simulación de conexión caída debe devolver SinConexion" {
        runTest {
            repo.autenticar("admin@guardian.test", "123456", true) shouldBe ResultadoAuth.SinConexion
        }
    }
})