package com.example.modoguardian.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton con la configuración de Retrofit apuntando a la API propia de ModoGuardian en MockAPI
object RetrofitInstance {

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://6af4c42e55a1de7dcf046d83.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}