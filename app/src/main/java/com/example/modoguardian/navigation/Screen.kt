package com.example.modoguardian.navigation

sealed class Screen(val route: String) {
    data object Login : Screen(route = "login")
    data object Home : Screen(route = "home_page")
    data object Profile : Screen(route = "profile_page")
    data object Settings : Screen(route = "settings_page")
    data object Posts : Screen(route = "posts_page")
}