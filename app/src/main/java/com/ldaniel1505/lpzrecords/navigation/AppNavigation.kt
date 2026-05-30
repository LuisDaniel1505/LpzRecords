package com.ldaniel1505.lpzrecords.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ldaniel1505.lpzrecords.ui.screens.MainScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.LoginScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.SignUpScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.CatalogScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.ProductDetailScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AccountScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.OrdersScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PersonalInfoScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AddressesScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PaymentMethodsScreen
import com.ldaniel1505.lpzrecords.ui.screens.cart.CartScreen
import com.ldaniel1505.lpzrecords.ui.screens.checkout.CheckoutScreen
import com.ldaniel1505.lpzrecords.ui.screens.favorites.FavoritesScreen
import com.ldaniel1505.lpzrecords.ui.screens.admin.ProductControlScreen
import com.ldaniel1505.lpzrecords.ui.screens.admin.AdminHostScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
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
                onLoginSuccess = { isAdmin ->
                    /*// TODO: Si isAdmin == true, redirigir a pantalla de administrador
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }*/
                    if (isAdmin) {
                        navController.navigate(Screen.AdminHost.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess   = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Catalog.route) {
            CatalogScreen(
                onNavigateToHome      = { /* Ya estamos en el catálogo */ },
                onNavigateToSearch    = { /* TODO: navegar a SearchScreen */ },
                onNavigateToCart      = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.navigate(Screen.Account.route) },
                onNavigateToProduct   = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        // ── Detalle del Producto ────────────────────────────────────────────
        composable(
            route     = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            ProductDetailScreen(
                productId             = productId,  // Descomentar cuando el ViewModel esté listo
                onNavigateBack        = { navController.popBackStack() },
                onNavigateToCart      = { navController.navigate(Screen.Cart.route) },
                onNavigateToHome      = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch    = { /* TODO: navegar a SearchScreen */ },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.navigate(Screen.Account.route) }
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                onNavigateToHome           = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch         = { /* TODO: navegar a SearchScreen */ },
                onNavigateToCart           = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToOrders         = { navController.navigate(Screen.Orders.route) },
                onNavigateToPersonalInfo   = { navController.navigate(Screen.PersonalInfo.route) },
                onNavigateToAddresses      = { navController.navigate(Screen.Addresses.route) },
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
                onNavigateToHome      = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.popBackStack() }
            )
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(
                onNavigateToHome      = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.popBackStack() }
            )
        }

        composable(Screen.Addresses.route) {
            AddressesScreen(
                onNavigateToHome      = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(
                onNavigateToHome      = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToCart      = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateBack        = { navController.popBackStack() },
                onNavigateToHome      = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch    = { /* TODO: SearchScreen */ },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile   = { navController.navigate(Screen.Account.route) },
                onNavigateToCheckout  = { navController.navigate(Screen.Checkout.route) }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack             = { navController.popBackStack() },
                onNavigateToHome           = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch         = { /* TODO: SearchScreen */ },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile        = { navController.navigate(Screen.Account.route) },
                onNavigateToAddresses      = { navController.navigate(Screen.Addresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onConfirmOrder             = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateToHome    = {
                    navController.navigate(Screen.Catalog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch  = { /* TODO: SearchScreen */ },
                onNavigateToCart    = { navController.navigate(Screen.Cart.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) }
            )
        }

        composable(Screen.ProductControl.route) {
            ProductControlScreen(
                onNavigateBack = { navController.popBackStack() },
                //onNavigateToAddProduct = { /* TODO: navegar a formulario de nuevo producto si lo separas */}
            )
        }

        composable(Screen.AdminHost.route) {
            AdminHostScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminHost.route) { inclusive = true }
                    }
                }
            )
        }
    }
}