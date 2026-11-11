package com.example.modoguardian.model

data class SecurityEvent(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val severidad: Severidad = Severidad.BAJA,
    val camara: String = ""
)