package com.example.modoguardian.data.model

import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad

// Convierte un Post de la API en un evento de seguridad de la app
fun Post.toSecurityEvent(): SecurityEvent {
    return SecurityEvent(
        id = id.toString(),
        title = title,
        description = body,
        isResolved = resuelto ?: false,
        severidad = Severidad.desdeClave(severidad),
        camara = camara.orEmpty()
    )
}

// Convierte un evento de seguridad en un Post para enviarlo a la API
fun SecurityEvent.toPost(): Post {
    return Post(
        userId = 1,
        id = id.toIntOrNull() ?: 0,
        title = title,
        body = description,
        severidad = severidad.clave,
        resuelto = isResolved,
        camara = camara
    )
}