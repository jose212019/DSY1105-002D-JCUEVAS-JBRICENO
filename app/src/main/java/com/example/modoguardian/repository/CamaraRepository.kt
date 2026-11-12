package com.example.modoguardian.repository

import com.example.modoguardian.data.model.toCamara
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.data.remote.RetrofitInstance
import com.example.modoguardian.model.Camara

// Cámaras obtenidas desde la API REST, recibe el ApiService para poder testearlo
class CamaraRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun getCamaras(): List<Camara> {
        return api.getCamaras().map { it.toCamara() }
    }
}