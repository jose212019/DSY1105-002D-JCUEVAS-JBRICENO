package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.Camara
import com.example.modoguardian.repository.CamaraRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Mantiene la lista de cámaras con su estado de carga y error
class CamarasViewModel(
    private val repository: CamaraRepository = CamaraRepository()
) : ViewModel() {

    private val _camaras = MutableStateFlow<List<Camara>>(emptyList())
    val camaras: StateFlow<List<Camara>> = _camaras

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarCamaras()
    }

    fun cargarCamaras() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _camaras.value = repository.getCamaras()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las cámaras. Revisa tu conexión."
            } finally {
                _cargando.value = false
            }
        }
    }
}