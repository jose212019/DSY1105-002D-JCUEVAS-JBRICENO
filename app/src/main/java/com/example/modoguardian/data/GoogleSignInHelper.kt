package com.example.modoguardian.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CancellationException

// Resultado de pedirle una cuenta de Google al sistema
sealed class ResultadoGoogle {
    data class Token(val idToken: String) : ResultadoGoogle()
    data object Cancelado : ResultadoGoogle()
    data object SinCuentas : ResultadoGoogle()
    data object Error : ResultadoGoogle()
}

// Abre el selector de cuentas de Google con Credential Manager y entrega el token para Firebase
class GoogleSignInHelper(private val webClientId: String) {

    suspend fun obtenerIdToken(context: Context): ResultadoGoogle {
        val credentialManager = CredentialManager.create(context)
        val opcion = GetSignInWithGoogleOption.Builder(webClientId).build()
        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcion)
            .build()

        return try {
            val respuesta = credentialManager.getCredential(context, solicitud)
            val credencial = respuesta.credential

            if (credencial is CustomCredential &&
                credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                ResultadoGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
            } else {
                ResultadoGoogle.Error
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            ResultadoGoogle.Cancelado
        } catch (e: NoCredentialException) {
            ResultadoGoogle.SinCuentas
        } catch (e: GetCredentialException) {
            ResultadoGoogle.Error
        } catch (e: GoogleIdTokenParsingException) {
            ResultadoGoogle.Error
        }
    }
}