package com.example.modoguardian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad

// Copia local de un evento de seguridad, para verlo aunque no haya conexión
@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val marcaTiempo: Long,
    val resuelto: Boolean,
    val severidad: String,
    val camara: String
)

fun EventoEntity.toSecurityEvent(): SecurityEvent {
    return SecurityEvent(
        id = id,
        title = titulo,
        description = descripcion,
        timestamp = marcaTiempo,
        isResolved = resuelto,
        severidad = Severidad.desdeClave(severidad),
        camara = camara
    )
}

fun SecurityEvent.toEntity(): EventoEntity {
    return EventoEntity(
        id = id,
        titulo = title,
        descripcion = description,
        marcaTiempo = timestamp,
        resuelto = isResolved,
        severidad = severidad.clave,
        camara = camara
    )
}