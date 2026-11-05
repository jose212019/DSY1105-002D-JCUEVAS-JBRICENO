package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.model.Post
import com.example.modoguardian.repository.PostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Mantiene el estado de los posts obtenidos desde la API: lista, carga y error
class PostViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _postList.value = repository.getPosts()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar los posts. Revisa tu conexión."
            } finally {
                _isLoading.value = false
            }
        }
    }
}