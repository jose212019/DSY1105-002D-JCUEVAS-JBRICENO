package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth
import com.example.modoguardian.model.ResultadoRegistro

// Contrato del servicio de autenticación: permite cambiar Firebase por un servicio falso en los tests
interface AuthService {
    suspend fun iniciarSesion(email: String, password: String): ResultadoAuth
    suspend fun iniciarSesionConGoogle(idToken: String): ResultadoAuth
    suspend fun registrar(nombre: String, email: String, password: String): ResultadoRegistro
    fun cerrarSesion()
}