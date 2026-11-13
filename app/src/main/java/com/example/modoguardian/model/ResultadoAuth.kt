package com.example.modoguardian.model

sealed class ResultadoAuth {
    data class Exito(val rol: Rol, val email: String? = null) : ResultadoAuth()
    data object CredencialesInvalidas : ResultadoAuth()
    data object SinConexion : ResultadoAuth()
    data object ErrorInesperado : ResultadoAuth()
}