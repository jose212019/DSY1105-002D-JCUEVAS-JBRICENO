@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.modoguardian.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modoguardian.model.ModoTema
import com.example.modoguardian.model.Rol
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.components.ImagenInteligente
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel
import com.example.modoguardian.viewmodel.PerfilViewModel
import com.example.modoguardian.viewmodel.TemaViewModel
import java.io.File

// Perfil conectado a los ViewModels: maneja la foto (galería o cámara con permiso), el tema y el cierre de sesión
@Composable
fun ProfileScreen(
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    perfilViewModel: PerfilViewModel = viewModel(),
    temaViewModel: TemaViewModel = viewModel()
) {
    val context = LocalContext.current
    val estadoLogin by loginViewModel.estado.collectAsState()
    val imagenUri by perfilViewModel.imagenUri.collectAsState()
    val modoTema by temaViewModel.modo.collectAsState()
    var uriCamaraTemporal by remember { mutableStateOf<Uri?>(null) }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) perfilViewModel.actualizarImagen(uri)
    }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && uriCamaraTemporal != null) {
            perfilViewModel.actualizarImagen(uriCamaraTemporal)
        }
    }

    val abrirCamara = {
        val archivoTemporal = File.createTempFile("foto_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            archivoTemporal
        )
        uriCamaraTemporal = uri
        launcherCamara.launch(uri)
    }

    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    val rol = estadoLogin.rol

    if (rol == null) {
        LaunchedEffect(Unit) {
            mainViewModel.navigateTo(Screen.Login, limpiarPila = true)
        }
    } else {
        PerfilContenido(
            rol = rol,
            email = estadoLogin.email,
            imagenUri = imagenUri,
            modoTema = modoTema,
            onGaleria = { launcherGaleria.launch("image/*") },
            onCamara = {
                val tienePermiso = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (tienePermiso) {
                    abrirCamara()
                } else {
                    launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                }
            },
            onCambiarTema = temaViewModel::cambiarModo,
            onCerrarSesion = loginViewModel::cerrarSesion
        )
    }
}

@Composable
fun PerfilContenido(
    rol: Rol,
    email: String,
    imagenUri: Uri?,
    modoTema: ModoTema,
    onGaleria: () -> Unit,
    onCamara: () -> Unit,
    onCambiarTema: (ModoTema) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val nombre = email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "Guardián" }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ImagenInteligente(
                        imagenUri = imagenUri,
                        tamano = 112.dp,
                        modifier = Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = nombre, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Rol: ${rol.etiqueta}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            TituloSeccion(texto = "MIS PERMISOS")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = rol.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    rol.modulos.forEach { modulo ->
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = modulo, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            TituloSeccion(texto = "FOTO DE PERFIL")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    FilaAccion(
                        icono = Icons.Filled.PhotoLibrary,
                        titulo = "Seleccionar de la Galería",
                        subtitulo = "Elige una imagen que ya tengas",
                        onClick = onGaleria
                    )
                    FilaAccion(
                        icono = Icons.Filled.PhotoCamera,
                        titulo = "Tomar Foto con Cámara",
                        subtitulo = "Se te pedirá permiso la primera vez",
                        onClick = onCamara
                    )
                }
            }

            TituloSeccion(texto = "APARIENCIA")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val modos = ModoTema.entries
                        modos.forEachIndexed { indice, modo ->
                            SegmentedButton(
                                selected = modo == modoTema,
                                onClick = { onCambiarTema(modo) },
                                shape = SegmentedButtonDefaults.itemShape(index = indice, count = modos.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    activeBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                label = { Text(text = modo.etiqueta) }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cerrar sesión")
            }

            Text(
                text = "ModoGuardian · versión 1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FilaAccion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = titulo) },
        supportingContent = { Text(text = subtitulo) },
        leadingContent = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(name = "Perfil Admin", widthDp = 360, heightDp = 900, showBackground = true)
@Composable
private fun PerfilPreview() {
    ModoGuardianTheme {
        PerfilContenido(
            rol = Rol.ADMIN,
            email = "admin@guardian.test",
            imagenUri = null,
            modoTema = ModoTema.OSCURO,
            onGaleria = {},
            onCamara = {},
            onCambiarTema = {},
            onCerrarSesion = {}
        )
    }
}