package com.ldaniel1505.lpzrecords.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.ldaniel1505.lpzrecords.ui.screens.account.OrderDetailScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.OrdersScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PaymentMethodFormScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PaymentMethodsScreen
import com.ldaniel1505.lpzrecords.ui.screens.account.PersonalInfoScreen
import com.ldaniel1505.lpzrecords.ui.screens.admin.AdminHostScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.LoginScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.SessionBootstrapScreen
import com.ldaniel1505.lpzrecords.ui.screens.auth.SignUpScreen
import com.ldaniel1505.lpzrecords.ui.screens.cart.CartScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.CatalogScreen
import com.ldaniel1505.lpzrecords.ui.screens.catalog.ProductDetailScreen
import com.ldaniel1505.lpzrecords.ui.screens.checkout.CheckoutScreen
import com.ldaniel1505.lpzrecords.ui.screens.favorites.FavoritesScreen
import com.ldaniel1505.lpzrecords.ui.screens.search.SearchScreen
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel
import com.ldaniel1505.lpzrecords.viewmodel.checkout.CheckoutViewModel
import com.ldaniel1505.lpzrecords.viewmodel.favorites.FavoritesViewModel
import com.ldaniel1505.lpzrecords.viewmodel.auth.SessionBootstrapViewModel
import com.ldaniel1505.lpzrecords.viewmodel.auth.SessionDestination
import com.ldaniel1505.lpzrecords.viewmodel.orders.OrdersViewModel
import com.ldaniel1505.lpzrecords.viewmodel.profile.ProfileViewModel
import io.github.jan.supabase.gotrue.auth

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
    var showRegistrationRequiredDialog by remember { mutableStateOf(false) }

    fun isAuthenticated(): Boolean {
        return SupabaseClient.client.auth.currentUserOrNull() != null
    }

    fun requireAccount(action: () -> Unit) {
        if (isAuthenticated()) {
            action()
        } else {
            showRegistrationRequiredDialog = true
        }
    }

    fun navigateToStoreRoot(route: String) {
        navController.navigate(route) {
            popUpTo(Screen.Catalog.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun navigateToCatalog() = navigateToStoreRoot(Screen.Catalog.route)

    fun navigateToSearch() {
        navigateToStoreRoot(Screen.Search.route)
    }

    fun navigateToCart() {
        requireAccount { navigateToStoreRoot(Screen.Cart.route) }
    }

    fun navigateToFavorites() {
        requireAccount { navigateToStoreRoot(Screen.Favorites.route) }
    }

    fun navigateToAccount() {
        requireAccount { navigateToStoreRoot(Screen.Account.route) }
    }

    fun navigateToOrders() {
        requireAccount { navigateToStoreRoot(Screen.Orders.route) }
    }

    fun navigateClearingBackStack(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) { inclusive = true }
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
        startDestination = Screen.Bootstrap.route
    ) {
        composable(Screen.Bootstrap.route) {
            val bootstrapViewModel: SessionBootstrapViewModel = viewModel()
            val bootstrapState by bootstrapViewModel.uiState.collectAsState()

            LaunchedEffect(bootstrapState.destination) {
                val route = when (bootstrapState.destination) {
                    SessionDestination.PUBLIC_HOME -> Screen.Main.route
                    SessionDestination.CATALOG -> Screen.Catalog.route
                    SessionDestination.ADMIN -> Screen.Admin.route
                    null -> return@LaunchedEffect
                }
                navController.navigate(route) {
                    popUpTo(Screen.Bootstrap.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            SessionBootstrapScreen(
                isLoading = bootstrapState.isLoading,
                errorMessage = bootstrapState.errorMessage,
                onRetry = bootstrapViewModel::retry,
                onGoToLogin = {
                    navigateClearingBackStack(Screen.Login.route)
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToStore = { navigateToCatalog() }
            )
        }

        composable(Screen.Login.route) {
            BackHandler {
                navigateClearingBackStack(Screen.Main.route)
            }
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = { isAdmin ->
                    navigateClearingBackStack(
                        if (isAdmin) Screen.Admin.route else Screen.Catalog.route
                    )
                }
            )
        }

        composable(Screen.Admin.route) {
            AdminHostScreen(
                profileViewModel = profileViewModel,
                onLogout = {
                    clearSessionState()
                    navigateClearingBackStack(Screen.Login.route)
                },
                onUnauthorized = {
                    clearSessionState()
                    navigateClearingBackStack(Screen.Login.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navigateClearingBackStack(Screen.Catalog.route)
                }
            )
        }

        composable(Screen.Catalog.route) {
            CatalogScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToOrders() },
                onNavigateToAccount = { navigateToAccount() },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                favoriteProductIds = favoriteProductIds,
                onAddToCart = { product -> requireAccount { cartViewModel.addProduct(product) } },
                onToggleFavorite = { product -> requireAccount { favoritesViewModel.toggleFavorite(product) } },
                cartViewModel = cartViewModel
            )
        }

        composable(Screen.Search.route) {
            BackHandler { navigateToCatalog() }
            SearchScreen(
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToOrders() },
                onNavigateToAccount = { navigateToAccount() },
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
            BackHandler { navigateToCatalog() }
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToOrders() },
                onAddToCart = { product -> requireAccount { cartViewModel.addProduct(product) } },
                onBuyNow = { product ->
                    requireAccount {
                        cartViewModel.addProductIfMissing(product)
                        navController.navigate(Screen.Checkout.route)
                    }
                },
                favoriteProductIds = favoriteProductIds,
                onToggleFavorite = { product -> requireAccount { favoritesViewModel.toggleFavorite(product) } },
                cartViewModel = cartViewModel
            )
        }

        composable(Screen.Account.route) {
            BackHandler { navigateToCatalog() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            AccountScreen(
                profileViewModel = profileViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onLogout = {
                    clearSessionState()
                    navigateClearingBackStack(Screen.Login.route)
                }
            )
        }

        composable(Screen.Orders.route) {
            BackHandler { navigateToAccount() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            OrdersScreen(
                viewModel = ordersViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToAccount() },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            BackHandler { navController.popBackStack() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                } else if (ordersViewModel.orders.isEmpty()) {
                    ordersViewModel.fetchOrders()
                }
            }
            OrderDetailScreen(
                order = ordersViewModel.selectedOrder(orderId),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PersonalInfo.route) {
            BackHandler { navigateToAccount() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            PersonalInfoScreen(
                profileViewModel = profileViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToAccount() }
            )
        }

        composable(Screen.Addresses.route) {
            BackHandler { navigateToAccount() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            AddressesScreen(
                addressViewModel = addressViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToAccount() },
                onNavigateToAddressForm = { navController.navigate(Screen.AddressForm.route) }
            )
        }

        composable(Screen.AddressForm.route) {
            BackHandler { navController.popBackStack() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            AddressFormScreen(
                addressViewModel = addressViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentMethods.route) {
            BackHandler { navigateToAccount() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            PaymentMethodsScreen(
                paymentMethodsViewModel = paymentMethodsViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToAccount() },
                onNavigateToPaymentMethodForm = { navController.navigate(Screen.PaymentMethodForm.route) }
            )
        }

        composable(Screen.PaymentMethodForm.route) {
            BackHandler { navController.popBackStack() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            PaymentMethodFormScreen(
                paymentMethodsViewModel = paymentMethodsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            BackHandler { navigateToCatalog() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            CartScreen(
                cartViewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToOrders() },
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
            )
        }

        composable(Screen.Checkout.route) {
            BackHandler { navigateToCatalog() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            CheckoutScreen(
                checkoutViewModel = checkoutViewModel,
                cartViewModel = cartViewModel,
                addressViewModel = addressViewModel,
                paymentMethodsViewModel = paymentMethodsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToFavorites = { navigateToFavorites() },
                onNavigateToProfile = { navigateToOrders() },
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
            BackHandler { navigateToCatalog() }
            LaunchedEffect(Unit) {
                if (!isAuthenticated()) {
                    navigateClearingBackStack(Screen.Login.route)
                }
            }
            FavoritesScreen(
                favoritesViewModel = favoritesViewModel,
                onNavigateToHome = { navigateToCatalog() },
                onNavigateToSearch = { navigateToSearch() },
                onNavigateToCart = { navigateToCart() },
                onNavigateToProfile = { navigateToOrders() },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onAddToCart = { product -> requireAccount { cartViewModel.addProduct(product) } },
                onToggleFavorite = { product -> requireAccount { favoritesViewModel.toggleFavorite(product) } },
                cartViewModel = cartViewModel
            )
        }
    }

    if (showRegistrationRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showRegistrationRequiredDialog = false },
            title = { Text("Registro requerido") },
            text = { Text("Necesitas registrarte para realizar esta acción.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegistrationRequiredDialog = false
                        navigateClearingBackStack(Screen.Login.route)
                    }
                ) {
                    Text("Iniciar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegistrationRequiredDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
