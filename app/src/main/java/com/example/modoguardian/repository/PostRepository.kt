package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.RetrofitInstance

// Accede a los datos de la API usando Retrofit
class PostRepository {

    suspend fun getPosts(): List<Post> {
        return RetrofitInstance.api.getPosts()
    }

    suspend fun createPost(post: Post): Post {
        return RetrofitInstance.api.createPost(post)
    }

    suspend fun updatePost(post: Post): Post {
        return RetrofitInstance.api.updatePost(post.id, post)
    }

    suspend fun deletePost(id: Int): Boolean {
        return RetrofitInstance.api.deletePost(id).isSuccessful
    }
}