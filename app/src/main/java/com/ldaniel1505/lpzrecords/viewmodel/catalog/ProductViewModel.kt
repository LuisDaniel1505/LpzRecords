package com.ldaniel1505.lpzrecords.viewmodel.catalog

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    var filteredProducts by mutableStateOf<List<Product>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf(ALL_CATEGORIES)
        private set

    val categoryOptions: List<String>
        get() = listOf(ALL_CATEGORIES) + products
            .mapNotNull { it.category?.name }
            .distinct()
            .sorted()

    var selectProduct by mutableStateOf<Product?>(null)

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        applyCatalogFilters()
    }

    fun onCategorySelected(category: String) {
        selectedCategory = category
        applyCatalogFilters()
    }

    private fun applyCatalogFilters() {
        val query = searchQuery.trim()

        filteredProducts = products.filter { product ->
            val matchesSearch = query.isBlank() ||
                    product.title.contains(query, ignoreCase = true) ||
                    product.artist?.name?.contains(query, ignoreCase = true) == true ||
                    product.category?.name?.contains(query, ignoreCase = true) == true

            val matchesCategory = selectedCategory == ALL_CATEGORIES ||
                    product.category?.name.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }

    fun loadProductById(productId: String) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val columns = Columns.raw(
                    """
                    id,
                    artist:fk_id_artist (
                        id,
                        name,
                        biography,
                        musical_genre
                    ),
                    category:fk_id_category (
                        id,
                        name
                    ),
                    supplier:fk_id_supplier (
                        id,
                        name,
                        telephone,
                        email
                    ),
                    title,
                    description,
                    price,
                    stock,
                    img_url,
                    release_date,
                    active,
                    created_at
                    """.trimIndent()
                )

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .select(columns = columns) {
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
                val columns = Columns.raw(
                    """
                    id,
                    artist:fk_id_artist (
                        id,
                        name,
                        biography,
                        musical_genre
                    ),
                    category:fk_id_category (
                        id,
                        name
                    ),
                    supplier:fk_id_supplier (
                        id,
                        name,
                        telephone,
                        email
                    ),
                    title,
                    description,
                    price,
                    stock,
                    img_url,
                    release_date,
                    active,
                    created_at
                    """.trimIndent()
                )

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .select(columns = columns) {
                            filter {
                                eq("active", true)
                            }
                        }
                        .decodeList<Product>()
                }
                products = result
                applyCatalogFilters()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
                products = emptyList()
                filteredProducts = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    private companion object {
        const val ALL_CATEGORIES = "Todas"
    }
}
