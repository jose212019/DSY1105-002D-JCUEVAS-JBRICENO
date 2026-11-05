package com.example.modoguardian.data.model

import com.example.modoguardian.model.SecurityEvent

// Convierte un Post de la API en un evento de seguridad de la app
fun Post.toSecurityEvent(): SecurityEvent {
    return SecurityEvent(
        id = id.toString(),
        title = title,
        description = body
    )
}

// Convierte un evento de seguridad en un Post para enviarlo a la API
fun SecurityEvent.toPost(): Post {
    return Post(
        userId = 1,
        id = id.toIntOrNull() ?: 0,
        title = title,
        body = description
    )
}