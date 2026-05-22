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

class ArtistViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    fun loadArtist() {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("artist").select().decodeList<Product>()
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