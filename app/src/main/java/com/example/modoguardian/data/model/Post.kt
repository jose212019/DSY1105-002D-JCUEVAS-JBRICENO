package com.example.modoguardian.data.model

// post obtenido desde la API de JSONPlaceholder
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)