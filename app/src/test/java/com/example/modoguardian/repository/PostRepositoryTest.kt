package com.example.modoguardian.repository

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.data.remote.ApiService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import retrofit2.Response

// Pruebas de PostRepository con un ApiService simulado con MockK
class PostRepositoryTest : StringSpec({

    val fakePosts = listOf(
        Post(userId = 1, id = 1, title = "Título 1", body = "Cuerpo 1"),
        Post(userId = 2, id = 2, title = "Título 2", body = "Cuerpo 2")
    )

    "getPosts() debe retornar la lista de posts simulada" {
        val mockApi = mockk<ApiService>()
        coEvery { mockApi.getPosts() } returns fakePosts

        val repo = PostRepository(mockApi)

        repo.getPosts() shouldContainExactly fakePosts
    }

    "createPost() debe retornar el post creado por la API" {
        val nuevo = Post(userId = 1, id = 0, title = "Nuevo", body = "Contenido")
        val creado = nuevo.copy(id = 101)
        val mockApi = mockk<ApiService>()
        coEvery { mockApi.createPost(nuevo) } returns creado

        val repo = PostRepository(mockApi)

        repo.createPost(nuevo) shouldBe creado
    }

    "updatePost() debe enviar el id del post y retornar el actualizado" {
        val editado = Post(userId = 1, id = 5, title = "Editado", body = "Texto")
        val mockApi = mockk<ApiService>()
        coEvery { mockApi.updatePost(5, editado) } returns editado

        val repo = PostRepository(mockApi)

        repo.updatePost(editado) shouldBe editado
    }

    "deletePost() debe retornar true cuando la API responde con éxito" {
        val mockApi = mockk<ApiService>()
        coEvery { mockApi.deletePost(1) } returns Response.success(Unit)

        val repo = PostRepository(mockApi)

        repo.deletePost(1) shouldBe true
    }
})