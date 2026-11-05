package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.SecurityEvent
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

// Maneja la navegación y los eventos de seguridad cargados desde la API
class MainViewModel : ViewModel() {

    private val repository = SecurityRepository()

    private val _events = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val events: StateFlow<List<SecurityEvent>> = _events

    private val _cargandoEventos = MutableStateFlow(false)
    val cargandoEventos: StateFlow<Boolean> = _cargandoEventos

    private val _errorEventos = MutableStateFlow<String?>(null)
    val errorEventos: StateFlow<String?> = _errorEventos

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        cargarEventos()
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