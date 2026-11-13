package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth

// Contrato del servicio de autenticación: permite cambiar Firebase por un servicio falso en los tests
interface AuthService {
    suspend fun iniciarSesion(email: String, password: String): ResultadoAuth
    suspend fun iniciarSesionConGoogle(idToken: String): ResultadoAuth
    fun cerrarSesion()
}