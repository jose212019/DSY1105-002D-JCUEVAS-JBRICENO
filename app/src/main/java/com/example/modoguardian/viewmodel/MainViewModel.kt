package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.SecurityEvent
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.repository.SecurityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Maneja la navegación y los eventos de seguridad (cargar, crear, actualizar y eliminar) con la API
class MainViewModel(
    private val repository: SecurityRepository = SecurityRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val events: StateFlow<List<SecurityEvent>> = _events

    private val _cargandoEventos = MutableStateFlow(false)
    val cargandoEventos: StateFlow<Boolean> = _cargandoEventos

    private val _errorEventos = MutableStateFlow<String?>(null)
    val errorEventos: StateFlow<String?> = _errorEventos

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        cargarEventos()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _cargandoEventos.value = true
            _errorEventos.value = null
            try {
                _events.value = repository.getSecurityEvents()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorEventos.value = "No se pudieron cargar los eventos. Revisa tu conexión."
            } finally {
                _cargandoEventos.value = false
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
                if (repository.deleteEvent(id)) {
                    _events.value = _events.value.filter { it.id != id }
                    _mensaje.value = "Evento eliminado"
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = "No se pudo actualizar el evento"
            }
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