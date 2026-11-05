package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.model.toPost
import com.example.modoguardian.data.model.toSecurityEvent
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.data.remote.RetrofitInstance
import com.example.modoguardian.model.SecurityEvent

// Eventos de seguridad obtenidos y modificados a través de la API REST, recibe el ApiService para poder testearlo
class SecurityRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun getSecurityEvents(): List<SecurityEvent> {
        return api.getPosts().take(10).map { it.toSecurityEvent() }
    }

    suspend fun createEvent(titulo: String, descripcion: String): SecurityEvent {
        val nuevo = Post(userId = 1, id = 0, title = titulo, body = descripcion)
        return api.createPost(nuevo).toSecurityEvent()
    }

    suspend fun updateEvent(evento: SecurityEvent): SecurityEvent {
        return api.updatePost(evento.id.toIntOrNull() ?: 0, evento.toPost()).toSecurityEvent()
    }

    suspend fun deleteEvent(id: String): Boolean {
        return api.deletePost(id.toIntOrNull() ?: 0).isSuccessful
    }
}