package com.example.modoguardian.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class LoginDataStore(private val context: Context) {

    private val ultimoEmailKey = stringPreferencesKey(name = "ultimo_email")

    suspend fun guardarUltimoEmail(email: String) {
        context.dataStore.edit { preferencias ->
            preferencias[ultimoEmailKey] = email
        }
    }

    suspend fun limpiarUltimoEmail() {
        context.dataStore.edit { preferencias ->
            preferencias.remove(ultimoEmailKey)
        }
    }

    fun obtenerUltimoEmail(): Flow<String?> {
        return context.dataStore.data.map { preferencias ->
            preferencias[ultimoEmailKey]
        }
    }
}
