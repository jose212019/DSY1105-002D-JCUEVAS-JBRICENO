package com.example.modoguardian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.modoguardian.model.Accion
import com.example.modoguardian.model.TipoAccion

// Registro de una acción sobre un evento: quién la hizo, con qué rol y cuándo (trazabilidad)
@Entity(tableName = "acciones")
data class AccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: String,
    val eventoTitulo: String,
    val tipo: String,
    val usuario: String,
    val rol: String,
    val fecha: Long
)

fun AccionEntity.toAccion(): Accion {
    return Accion(
        id = id,
        eventoId = eventoId,
        eventoTitulo = eventoTitulo,
        tipo = TipoAccion.entries.firstOrNull { it.name == tipo } ?: TipoAccion.CREADO,
        usuario = usuario,
        rol = rol,
        fecha = fecha
    )
}