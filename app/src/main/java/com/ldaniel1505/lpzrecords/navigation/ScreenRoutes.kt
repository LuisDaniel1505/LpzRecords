package com.ldaniel1505.lpzrecords.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object Catalog: Screen("catalog_screen")
    object Account : Screen("account_screen")
}