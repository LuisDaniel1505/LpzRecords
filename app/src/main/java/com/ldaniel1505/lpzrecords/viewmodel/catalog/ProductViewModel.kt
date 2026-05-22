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

class ProductViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var products by mutableStateOf<List<Product>>(emptyList())
        private set

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