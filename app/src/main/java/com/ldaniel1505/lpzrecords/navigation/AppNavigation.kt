package com.ldaniel1505.lpzrecords.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ldaniel1505.lpzrecords.ui.screens.MainScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AccountScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AddressFormScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.AddressesScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.OrdersScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PaymentMethodFormScreen
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
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel
import com.ldaniel1505.lpzrecords.viewmodel.checkout.CheckoutViewModel
import com.ldaniel1505.lpzrecords.viewmodel.favorites.FavoritesViewModel
import com.ldaniel1505.lpzrecords.viewmodel.orders.OrdersViewModel
import com.ldaniel1505.lpzrecords.viewmodel.profile.ProfileViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val checkoutViewModel: CheckoutViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val addressViewModel: AddressViewModel = viewModel()
    val paymentMethodsViewModel: PaymentMethodsViewModel = viewModel()
    val ordersViewModel: OrdersViewModel = viewModel()
    val favoriteProducts by favoritesViewModel.favoriteProducts.collectAsState()
    val favoriteProductIds = favoriteProducts.map { it.id }.toSet()

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

    fun clearSessionState() {
        cartViewModel.clearCart()
        favoritesViewModel.clearFavorites()
        checkoutViewModel.resetCheckoutState()
        profileViewModel.clearProfile()
        addressViewModel.clearState()
        paymentMethodsViewModel.clearState()
        ordersViewModel.clearState()
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
                profileViewModel = profileViewModel,
                onLogout = {
                    clearSessionState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onUnauthorized = {
                    clearSessionState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                        launchSingleTop = true
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
                favoriteProductIds = favoriteProductIds,
                onAddToCart = { product -> cartViewModel.addProduct(product) },
                onToggleFavorite = { product -> favoritesViewModel.toggleFavorite(product) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
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
                },
                favoriteProductIds = favoriteProductIds,
                onToggleFavorite = { product -> favoritesViewModel.toggleFavorite(product) }
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                profileViewModel = profileViewModel,
                ordersViewModel = ordersViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                onNavigateToPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onLogout = {
                    clearSessionState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Orders.route) {
            OrdersScreen(
                viewModel = ordersViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() }
            )
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(
                profileViewModel = profileViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() }
            )
        }

        composable(Screen.Addresses.route) {
            AddressesScreen(
                addressViewModel = addressViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() },
                onNavigateToAddressForm = { navController.navigate(Screen.AddressForm.route) }
            )
        }

        composable(Screen.AddressForm.route) {
            AddressFormScreen(
                addressViewModel = addressViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(
                paymentMethodsViewModel = paymentMethodsViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.popBackStack() },
                onNavigateToPaymentMethodForm = { navController.navigate(Screen.PaymentMethodForm.route) }
            )
        }

        composable(Screen.PaymentMethodForm.route) {
            PaymentMethodFormScreen(
                paymentMethodsViewModel = paymentMethodsViewModel,
                onNavigateBack = { navController.popBackStack() }
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
                checkoutViewModel = checkoutViewModel,
                cartViewModel = cartViewModel,
                addressViewModel = addressViewModel,
                paymentMethodsViewModel = paymentMethodsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onConfirmOrder = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                favoritesViewModel = favoritesViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToProfile = { navController.navigate(Screen.Account.route) },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onAddToCart = { product -> cartViewModel.addProduct(product) },
                onToggleFavorite = { product -> favoritesViewModel.toggleFavorite(product) }
            )
        }
    }
}
