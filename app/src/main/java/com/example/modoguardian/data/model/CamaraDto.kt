package com.example.modoguardian.data.model

import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.EstadoCamara

// Cámara tal como la entrega la API; los campos opcionales evitan errores si falta alguno
data class CamaraDto(
    val id: Int = 0,
    val nombre: String? = null,
    val zona: String? = null,
    val estado: String? = null,
    val grabando: Boolean? = null
)

fun CamaraDto.toCamara(): Camara {
    return Camara(
        id = id.toString(),
        nombre = nombre ?: "Cámara $id",
        zona = zona.orEmpty(),
        estado = EstadoCamara.desdeTexto(estado),
        grabando = grabando ?: false
    )
}