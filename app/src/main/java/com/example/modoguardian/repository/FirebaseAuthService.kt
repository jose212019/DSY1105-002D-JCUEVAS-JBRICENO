package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoAuth
import com.example.modoguardian.model.Rol
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

// Autenticación con Firebase: valida correo y contraseña y lee el rol desde la colección usuarios de Firestore
class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthService {

    override suspend fun iniciarSesion(email: String, password: String): ResultadoAuth {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            val documento = db.collection("usuarios").document(email).get().await()
            val rol = Rol.entries.firstOrNull { it.name == documento.getString("rol") }
            val activo = documento.getBoolean("activo") ?: true

            if (!documento.exists() || rol == null || !activo) {
                // Usuario sin rol asignado o desactivado: se cierra la sesión y se niega el acceso
                auth.signOut()
                ResultadoAuth.CredencialesInvalidas
            } else {
                ResultadoAuth.Exito(rol)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidUserException) {
            ResultadoAuth.CredencialesInvalidas
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResultadoAuth.CredencialesInvalidas
        } catch (e: FirebaseNetworkException) {
            ResultadoAuth.SinConexion
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                ResultadoAuth.SinConexion
            } else {
                ResultadoAuth.ErrorInesperado
            }
        } catch (e: Exception) {
            ResultadoAuth.ErrorInesperado
        }
    }

    override fun cerrarSesion() {
        auth.signOut()
    }
}