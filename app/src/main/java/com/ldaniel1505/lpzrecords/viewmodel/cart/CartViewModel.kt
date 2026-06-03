package com.ldaniel1505.lpzrecords.viewmodel.cart

import androidx.lifecycle.ViewModel
import com.ldaniel1505.lpzrecords.data.model.CartItem
import com.ldaniel1505.lpzrecords.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private val _cartMessage = MutableStateFlow<String?>(null)
    val cartMessage: StateFlow<String?> = _cartMessage.asStateFlow()

    fun addProduct(product: Product) {
        addProduct(product, product.category?.name ?: DEFAULT_FORMAT)
    }

    fun addProductIfMissing(product: Product) {
        addProductIfMissing(product, product.category?.name ?: DEFAULT_FORMAT)
    }

    fun addProductIfMissing(product: Product, selectedFormat: String) {
        val alreadyExists = _cartItems.value.any {
            it.product.id == product.id && it.selectedFormat == selectedFormat
        }

        if (!alreadyExists) {
            addProduct(product, selectedFormat)
        }
    }

    fun addProduct(product: Product, selectedFormat: String) {
        if (product.stock <= 0) {
            _cartMessage.value = "Este producto no tiene stock disponible."
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.product.id == product.id && it.selectedFormat == selectedFormat
        }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            if (existingItem.quantity >= product.stock) {
                _cartMessage.value = "Solo hay ${product.stock} unidades disponibles."
                return
            }
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentItems.add(CartItem(product = product, quantity = 1, selectedFormat = selectedFormat))
        }

        publishCart(currentItems)
    }

    fun removeProduct(product: Product) {
        removeProduct(product, product.category?.name ?: DEFAULT_FORMAT)
    }

    fun removeProduct(product: Product, selectedFormat: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.product.id == product.id && it.selectedFormat == selectedFormat
        }

        if (existingIndex < 0) return

        val existingItem = currentItems[existingIndex]
        if (existingItem.quantity <= 1) {
            currentItems.removeAt(existingIndex)
        } else {
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity - 1)
        }

        publishCart(currentItems)
    }

    fun clearCart() {
        publishCart(emptyList())
    }

    fun consumeCartMessage() {
        _cartMessage.value = null
    }

    private fun publishCart(items: List<CartItem>) {
        _cartItems.value = items
        _totalPrice.value = items.sumOf { it.product.price * it.quantity }
    }

    private companion object {
        const val DEFAULT_FORMAT = "Formato fisico"
    }
}
