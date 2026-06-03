package com.ldaniel1505.lpzrecords.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ldaniel1505.lpzrecords.ui.screens.MainScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AccountScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AddressesScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.OrdersScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PaymentMethodsScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PersonalInfoScreen
import com.ldaniel1505.lpzrecords.ui.screens.admin.AdminHostScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.LoginScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.SignUpScreen
import com.ldaniel1505.lpzrecords.ui.screens.cart.CartScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.CatalogScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.ProductDetailScreen
import com.ldaniel1505.lpzrecords.ui.screens.checkout.CheckoutScreen
import com.ldaniel1505.lpzrecords.ui.screens.favorites.FavoritesScreen
import com.ldaniel1505.lpzrecords.ui.screens.search.SearchScreen
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()

    fun navigateToCatalog() {
        navController.navigate(Screen.Catalog.route) {
            launchSingleTop = true
        }
    }

    fun navigateToSearch() {
        navController.navigate(Screen.Search.route) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToStore = { navigateToCatalog() }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = { isAdmin ->
                    if (isAdmin) {
                        navController.navigate(Screen.Admin.route) {
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

        composable(Screen.Admin.route) {
            AdminHostScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Admin.route) { inclusive = true }
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
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onAddToCart = { product -> cartViewModel.addProduct(product) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onAddToCart = { product -> cartViewModel.addProduct(product) },
                onBuyNow = { product ->
                    cartViewModel.addProductIfMissing(product)
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
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
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() }
            )
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() }
            )
        }

        composable(Screen.Addresses.route) {
            AddressesScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onConfirmOrder = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) }
            )
        }
    }
}
