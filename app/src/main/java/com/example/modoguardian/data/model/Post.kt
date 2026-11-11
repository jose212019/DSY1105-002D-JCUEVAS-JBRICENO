package com.example.modoguardian.data.model

// Representa un post de la API; los tres últimos campos los usa ModoGuardian para sus eventos
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String,
    val severidad: String? = null,
    val resuelto: Boolean? = null,
    val camara: String? = null
)