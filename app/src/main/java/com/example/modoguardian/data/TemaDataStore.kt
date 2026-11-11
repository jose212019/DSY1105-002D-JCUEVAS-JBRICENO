package com.example.modoguardian.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.modoguardian.model.ModoTema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Guarda y lee el modo de apariencia elegido, por defecto Oscuro
class TemaDataStore(private val context: Context) {

    private val MODO_TEMA = stringPreferencesKey(name = "modo_tema")

    suspend fun guardarModo(modo: ModoTema) {
        context.dataStore.edit { preferencias ->
            preferencias[MODO_TEMA] = modo.name
        }
    }

    fun obtenerModo(): Flow<ModoTema> {
        return context.dataStore.data.map { preferencias ->
            val guardado = preferencias[MODO_TEMA]
            ModoTema.entries.firstOrNull { it.name == guardado } ?: ModoTema.OSCURO
        }
    }
}