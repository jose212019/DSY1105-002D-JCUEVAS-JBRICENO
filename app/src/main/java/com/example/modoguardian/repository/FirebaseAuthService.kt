package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth
import com.example.modoguardian.model.Rol
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

// Autenticación con Firebase (correo y Google): el rol de cada persona se lee o se crea en la colección usuarios de Firestore
class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthService {

    override suspend fun iniciarSesion(email: String, password: String): ResultadoAuth {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            resolverAcceso(email = email, nombre = null, crearSiNoExiste = false)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirError(e)
        }
    }

    override suspend fun iniciarSesionConGoogle(idToken: String): ResultadoAuth {
        return try {
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            val usuario = auth.signInWithCredential(credencial).await().user
            val email = usuario?.email?.lowercase()

            if (email == null) {
                auth.signOut()
                ResultadoAuth.ErrorInesperado
            } else {
                resolverAcceso(email = email, nombre = usuario.displayName, crearSiNoExiste = true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirError(e)
        }
    }

    override fun cerrarSesion() {
        auth.signOut()
    }

    // Busca el rol de la persona; si no existe y se permite, la crea como Operador (el rol con menos permisos)
    private suspend fun resolverAcceso(
        email: String,
        nombre: String?,
        crearSiNoExiste: Boolean
    ): ResultadoAuth {
        val referencia = db.collection("usuarios").document(email)
        val documento = referencia.get().await()

        if (!documento.exists()) {
            if (!crearSiNoExiste) {
                auth.signOut()
                return ResultadoAuth.CredencialesInvalidas
            }
            referencia.set(
                mapOf(
                    "rol" to Rol.OPERADOR.name,
                    "nombre" to (nombre ?: ""),
                    "activo" to true
                )
            ).await()
            return ResultadoAuth.Exito(Rol.OPERADOR, email)
        }

        val rol = Rol.entries.firstOrNull { it.name == documento.getString("rol") }
        val activo = documento.getBoolean("activo") ?: true

        if (rol == null || !activo) {
            auth.signOut()
            return ResultadoAuth.CredencialesInvalidas
        }
        return ResultadoAuth.Exito(rol, email)
    }

    private fun traducirError(e: Exception): ResultadoAuth {
        return when (e) {
            is FirebaseAuthInvalidUserException -> ResultadoAuth.CredencialesInvalidas
            is FirebaseAuthInvalidCredentialsException -> ResultadoAuth.CredencialesInvalidas
            is FirebaseNetworkException -> ResultadoAuth.SinConexion
            is FirebaseFirestoreException -> {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    ResultadoAuth.SinConexion
                } else {
                    ResultadoAuth.ErrorInesperado
                }
            }
            else -> ResultadoAuth.ErrorInesperado
        }
    }
}