package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.modoguardian.model.ModoTema
import com.example.modoguardian.model.Severidad
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.components.BarraNavegacion
import com.example.modoguardian.ui.screens.AlertaDetalleScreen
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
import com.example.modoguardian.viewmodel.TemaViewModel

// Actividad principal: tema elegido por el usuario, navegación con barra inferior y burbuja de alertas críticas
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val temaViewModel: TemaViewModel = viewModel()
            val modoTema by temaViewModel.modo.collectAsState()
            val oscuro = when (modoTema) {
                ModoTema.AUTOMATICO -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }

            DisposableEffect(oscuro) {
                this@MainActivity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { oscuro },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { oscuro }
                )
                onDispose { }
            }

            ModoGuardianTheme(darkTheme = oscuro) {
                val navController = rememberNavController()

                val mainViewModel: MainViewModel = viewModel()
                val loginViewModel: LoginViewModel = viewModel()
                val perfilViewModel: PerfilViewModel = viewModel()
                val postViewModel: PostViewModel = viewModel()

                val eventos by mainViewModel.events.collectAsState()
                val alertasPendientes = eventos.count { it.severidad == Severidad.ALTA && !it.isResolved }

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
                                alertasPendientes = alertasPendientes,
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
                        composable(Screen.Events.route) {
                            EventosScreen(
                                loginViewModel = loginViewModel,
                                mainViewModel = mainViewModel
                            )
                        }
                        composable(Screen.Alerts.route) {
                            AlertasScreen(
                                mainViewModel = mainViewModel,
                                onAbrirAlerta = { id ->
                                    navController.navigate(Screen.AlertDetail.crearRuta(id))
                                }
                            )
                        }
                        composable(
                            route = Screen.AlertDetail.route,
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { entrada ->
                            AlertaDetalleScreen(
                                idAlerta = entrada.arguments?.getString("id"),
                                loginViewModel = loginViewModel,
                                mainViewModel = mainViewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                loginViewModel = loginViewModel,
                                mainViewModel = mainViewModel,
                                perfilViewModel = perfilViewModel,
                                temaViewModel = temaViewModel
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