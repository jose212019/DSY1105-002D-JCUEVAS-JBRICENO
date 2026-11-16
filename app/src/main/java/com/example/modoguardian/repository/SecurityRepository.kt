package com.example.modoguardian.repository

import com.example.modoguardian.data.local.AccionDao
import com.example.modoguardian.data.local.AccionEntity
import com.example.modoguardian.data.local.EventoDao
import com.example.modoguardian.data.local.toAccion
import com.example.modoguardian.data.local.toEntity
import com.example.modoguardian.data.local.toSecurityEvent
import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.model.toPost
import com.example.modoguardian.data.model.toSecurityEvent
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.data.remote.RetrofitInstance
import com.example.modoguardian.model.Accion
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.model.TipoAccion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Eventos cargados, indicando si vienen de la API o de la copia guardada en el teléfono
data class CargaEventos(
    val eventos: List<SecurityEvent>,
    val desdeCache: Boolean
)

// Eventos de seguridad: consulta la API REST, usa la copia local de Room si falla y registra las acciones realizadas
class SecurityRepository(
    private val api: ApiService = RetrofitInstance.api,
    private val eventoDao: EventoDao? = null,
    private val accionDao: AccionDao? = null
) {

    suspend fun getSecurityEvents(): CargaEventos {
        val eventos = try {
            api.getPosts().map { it.toSecurityEvent() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return desdeBaseLocal(e)
        }

        guardarEnBaseLocal(eventos)
        return CargaEventos(eventos = eventos, desdeCache = false)
    }

    private suspend fun desdeBaseLocal(causa: Exception): CargaEventos {
        val guardados = try {
            eventoDao?.obtenerTodos().orEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }

        if (guardados.isEmpty()) throw causa
        return CargaEventos(eventos = guardados.map { it.toSecurityEvent() }, desdeCache = true)
    }

    private suspend fun guardarEnBaseLocal(eventos: List<SecurityEvent>) {
        try {
            eventoDao?.reemplazarTodos(eventos.map { it.toEntity() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Si falla el guardado local, se sigue mostrando lo que entregó la API
        }
    }

    suspend fun createEvent(
        titulo: String,
        descripcion: String,
        severidad: Severidad = Severidad.BAJA,
        camara: String = ""
    ): SecurityEvent {
        val nuevo = Post(
            userId = 1,
            id = 0,
            title = titulo,
            body = descripcion,
            severidad = severidad.clave,
            resuelto = false,
            camara = camara
        )
        return api.createPost(nuevo).toSecurityEvent()
    }

    suspend fun updateEvent(evento: SecurityEvent): SecurityEvent {
        return api.updatePost(evento.id.toIntOrNull() ?: 0, evento.toPost()).toSecurityEvent()
    }

    suspend fun deleteEvent(id: String): Boolean {
        return api.deletePost(id.toIntOrNull() ?: 0).isSuccessful
    }

    // Trazabilidad: guarda quién hizo qué sobre un evento
    suspend fun registrarAccion(
        tipo: TipoAccion,
        evento: SecurityEvent,
        usuario: String,
        rol: String
    ) {
        accionDao?.insertar(
            AccionEntity(
                eventoId = evento.id,
                eventoTitulo = evento.title,
                tipo = tipo.name,
                usuario = usuario,
                rol = rol,
                fecha = System.currentTimeMillis()
            )
        )
    }

    fun observarAcciones(): Flow<List<Accion>> {
        val dao = accionDao ?: return flowOf(emptyList())
        return dao.observarTodas().map { lista -> lista.map { it.toAccion() } }
    }
}