package com.ldaniel1505.lpzrecords.viewmodel.cart

import androidx.lifecycle.ViewModel
import com.ldaniel1505.lpzrecords.data.model.CartItem
import com.ldaniel1505.lpzrecords.data.model.Product
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface CartUiEvent {
    data class Message(val text: String) : CartUiEvent
    data class ProductDeleted(val item: CartItem) : CartUiEvent
}

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private val _events = MutableSharedFlow<CartUiEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<CartUiEvent> = _events

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
            emitMessage("Este producto no tiene stock disponible.")
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.product.id == product.id && it.selectedFormat == selectedFormat
        }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            if (existingItem.quantity >= product.stock) {
                emitMessage("Solo hay ${product.stock} unidades disponibles.")
                return
            }
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentItems.add(CartItem(product = product, quantity = 1, selectedFormat = selectedFormat))
        }

        publishCart(currentItems)
        emitMessage("Producto agregado al carrito.")
    }

    fun decreaseProductQuantity(product: Product) {
        decreaseProductQuantity(product, product.category?.name ?: DEFAULT_FORMAT)
    }

    fun decreaseProductQuantity(product: Product, selectedFormat: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.product.id == product.id && it.selectedFormat == selectedFormat
        }

        if (existingIndex < 0) return

        val existingItem = currentItems[existingIndex]
        if (existingItem.quantity <= 1) return

        currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity - 1)
        publishCart(currentItems)
    }

    fun deleteProduct(product: Product, selectedFormat: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.product.id == product.id && it.selectedFormat == selectedFormat
        }

        if (existingIndex < 0) return

        val deletedItem = currentItems.removeAt(existingIndex)
        publishCart(currentItems)
        _events.tryEmit(CartUiEvent.ProductDeleted(deletedItem))
    }

    fun restoreProduct(item: CartItem) {
        if (item.product.stock <= 0) {
            emitMessage("El producto ya no tiene stock disponible.")
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.product.id == item.product.id && it.selectedFormat == item.selectedFormat
        }
        val restoredQuantity = item.quantity.coerceAtMost(item.product.stock)

        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(
                quantity = (existing.quantity + restoredQuantity).coerceAtMost(item.product.stock)
            )
        } else {
            currentItems.add(item.copy(quantity = restoredQuantity))
        }

        publishCart(currentItems)
        emitMessage("Producto restaurado.")
    }

    fun clearCart() {
        publishCart(emptyList())
    }

    private fun publishCart(items: List<CartItem>) {
        _cartItems.value = items
        _totalPrice.value = items.sumOf { it.product.price * it.quantity }
    }

    private fun emitMessage(message: String) {
        _events.tryEmit(CartUiEvent.Message(message))
    }

    private companion object {
        const val DEFAULT_FORMAT = "Formato fisico"
    }
}
