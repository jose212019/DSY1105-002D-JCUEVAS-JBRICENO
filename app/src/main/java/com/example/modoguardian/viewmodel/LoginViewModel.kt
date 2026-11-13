package com.example.modoguardian.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.LoginDataStore
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.ResultadoAuth
import com.example.modoguardian.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val loginDataStore = LoginDataStore(application.applicationContext)

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    init {
        //cargarUltimoEmail()
    }


    private fun cargarUltimoEmail() {
        viewModelScope.launch {
            try {
                val guardado = loginDataStore.obtenerUltimoEmail().first()
                if (!guardado.isNullOrBlank() && _estado.value.email.isBlank()) {
                    _estado.update { it.copy(email = guardado) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

            }
        }
    }

    fun onEmailChange(nuevoEmail: String) {

        _estado.update {
            it.copy(
                email = nuevoEmail,
                errores = it.errores.copy(email = null, general = null)
            )
        }
    }

    fun onPasswordChange(nuevaPassword: String) {
        _estado.update {
            it.copy(
                password = nuevaPassword,
                errores = it.errores.copy(password = null, general = null)
            )
        }
    }

    fun onSimularSinConexionChange(valor: Boolean) {
        _estado.update { it.copy(simularSinConexion = valor) }
    }

    // Valida los campos y publica los mensajes de error. Devuelve true si todo es válido.
    fun validarFormulario(): Boolean {
        val actual = _estado.value

        val errorEmail = when {
            actual.email.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(actual.email.trim()).matches() -> "Ingresa un correo válido"
            else -> null
        }
        val errorPassword = when {
            actual.password.isBlank() -> "La contraseña es obligatoria"
            actual.password.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }

        _estado.update {
            it.copy(errores = LoginErrores(email = errorEmail, password = errorPassword))
        }
        return errorEmail == null && errorPassword == null
    }

    fun iniciarSesion(onExito: () -> Unit) {
        if (_estado.value.isLoading) return
        if (!validarFormulario()) return

        val actual = _estado.value
        _estado.update { it.copy(isLoading = true, errores = LoginErrores()) }

        viewModelScope.launch {

            val resultado: ResultadoAuth = try {
                authRepository.autenticar(
                    email = actual.email,
                    password = actual.password,
                    simularSinConexion = actual.simularSinConexion
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ResultadoAuth.ErrorInesperado
            }

            when (resultado) {
                is ResultadoAuth.Exito -> {
                    _estado.update {
                        it.copy(isLoading = false, rol = resultado.rol, password = "")
                    }
                    guardarEmailSinFallar(actual.email.trim())
                    onExito()
                }

                ResultadoAuth.CredencialesInvalidas -> mostrarErrorGeneral(
                    "Correo o contraseña incorrectos"
                )

                ResultadoAuth.SinConexion -> mostrarErrorGeneral(
                    "Sin conexión. Revisa tu red e inténtalo de nuevo"
                )

                ResultadoAuth.ErrorInesperado -> mostrarErrorGeneral(
                    "Ocurrió un error inesperado. Inténtalo nuevamente"
                )
            }
        }
    }

    private fun mostrarErrorGeneral(mensaje: String) {
        _estado.update {
            it.copy(isLoading = false, errores = LoginErrores(general = mensaje))
        }
    }

    private suspend fun guardarEmailSinFallar(email: String) {
        try {
            loginDataStore.guardarUltimoEmail(email)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {

        }
    }


    fun iniciarSesionConGoogle(idToken: String, onExito: () -> Unit) {
        if (_estado.value.isLoading) return
        _estado.update { it.copy(isLoading = true, errores = LoginErrores()) }

        viewModelScope.launch {
            val resultado: ResultadoAuth = try {
                authRepository.autenticarConGoogle(idToken)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ResultadoAuth.ErrorInesperado
            }

            when (resultado) {
                is ResultadoAuth.Exito -> {
                    _estado.update {
                        it.copy(
                            isLoading = false,
                            rol = resultado.rol,
                            email = resultado.email ?: it.email,
                            password = ""
                        )
                    }
                    onExito()
                }

                ResultadoAuth.CredencialesInvalidas -> mostrarErrorGeneral(
                    "Tu cuenta no tiene acceso a ModoGuardian"
                )

                ResultadoAuth.SinConexion -> mostrarErrorGeneral(
                    "Sin conexión. Revisa tu red e inténtalo de nuevo"
                )

                ResultadoAuth.ErrorInesperado -> mostrarErrorGeneral(
                    "Ocurrió un error inesperado. Inténtalo nuevamente"
                )
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _estado.update {
            it.copy(rol = null, password = "", isLoading = false, errores = LoginErrores())
        }
    }
}
