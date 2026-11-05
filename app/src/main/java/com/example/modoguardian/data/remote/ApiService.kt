package com.example.modoguardian.data.remote

import com.example.modoguardian.data.model.Post
import retrofit2.http.GET

// Define los endpoints HTTP de la API
interface ApiService {

    @GET("/posts")
    suspend fun getPosts(): List<Post>
}