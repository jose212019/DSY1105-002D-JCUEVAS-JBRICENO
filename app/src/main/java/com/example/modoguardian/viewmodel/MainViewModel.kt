package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.Accion
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.model.TipoAccion
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.repository.SecurityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Maneja la navegación, los eventos de seguridad (con copia local) y el registro de acciones para la trazabilidad
class MainViewModel(
    private val repository: SecurityRepository = SecurityRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val events: StateFlow<List<SecurityEvent>> = _events

    private val _cargandoEventos = MutableStateFlow(false)
    val cargandoEventos: StateFlow<Boolean> = _cargandoEventos

    private val _errorEventos = MutableStateFlow<String?>(null)
    val errorEventos: StateFlow<String?> = _errorEventos

    private val _mostrandoDatosGuardados = MutableStateFlow(false)
    val mostrandoDatosGuardados: StateFlow<Boolean> = _mostrandoDatosGuardados

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private var usuarioActual: String = "sin sesión"
    private var rolActual: String = ""

    init {
        cargarEventos()
    }

    // Se llama cuando cambia la sesión, para anotar quién realiza cada acción
    fun establecerUsuario(email: String, rol: Rol?) {
        usuarioActual = email.ifBlank { "sin sesión" }
        rolActual = rol?.name.orEmpty()
    }

    fun acciones(): Flow<List<Accion>> = repository.observarAcciones()

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    // Con silencioso = true recarga sin spinner y, si falla, deja los datos actuales marcados como guardados
    fun cargarEventos(silencioso: Boolean = false) {
        viewModelScope.launch {
            if (!silencioso) {
                _cargandoEventos.value = true
                _errorEventos.value = null
            }
            try {
                val carga = repository.getSecurityEvents()
                _events.value = carga.eventos
                _mostrandoDatosGuardados.value = carga.desdeCache
                _errorEventos.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (silencioso) {
                    _mostrandoDatosGuardados.value = true
                } else {
                    _errorEventos.value = "No se pudieron cargar los eventos. Revisa tu conexión."
                }
            } finally {
                if (!silencioso) _cargandoEventos.value = false
            }
        }
    }

    fun crearEvento(
        titulo: String,
        descripcion: String,
        severidad: Severidad = Severidad.BAJA,
        camara: String = ""
    ) {
        viewModelScope.launch {
            try {
                val creado = repository.createEvent(titulo.trim(), descripcion.trim(), severidad, camara)
                _events.value = listOf(creado) + _events.value
                _mensaje.value = "Evento creado"
                registrar(TipoAccion.CREADO, creado)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = "No se pudo crear el evento"
            }
        }
    }

    fun eliminarEvento(id: String) {
        viewModelScope.launch {
            try {
                val evento = _events.value.firstOrNull { it.id == id }
                if (repository.deleteEvent(id)) {
                    _events.value = _events.value.filter { it.id != id }
                    _mensaje.value = "Evento eliminado"
                    if (evento != null) registrar(TipoAccion.ELIMINADO, evento)
                } else {
                    _mensaje.value = "No se pudo eliminar el evento"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = "No se pudo eliminar el evento"
            }
        }
    }

    fun alternarResuelto(evento: SecurityEvent) {
        viewModelScope.launch {
            try {
                val actualizado = evento.copy(isResolved = !evento.isResolved)
                repository.updateEvent(actualizado)
                _events.value = _events.value.map { if (it.id == evento.id) actualizado else it }
                _mensaje.value = "Evento actualizado"
                registrar(
                    if (actualizado.isResolved) TipoAccion.RESUELTO else TipoAccion.REABIERTO,
                    actualizado
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = "No se pudo actualizar el evento"
            }
        }
    }

    // Guarda la acción en el historial; si falla, no impide que la acción principal quede hecha
    private suspend fun registrar(tipo: TipoAccion, evento: SecurityEvent) {
        try {
            repository.registrarAccion(tipo, evento, usuarioActual, rolActual)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // La trazabilidad nunca debe bloquear la operación del usuario
        }
    }

    fun navigateTo(screen: Screen, limpiarPila: Boolean = false) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateTo(screen, limpiarPila))
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateBack)
        }
    }
}