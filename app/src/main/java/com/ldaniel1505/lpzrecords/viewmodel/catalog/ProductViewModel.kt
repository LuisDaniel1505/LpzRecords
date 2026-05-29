package com.ldaniel1505.lpzrecords.viewmodel.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    private val _productState = MutableStateFlow<Product?>(null)
    val productState: StateFlow<Product?> = _productState

    var selectProduct by mutableStateOf<Product?>(null)

    fun loadProductById(productId: Int) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val columns = Columns.raw("""
                id,
                name,
                release_date,
                description,
                price,
                stock,
                artist:fk_artist (
                    id,
                    name,
                    musical_genre
                ),
                category:fk_category (
                    id,
                    name
                )
            """.trimIndent())

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .select(columns = columns){
                            filter {
                                eq("id", productId)
                            }
                        }
                        .decodeSingle<Product>()
                }

                selectProduct = result

            } catch (e: Exception) {
                errorMessage = "Error al cargar los detalles: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
    fun loadProducts() {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val columns = Columns.raw("""
                id,
                name,
                release_date,
                description,
                price,
                stock,
                artist:fk_artist (
                    id,
                    name,
                    musical_genre
                ),
                category:fk_category (
                    id,
                    name
                )
            """.trimIndent())

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .select(columns = columns)
                        .decodeList<Product>()
                }
                products = result
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}