package com.ldaniel1505.lpzrecords.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    // Aquí iremos añadiendo las otras 27 pantallas...
}