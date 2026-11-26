package com.example.modoguardian.model

// Resultado de crear una cuenta nueva
sealed class ResultadoRegistro {
    data class Exito(val rol: Rol, val email: String) : ResultadoRegistro()
    data object CorreoEnUso : ResultadoRegistro()
    data object ContrasenaDebil : ResultadoRegistro()
    data object SinConexion : ResultadoRegistro()
    data object ErrorInesperado : ResultadoRegistro()
}
