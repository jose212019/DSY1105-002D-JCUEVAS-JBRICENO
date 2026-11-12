package com.example.modoguardian.data.remote

import com.example.modoguardian.data.model.CamaraDto
import com.example.modoguardian.data.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Define los endpoints HTTP de la API: posts (GET, POST, PUT y DELETE) y cámaras (GET)
interface ApiService {

    @GET("/posts")
    suspend fun getPosts(): List<Post>

    @POST("/posts")
    suspend fun createPost(@Body post: Post): Post

    @PUT("/posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: Post): Post

    @DELETE("/posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>

    @GET("/camaras")
    suspend fun getCamaras(): List<CamaraDto>
}