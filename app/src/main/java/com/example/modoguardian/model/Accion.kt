package com.example.modoguardian.model

// Una acción registrada: quién la hizo, con qué rol, sobre qué evento y cuándo
data class Accion(
    val id: Long,
    val eventoId: String,
    val eventoTitulo: String,
    val tipo: TipoAccion,
    val usuario: String,
    val rol: String,
    val fecha: Long
)