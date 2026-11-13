package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth

// Repositorio de autenticación: normaliza el correo, maneja la simulación sin conexión y delega en el servicio
class AuthRepository(private val servicio: AuthService = FirebaseAuthService()) {

    suspend fun autenticar(
        email: String,
        password: String,
        simularSinConexion: Boolean
    ): ResultadoAuth {
        // Interruptor de demostración: simula una caída de conexión sin llamar al servicio
        if (simularSinConexion) return ResultadoAuth.SinConexion

        return servicio.iniciarSesion(email.trim().lowercase(), password)
    }

    fun cerrarSesion() {
        servicio.cerrarSesion()
    }
}