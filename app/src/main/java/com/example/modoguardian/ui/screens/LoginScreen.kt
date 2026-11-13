@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.example.modoguardian.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import com.example.modoguardian.R
import com.example.modoguardian.data.GoogleSignInHelper
import com.example.modoguardian.data.ResultadoGoogle
import kotlinx.coroutines.launch
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel
) {
    val estado by loginViewModel.estado.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val iniciarGoogle: () -> Unit = {
        scope.launch {
            val ayudante = GoogleSignInHelper(context.getString(R.string.default_web_client_id))
            when (val resultado = ayudante.obtenerIdToken(context)) {
                is ResultadoGoogle.Token -> loginViewModel.iniciarSesionConGoogle(resultado.idToken) {
                    mainViewModel.navigateTo(Screen.Home, limpiarPila = true)
                }
                ResultadoGoogle.Cancelado -> Unit
                ResultadoGoogle.SinCuentas -> Toast.makeText(context, "No hay cuentas de Google en este dispositivo", Toast.LENGTH_SHORT).show()
                ResultadoGoogle.Error -> Toast.makeText(context, "No se pudo iniciar con Google. Inténtalo de nuevo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LoginContenido(
        estado = estado,
        widthSizeClass = windowSizeClass.widthSizeClass,
        onEmailChange = loginViewModel::onEmailChange,
        onPasswordChange = loginViewModel::onPasswordChange,
        onSimularSinConexionChange = loginViewModel::onSimularSinConexionChange,
        onIniciarSesion = {
            loginViewModel.iniciarSesion {
                mainViewModel.navigateTo(Screen.Home, limpiarPila = true)
            }
        },
        onGoogle = {
            iniciarGoogle()
        },
        onCrearCuenta = {
            Toast.makeText(context, "El registro de cuentas estará disponible pronto", Toast.LENGTH_SHORT).show()
        }
    )
}

// Pantalla de login adaptable: celular en una columna, tablet y horizontal en una tarjeta con dos columnas
@Composable
fun LoginContenido(
    estado: LoginUiState,
    widthSizeClass: WindowWidthSizeClass,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSimularSinConexionChange: (Boolean) -> Unit,
    onIniciarSesion: () -> Unit,
    onGoogle: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EncabezadoLogin(tamanoLogo = 96.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                    FormularioLogin(
                        estado = estado,
                        onEmailChange = onEmailChange,
                        onPasswordChange = onPasswordChange,
                        onSimularSinConexionChange = onSimularSinConexionChange,
                        onIniciarSesion = onIniciarSesion,
                        onGoogle = onGoogle,
                        onCrearCuenta = onCrearCuenta
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .widthIn(max = 900.dp)
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EncabezadoLogin(
                            tamanoLogo = 140.dp,
                            modifier = Modifier.weight(1f)
                        )
                        FormularioLogin(
                            estado = estado,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onSimularSinConexionChange = onSimularSinConexionChange,
                            onIniciarSesion = onIniciarSesion,
                            onGoogle = onGoogle,
                            onCrearCuenta = onCrearCuenta,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoConPulso(tamano: Dp) {
    val transicion = rememberInfiniteTransition(label = "pulso")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "escala"
    )
    val opacidad by transicion.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "opacidad"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(tamano * 1.5f)
    ) {
        Box(
            modifier = Modifier
                .size(tamano)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    alpha = opacidad
                }
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(tamano)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(6.dp)
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de ModoGuardian",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EncabezadoLogin(
    tamanoLogo: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoConPulso(tamano = tamanoLogo)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "MODOGUARDIAN",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Bienvenido, guardián",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Inicia sesión para activar tu turno",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface
)

@Composable
private fun FormularioLogin(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSimularSinConexionChange: (Boolean) -> Unit,
    onIniciarSesion: () -> Unit,
    onGoogle: () -> Unit,
    onCrearCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    var verPassword by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = estado.email,
            onValueChange = onEmailChange,
            label = { Text(text = "Correo electrónico") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            enabled = !estado.isLoading,
            isError = estado.errores.email != null,
            supportingText = {
                estado.errores.email?.let { Text(text = it) }
            },
            shape = MaterialTheme.shapes.small,
            colors = coloresCampo(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.password,
            onValueChange = onPasswordChange,
            label = { Text(text = "Contraseña") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        imageVector = if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            enabled = !estado.isLoading,
            isError = estado.errores.password != null,
            supportingText = {
                estado.errores.password?.let { Text(text = it) }
            },
            visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
            shape = MaterialTheme.shapes.small,
            colors = coloresCampo(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        estado.errores.general?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = estado.simularSinConexion,
                onCheckedChange = onSimularSinConexionChange,
                enabled = !estado.isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simular sin conexión (demo)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onIniciarSesion,
            enabled = !estado.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (estado.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Ingresando...")
            } else {
                Text(text = "Iniciar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "O continuar con",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        OutlinedButton(
            onClick = onGoogle,
            enabled = !estado.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Continuar con Google")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿No tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onCrearCuenta) {
                Text(text = "Crear cuenta")
            }
        }
    }
}

@Preview(name = "Login Compact", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LoginCompactPreview() {
    ModoGuardianTheme {
        LoginContenido(
            estado = LoginUiState(),
            widthSizeClass = WindowWidthSizeClass.Compact,
            onEmailChange = {},
            onPasswordChange = {},
            onSimularSinConexionChange = {},
            onIniciarSesion = {},
            onGoogle = {},
            onCrearCuenta = {}
        )
    }
}

@Preview(name = "Login Medium", widthDp = 700, heightDp = 900, showBackground = true)
@Composable
private fun LoginMediumPreview() {
    ModoGuardianTheme {
        LoginContenido(
            estado = LoginUiState(
                email = "correo-invalido",
                errores = LoginErrores(email = "Ingresa un correo válido")
            ),
            widthSizeClass = WindowWidthSizeClass.Medium,
            onEmailChange = {},
            onPasswordChange = {},
            onSimularSinConexionChange = {},
            onIniciarSesion = {},
            onGoogle = {},
            onCrearCuenta = {}
        )
    }
}

@Preview(name = "Login Expanded", widthDp = 1100, heightDp = 800, showBackground = true)
@Composable
private fun LoginExpandedPreview() {
    ModoGuardianTheme {
        LoginContenido(
            estado = LoginUiState(
                email = "admin@guardian.test",
                password = "123456",
                isLoading = true
            ),
            widthSizeClass = WindowWidthSizeClass.Expanded,
            onEmailChange = {},
            onPasswordChange = {},
            onSimularSinConexionChange = {},
            onIniciarSesion = {},
            onGoogle = {},
            onCrearCuenta = {}
        )
    }
}