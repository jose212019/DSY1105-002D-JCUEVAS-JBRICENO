package com.example.modoguardian.ui.components

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

// Ventana para crear una cuenta nueva: valida los datos y los entrega a quien la usa
@Composable
fun DialogoRegistro(
    cargando: Boolean,
    errorGeneral: String?,
    onRegistrar: (nombre: String, email: String, password: String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!cargando) onCancelar() },
        title = { Text(text = "Crear cuenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorLocal = null
                    },
                    label = { Text(text = "Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorLocal = null
                    },
                    label = { Text(text = "Correo electr\u00f3nico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorLocal = null
                    },
                    label = { Text(text = "Contrase\u00f1a") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmacion,
                    onValueChange = {
                        confirmacion = it
                        errorLocal = null
                    },
                    label = { Text(text = "Confirmar contrase\u00f1a") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                val mensaje = errorLocal ?: errorGeneral
                if (mensaje != null) {
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !cargando,
                onClick = {
                    errorLocal = when {
                        nombre.isBlank() -> "Ingresa tu nombre"
                        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Ingresa un correo v\u00e1lido"
                        password.length < 6 -> "La contrase\u00f1a debe tener al menos 6 caracteres"
                        password != confirmacion -> "Las contrase\u00f1as no coinciden"
                        else -> null
                    }
                    if (errorLocal == null) onRegistrar(nombre.trim(), email.trim(), password)
                }
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "Crear cuenta")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !cargando, onClick = onCancelar) {
                Text(text = "Cancelar")
            }
        }
    )
}
