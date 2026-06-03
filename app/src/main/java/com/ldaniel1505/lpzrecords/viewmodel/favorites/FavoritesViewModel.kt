package com.ldaniel1505.lpzrecords.viewmodel.favorites

import androidx.lifecycle.ViewModel
import com.ldaniel1505.lpzrecords.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesViewModel : ViewModel() {
    private val _favoriteProducts = MutableStateFlow<List<Product>>(emptyList())
    val favoriteProducts: StateFlow<List<Product>> = _favoriteProducts.asStateFlow()

    fun toggleFavorite(product: Product) {
        val currentFavorites = _favoriteProducts.value
        _favoriteProducts.value = if (currentFavorites.any { it.id == product.id }) {
            currentFavorites.filterNot { it.id == product.id }
        } else {
            currentFavorites + product
        }
    }

    fun isFavorite(productId: String): Boolean {
        return _favoriteProducts.value.any { it.id == productId }
    }

    fun clearFavorites() {
        _favoriteProducts.value = emptyList()
    }
}
