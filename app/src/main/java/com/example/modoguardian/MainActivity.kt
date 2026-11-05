package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.screens.HomeScreen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.screens.PostScreen
import com.example.modoguardian.ui.screens.ProfileScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.MainViewModel
import com.example.modoguardian.viewmodel.PerfilViewModel
import com.example.modoguardian.viewmodel.PostViewModel

// Actividad principal: navegación entre Login, Home, Perfil y Posts con ViewModels compartidos
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModoGuardianTheme {
                val navController = rememberNavController()

                val mainViewModel: MainViewModel = viewModel()
                val loginViewModel: LoginViewModel = viewModel()
                val perfilViewModel: PerfilViewModel = viewModel()
                val postViewModel: PostViewModel = viewModel()

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

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route,
                    modifier = Modifier.fillMaxSize()
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