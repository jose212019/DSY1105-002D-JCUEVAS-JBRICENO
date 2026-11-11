package com.example.modoguardian.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.TemaDataStore
import com.example.modoguardian.model.ModoTema
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Expone el modo de apariencia y lo guarda cuando el usuario lo cambia
class TemaViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = TemaDataStore(application)

    val modo: StateFlow<ModoTema> = dataStore.obtenerModo()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ModoTema.OSCURO)

    fun cambiarModo(nuevo: ModoTema) {
        viewModelScope.launch {
            dataStore.guardarModo(nuevo)
        }
    }
}