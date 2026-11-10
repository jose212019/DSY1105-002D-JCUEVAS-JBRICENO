package com.example.modoguardian.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.modoguardian.ui.components.ImagenInteligente
import com.example.modoguardian.viewmodel.MainViewModel
import com.example.modoguardian.viewmodel.PerfilViewModel
import java.io.File

// Pantalla de perfil: elige foto de la galería o toma una con la cámara (pide permiso antes)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: MainViewModel,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val context = LocalContext.current
    val imagenUri by perfilViewModel.imagenUri.collectAsState()
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

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            ImagenInteligente(imagenUri = imagenUri)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { launcherGaleria.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text("Seleccionar de la Galería", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
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
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text("Tomar Foto con Cámara", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}