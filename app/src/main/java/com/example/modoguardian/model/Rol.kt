package com.example.modoguardian.model

// Roles de la app: define qué puede hacer cada usuario
enum class Rol(
    val etiqueta: String,
    val descripcion: String,
    val puedeCambiarModo: Boolean,
    val puedeGestionarEventos: Boolean,
    val modulos: List<String>
) {
    ADMIN(
        etiqueta = "Admin",
        descripcion = "Acceso total al sistema.",
        puedeCambiarModo = true,
        puedeGestionarEventos = true,
        modulos = listOf("Gestión de usuarios", "Eventos de seguridad", "Modo Guardián")
    ),
    SUPERVISOR(
        etiqueta = "Supervisor",
        descripcion = "Supervisa eventos y controla el Modo Guardián.",
        puedeCambiarModo = true,
        puedeGestionarEventos = true,
        modulos = listOf("Eventos de seguridad", "Modo Guardián")
    ),
    OPERADOR(
        etiqueta = "Operador",
        descripcion = "Solo consulta de eventos (solo lectura).",
        puedeCambiarModo = false,
        puedeGestionarEventos = false,
        modulos = listOf("Eventos de seguridad (solo lectura)")
    )
}