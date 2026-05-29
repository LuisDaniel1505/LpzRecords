package com.ldaniel1505.lpzrecords.navigation

sealed class Screen(val route: String) {
    object Main          : Screen("main_screen")
    object Login         : Screen("login_screen")
    object SignUp        : Screen("signup_screen")
    object Catalog       : Screen("catalog_screen")
    object Account       : Screen("account_screen")
    object Orders        : Screen("orders_screen")
    object PersonalInfo  : Screen("personal_info_screen")
    object Addresses     : Screen("addresses_screen")
    object PaymentMethods: Screen("payment_methods_screen")
    object Cart          : Screen("cart_screen")
    object Checkout      : Screen("checkout_screen")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
}