package com.ldaniel1505.lpzrecords.viewmodel.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Artist
import com.ldaniel1505.lpzrecords.data.model.Supplier
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupplierViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var suppliers by mutableStateOf<List<Supplier>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectSupplier by mutableStateOf<Supplier?>(null)
    fun loadSupplier() {
        viewModelScope.launch {
            errorMessage = null
            try {
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("suppliers").select().decodeList<Supplier>()
                }
                suppliers = result
            } catch (e: Exception) {
                errorMessage = "Error: No se pudieron cargar los proveedores. ${e.localizedMessage}"
            }
        }
    }

    fun loadSupplierById(supplierId: Int) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val columns = Columns.raw("""
                id,
                name,
                telephone,
                email
            """.trimIndent())

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("suppliers")
                        .select(columns = columns){
                            filter {
                                eq("id", supplierId)
                            }
                        }
                        .decodeSingle<Supplier>()
                }

                selectSupplier = result

            } catch (e: Exception) {
                errorMessage = "Error al cargar al Supplier: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}