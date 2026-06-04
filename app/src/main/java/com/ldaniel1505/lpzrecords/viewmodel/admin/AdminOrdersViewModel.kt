package com.ldaniel1505.lpzrecords.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.AdminRecentOrderRpcRow
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class AdminOrderListItem(
    val idSale: String,
    val customerName: String,
    val total: Double,
    val status: String,
    val createdAt: String,
    val itemCount: Long
)

data class AdminOrdersUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val orders: List<AdminOrderListItem> = emptyList(),
    val updatingOrderId: String? = null
)

class AdminOrdersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminOrdersUiState())
    val uiState: StateFlow<AdminOrdersUiState> = _uiState.asStateFlow()

    fun fetchOrders() {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest
                        .rpc(
                            function = "get_admin_recent_orders",
                            parameters = buildJsonObject {
                                put("limit_count", 100)
                            }
                        )
                        .decodeList<AdminRecentOrderRpcRow>()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        orders = result.map { row -> row.toListItem() }
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar los pedidos.",
                        orders = emptyList()
                    )
                }
            }
        }
    }

    fun updateOrderStatus(idSale: String, status: String) {
        if (idSale.isBlank() || status.isBlank() || _uiState.value.updatingOrderId != null) return

        _uiState.update {
            it.copy(
                updatingOrderId = idSale,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest
                        .rpc(
                            function = "update_admin_order_status",
                            parameters = buildJsonObject {
                                put("order_id", idSale)
                                put("new_status", status)
                            }
                        )
                }

                _uiState.update { current ->
                    current.copy(
                        updatingOrderId = null,
                        orders = current.orders.map { order ->
                            if (order.idSale == idSale) {
                                order.copy(status = status)
                            } else {
                                order
                            }
                        }
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        updatingOrderId = null,
                        errorMessage = "No se pudo actualizar el estado del pedido."
                    )
                }
            }
        }
    }
}

private fun AdminRecentOrderRpcRow.toListItem(): AdminOrderListItem {
    return AdminOrderListItem(
        idSale = idSale,
        customerName = customerName?.takeIf { it.isNotBlank() } ?: "Cliente",
        total = total,
        status = status,
        createdAt = createdAt,
        itemCount = itemCount
    )
}
