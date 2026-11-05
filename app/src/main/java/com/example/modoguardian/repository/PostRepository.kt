package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.RetrofitInstance

// Accede a los datos de la API usando Retrofit
class PostRepository {

    suspend fun getPosts(): List<Post> {
        return RetrofitInstance.api.getPosts()
    }
}