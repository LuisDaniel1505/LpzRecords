package com.ldaniel1505.lpzrecords

import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartViewModelTest {

    @Test
    fun decreaseAtOneDoesNotDeleteProduct() {
        val viewModel = CartViewModel()
        val product = product(stock = 3)

        viewModel.addProduct(product, "Vinilo")
        viewModel.decreaseProductQuantity(product, "Vinilo")

        assertEquals(1, viewModel.cartItems.value.size)
        assertEquals(1, viewModel.cartItems.value.single().quantity)
    }

    @Test
    fun deleteAndRestoreKeepsPreviousQuantityWithinStock() {
        val viewModel = CartViewModel()
        val product = product(stock = 2)

        viewModel.addProduct(product, "Vinilo")
        viewModel.addProduct(product, "Vinilo")
        val deletedItem = viewModel.cartItems.value.single()

        viewModel.deleteProduct(product, "Vinilo")
        assertTrue(viewModel.cartItems.value.isEmpty())

        viewModel.restoreProduct(deletedItem.copy(quantity = 5))

        assertEquals(2, viewModel.cartItems.value.single().quantity)
    }

    private fun product(stock: Int) = Product(
        id = "product-1",
        title = "Disco de prueba",
        description = "",
        price = 100.0,
        stock = stock,
        img_url = null,
        release_date = "",
        active = true,
        created_at = "",
        unitCost = 60.0
    )
}
