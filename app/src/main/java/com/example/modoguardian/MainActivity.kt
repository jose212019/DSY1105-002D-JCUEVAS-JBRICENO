package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.components.BarraNavegacion
import com.example.modoguardian.ui.screens.AlertasScreen
import com.example.modoguardian.ui.screens.CamarasScreen
import com.example.modoguardian.ui.screens.EventosScreen
import com.example.modoguardian.ui.screens.HomeScreen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.screens.PostScreen
import com.example.modoguardian.ui.screens.ProfileScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel
import com.example.modoguardian.viewmodel.PerfilViewModel
import com.example.modoguardian.viewmodel.PostViewModel

// Actividad principal: navegación con barra inferior de cinco pestañas y ViewModels compartidos
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            ModoGuardianTheme {
                val navController = rememberNavController()

                val mainViewModel: MainViewModel = viewModel()
                val loginViewModel: LoginViewModel = viewModel()
                val perfilViewModel: PerfilViewModel = viewModel()
                val postViewModel: PostViewModel = viewModel()

                val entradaActual by navController.currentBackStackEntryAsState()
                val rutaActual = entradaActual?.destination?.route
                val rutasConBarra = listOf(
                    Screen.Home.route,
                    Screen.Cameras.route,
                    Screen.Events.route,
                    Screen.Alerts.route,
                    Screen.Profile.route
                )

                LaunchedEffect(Unit) {
                    mainViewModel.navigationEvent.collect { evento ->
                        when (evento) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(evento.destination.route) {
                                    if (evento.limpiarPila) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                    }
                                    launchSingleTop = true
                                }
                            }

                            NavigationEvent.NavigateBack -> {
                                navController.popBackStack()
                            }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (rutaActual in rutasConBarra) {
                            BarraNavegacion(
                                rutaActual = rutaActual,
                                onSeleccionar = { destino ->
                                    navController.navigate(destino.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                loginViewModel = loginViewModel,
                                mainViewModel = mainViewModel
                            )
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(
                                loginViewModel = loginViewModel,
                                mainViewModel = mainViewModel
                            )
                        }
                        composable(Screen.Cameras.route) { CamarasScreen() }
                        composable(Screen.Events.route) { EventosScreen() }
                        composable(Screen.Alerts.route) { AlertasScreen() }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                navController = navController,
                                viewModel = mainViewModel,
                                perfilViewModel = perfilViewModel
                            )
                        }
                        composable(Screen.Posts.route) {
                            PostScreen(viewModel = postViewModel)
                        }
                    }
                }
            }
        }
    }
}