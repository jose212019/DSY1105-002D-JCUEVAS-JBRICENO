package com.example.modoguardian.model

// Estado de funcionamiento de una cámara
enum class EstadoCamara(val etiqueta: String) {
    EN_LINEA("En línea"),
    SIN_CONEXION("Sin conexión"),
    MANTENCION("Mantención");

    companion object {
        fun desdeTexto(texto: String?): EstadoCamara {
            return entries.firstOrNull { it.etiqueta.equals(texto?.trim(), ignoreCase = true) } ?: SIN_CONEXION
        }
    }
}

data class Camara(
    val id: String,
    val nombre: String,
    val zona: String,
    val estado: EstadoCamara,
    val grabando: Boolean
) {
    val codigo: String
        get() = "CAM-" + id.padStart(2, '0')
}