package com.ldaniel1505.lpzrecords.viewmodel.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Order
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    fun fetchOrders() {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val uid = SupabaseClient.client.auth.currentUserOrNull()?.id

                if (uid == null) {
                    errorMessage = "No se pudo identificar al usuario actual."
                    orders = emptyList()
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("sales")
                        .select {
                            filter {
                                eq("id_user", uid)
                            }
                        }
                        .decodeList<Order>()
                }

                orders = result.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                errorMessage = "No se pudieron cargar tus pedidos."
                orders = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}
