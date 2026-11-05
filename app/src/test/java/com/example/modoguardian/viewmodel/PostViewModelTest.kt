package com.example.modoguardian.viewmodel

import com.example.modoguardian.data.model.Post
import com.example.modoguardian.repository.PostRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Pruebas de PostViewModel con un PostRepository simulado con MockK
@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest : StringSpec({

    val fakePosts = listOf(
        Post(userId = 1, id = 1, title = "Título 1", body = "Contenido 1"),
        Post(userId = 2, id = 2, title = "Título 2", body = "Contenido 2")
    )

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "postList debe contener los datos esperados después de fetchPosts()" {
        val repo = mockk<PostRepository>()
        coEvery { repo.getPosts() } returns fakePosts

        val viewModel = PostViewModel(repo)

        viewModel.postList.value shouldContainExactly fakePosts
        viewModel.isLoading.value shouldBe false
        viewModel.error.value.shouldBeNull()
    }

    "debe publicar un mensaje de error si la API falla" {
        val repo = mockk<PostRepository>()
        coEvery { repo.getPosts() } throws RuntimeException("sin red")

        val viewModel = PostViewModel(repo)

        viewModel.postList.value.shouldBeEmpty()
        viewModel.error.value shouldBe "No se pudieron cargar los posts. Revisa tu conexión."
        viewModel.isLoading.value shouldBe false
    }

    "fetchPosts() debe limpiar el error al reintentar con éxito" {
        val repo = mockk<PostRepository>()
        coEvery { repo.getPosts() } throws RuntimeException("sin red") andThen fakePosts

        val viewModel = PostViewModel(repo)
        viewModel.error.value shouldBe "No se pudieron cargar los posts. Revisa tu conexión."

        viewModel.fetchPosts()

        viewModel.error.value.shouldBeNull()
        viewModel.postList.value shouldContainExactly fakePosts
    }
})