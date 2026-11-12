package com.example.modoguardian.navigation

sealed class Screen(val route: String) {
    data object Login : Screen(route = "login")
    data object Home : Screen(route = "home_page")
    data object Cameras : Screen(route = "cameras_page")
    data object Events : Screen(route = "events_page")
    data object Alerts : Screen(route = "alerts_page")
    data object Profile : Screen(route = "profile_page")
    data object Settings : Screen(route = "settings_page")
    data object Posts : Screen(route = "posts_page")

    data object AlertDetail : Screen(route = "alert_detail/{id}") {
        fun crearRuta(id: String): String = "alert_detail/$id"
    }

    data object CameraDetail : Screen(route = "camera_detail/{id}") {
        fun crearRuta(id: String): String = "camera_detail/$id"
    }
}