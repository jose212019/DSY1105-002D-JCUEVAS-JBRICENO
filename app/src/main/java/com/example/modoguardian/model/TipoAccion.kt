package com.example.modoguardian.model

// Tipos de acción que se registran sobre un evento
enum class TipoAccion(val verbo: String) {
    CREADO("Creó"),
    RESUELTO("Resolvió"),
    REABIERTO("Reabrió"),
    ELIMINADO("Eliminó")
}