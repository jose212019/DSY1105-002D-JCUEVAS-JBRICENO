package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.ApiService
import com.example.modoguardian.data.remote.RetrofitInstance

// Accede a los datos de la API usando Retrofit, recibe el ApiService para poder testearlo
class PostRepository(private val api: ApiService = RetrofitInstance.api) {

    suspend fun getPosts(): List<Post> {
        return api.getPosts()
    }

    suspend fun createPost(post: Post): Post {
        return api.createPost(post)
    }

    suspend fun updatePost(post: Post): Post {
        return api.updatePost(post.id, post)
    }

    suspend fun deletePost(id: Int): Boolean {
        return api.deletePost(id).isSuccessful
    }
}