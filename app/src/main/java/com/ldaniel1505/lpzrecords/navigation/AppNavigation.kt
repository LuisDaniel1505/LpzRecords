package com.ldaniel1505.lpzrecords.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ldaniel1505.lpzrecords.ui.screens.MainScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.LoginScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.SignUpScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.CatalogScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AccountScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.OrdersScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PersonalInfoScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AddressesScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PaymentMethodsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToStore = { /* TODO: Navegar a Home/Store */ }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Catalog.route) {
            CatalogScreen(
                onNavigateToHome      = { /* TODO: navegar a HomeScreen */ },
                onNavigateToSearch    = { /* TODO: navegar a SearchScreen */ },
                onNavigateToCart      = { /* TODO: navegar a CartScreen */ },
                onNavigateToFavorites = { /* TODO: navegar a FavoritesScreen */ },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                        onNavigateToProduct   = { productId -> /* TODO: Screen.ProductDetail(productId) */ }
            )
        }
        composable(Screen.Account.route) {
            AccountScreen(
                onNavigateToHome         = { navController.navigate(Screen.Catalog.route) },
                onNavigateToSearch       = { /* TODO: navegar a SearchScreen */ },
                onNavigateToCart         = { /* TODO: navegar a CartScreen */ },
                onNavigateToFavorites    = { /* TODO: navegar a FavoritesScreen */ },
                onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                onNavigateToPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Orders.route) {
            OrdersScreen(
                onNavigateToHome      = { navController.navigate(Screen.Catalog.route) },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { /* TODO: CartScreen */ },
                onNavigateToFavorites = { /* TODO: FavoritesScreen */ },
                onNavigateToProfile   = { navController.popBackStack() }
            )
        }
        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(
                onNavigateToHome      = { navController.navigate(Screen.Catalog.route) },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { /* TODO: CartScreen */ },
                onNavigateToFavorites = { /* TODO: FavoritesScreen */ },
                onNavigateToProfile   = { navController.popBackStack() } // vuelve a AccountScreen
            )
        }
        composable(Screen.Addresses.route) {
            AddressesScreen(
                onNavigateToHome      = { navController.navigate(Screen.Catalog.route) },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { /* TODO: CartScreen */ },
                onNavigateToFavorites = { /* TODO: FavoritesScreen */ },
                onNavigateToProfile   = { navController.popBackStack() } // vuelve a AccountScreen
            )
        }
        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(
                onNavigateToHome      = { navController.navigate(Screen.Catalog.route) },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { /* TODO: CartScreen */ },
                onNavigateToFavorites = { /* TODO: FavoritesScreen */ },
                onNavigateToProfile   = { navController.popBackStack() } // vuelve a AccountScreen
            )
        }
    }
}