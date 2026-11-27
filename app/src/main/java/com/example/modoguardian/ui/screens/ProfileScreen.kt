@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.modoguardian.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.modoguardian.ui.theme.BordeSutil
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel
import com.example.modoguardian.viewmodel.PerfilViewModel
import com.example.modoguardian.viewmodel.TemaViewModel
import java.io.File

@Composable
fun ProfileScreen(
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    perfilViewModel: PerfilViewModel = viewModel(),
    temaViewModel: TemaViewModel = viewModel(),
    onAbrirHistorial: () -> Unit = {}
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
            onAbrirHistorial = onAbrirHistorial,
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
    onAbrirHistorial: () -> Unit,
    onCambiarTema: (ModoTema) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val nombre = email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "Guardián" }
    val primario = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = "Perfil", style = MaterialTheme.typography.headlineMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, primario.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(primario.copy(alpha = 0.16f), Color.Transparent),
                                radius = 420f
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, primario.copy(alpha = 0.35f), CircleShape)
                                .padding(4.dp)
                        ) {
                            ImagenInteligente(
                                imagenUri = imagenUri,
                                tamano = 104.dp,
                                modifier = Modifier.border(2.5.dp, primario, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = nombre, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, primario.copy(alpha = 0.35f))
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
            }

            TituloSeccion(texto = "MIS PERMISOS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
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
                                tint = primario,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = modulo, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (rol.puedeGestionarEventos) {
                TituloSeccion(texto = "TRAZABILIDAD")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BordeSutil)
                ) {
                    FilaAccion(
                        icono = Icons.Filled.History,
                        titulo = "Historial de acciones",
                        subtitulo = "Quién hizo qué sobre cada evento",
                        onClick = onAbrirHistorial
                    )
                }
            }

            TituloSeccion(texto = "FOTO DE PERFIL")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
                Column {
                    FilaAccion(
                        icono = Icons.Filled.PhotoLibrary,
                        titulo = "Seleccionar de la Galería",
                        subtitulo = "Elige una imagen que ya tengas",
                        onClick = onGaleria
                    )
                    HorizontalDivider(color = BordeSutil)
                    FilaAccion(
                        icono = Icons.Filled.PhotoCamera,
                        titulo = "Tomar Foto con Cámara",
                        subtitulo = "Se te pedirá permiso la primera vez",
                        onClick = onCamara
                    )
                }
            }

            TituloSeccion(texto = "APARIENCIA")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    activeBorderColor = primario,
                                    inactiveBorderColor = BordeSutil
                                ),
                                label = { Text(text = modo.etiqueta) }
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.error
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
                Text(text = "Cerrar sesión", style = MaterialTheme.typography.labelLarge)
            }

            Text(
                text = "ModoGuardian · versión 1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
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
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = titulo, style = MaterialTheme.typography.titleSmall) },
        supportingContent = {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(name = "Perfil Admin", widthDp = 360, heightDp = 1000, showBackground = true)
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
            onAbrirHistorial = {},
            onCambiarTema = {},
            onCerrarSesion = {}
        )
    }
}
