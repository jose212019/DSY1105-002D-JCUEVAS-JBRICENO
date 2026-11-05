package com.example.modoguardian.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

// Pruebas de los permisos de cada rol
class RolTest : StringSpec({

    "Admin puede cambiar el Modo Guardián y gestionar eventos" {
        Rol.ADMIN.puedeCambiarModo shouldBe true
        Rol.ADMIN.puedeGestionarEventos shouldBe true
    }

    "Supervisor puede cambiar el Modo Guardián y gestionar eventos" {
        Rol.SUPERVISOR.puedeCambiarModo shouldBe true
        Rol.SUPERVISOR.puedeGestionarEventos shouldBe true
    }

    "Operador es de solo lectura: no cambia el modo ni gestiona eventos" {
        Rol.OPERADOR.puedeCambiarModo shouldBe false
        Rol.OPERADOR.puedeGestionarEventos shouldBe false
    }

    "Admin es el único con el módulo de gestión de usuarios" {
        Rol.ADMIN.modulos shouldContain "Gestión de usuarios"
        Rol.SUPERVISOR.modulos.contains("Gestión de usuarios") shouldBe false
        Rol.OPERADOR.modulos.contains("Gestión de usuarios") shouldBe false
    }

    "Operador solo tiene un módulo disponible" {
        Rol.OPERADOR.modulos shouldHaveSize 1
    }

    "cada rol tiene su etiqueta para mostrar en pantalla" {
        Rol.ADMIN.etiqueta shouldBe "Admin"
        Rol.SUPERVISOR.etiqueta shouldBe "Supervisor"
        Rol.OPERADOR.etiqueta shouldBe "Operador"
    }
})