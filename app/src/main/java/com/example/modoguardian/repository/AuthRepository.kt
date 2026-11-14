package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth
import kotlinx.coroutines.withTimeoutOrNull

private const val TIEMPO_MAXIMO_MS = 20_000L

// Repositorio de autenticación: normaliza el correo, maneja la simulación sin conexión y corta las esperas demasiado largas
class AuthRepository(private val servicio: AuthService = FirebaseAuthService()) {

    suspend fun autenticar(
        email: String,
        password: String,
        simularSinConexion: Boolean
    ): ResultadoAuth {
        // Interruptor de demostración: simula una caída de conexión sin llamar al servicio
        if (simularSinConexion) return ResultadoAuth.SinConexion

        return withTimeoutOrNull(TIEMPO_MAXIMO_MS) {
            servicio.iniciarSesion(email.trim().lowercase(), password)
        } ?: ResultadoAuth.SinConexion
    }

    suspend fun autenticarConGoogle(idToken: String): ResultadoAuth {
        return withTimeoutOrNull(TIEMPO_MAXIMO_MS) {
            servicio.iniciarSesionConGoogle(idToken)
        } ?: ResultadoAuth.SinConexion
    }

    fun cerrarSesion() {
        servicio.cerrarSesion()
    }
}