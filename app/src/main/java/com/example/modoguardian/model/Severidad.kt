package com.example.modoguardian.model

// Nivel de gravedad de un evento de seguridad
enum class Severidad(val etiqueta: String, val clave: String) {
    ALTA("Alta", "alta"),
    MEDIA("Media", "media"),
    BAJA("Baja", "baja");

    companion object {
        fun desdeClave(clave: String?): Severidad {
            return entries.firstOrNull { it.clave == clave?.lowercase() } ?: BAJA
        }
    }
}