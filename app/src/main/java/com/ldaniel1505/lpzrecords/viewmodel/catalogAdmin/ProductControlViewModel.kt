package com.ldaniel1505.lpzrecords.viewmodel.catalogAdmin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PRODUCT_COLUMNS = Columns.raw(
    """
    id,
    title,
    description,
    price,
    stock,
    img_url,
    release_date,
    active,
    created_at,
    fk_id_artist,
    fk_id_category,
    fk_id_supplier,
    artist:fk_id_artist (
        id, name, biography, musical_genre
    ),
    category:fk_id_category (
        id, name
    ),
    supplier:fk_id_supplier (
        id, name, telephone, email
    )
    """.trimIndent()
)

class ProductControlViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchProducts() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .select(columns = PRODUCT_COLUMNS)
                        .decodeList<Product>()
                }
                _products.value = result
            } catch (e: Exception) {
                errorMessage = "Error al cargar productos: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun insertProduct(product: Product) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("products").insert(product)
                }
                fetchProducts()
            } catch (e: Exception) {
                errorMessage = "Error al crear producto: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    fun updateProduct(product: Product) {
        val id = product.id ?: return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .update(product) {
                            filter { eq("id", id) }
                        }
                }
                fetchProducts()
            } catch (e: Exception) {
                errorMessage = "Error al actualizar producto: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    fun deactivateProduct(product: Product) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .update(mapOf("active" to false)) {
                            filter {
                                eq("id", product.id)
                            }
                        }
                }
                _products.value = _products.value.filter { it.id != product.id }

            } catch (e: Exception) {
                errorMessage = "Error al desactivar producto: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}