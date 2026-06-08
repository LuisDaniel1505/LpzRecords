package com.ldaniel1505.lpzrecords.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.AdminRecentOrderRpcRow
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AdminOrderListItem(
    val idSale: String,
    val customerName: String,
    val total: Double,
    val status: String,
    val cancellationReason: String? = null,
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
                val result = withContext(Dispatchers.IO) { loadOrders() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        orders = result
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

    private suspend fun loadOrders(): List<AdminOrderListItem> {
        val rpcOrders = loadRpcOrders("get_admin_recent_orders_v2")
            .ifEmpty { loadRpcOrders("get_admin_recent_orders") }

        if (rpcOrders.isNotEmpty()) {
            return rpcOrders.map { row -> row.toListItem() }
        }

        return runCatching {
            loadSalesFallback(Columns.raw("id_sale,id_user,total,state,cancellation_reason,created_at"))
        }.getOrElse {
            loadSalesFallback(Columns.raw("id_sale,id_user,total,state,created_at"))
        }
    }

    private suspend fun loadRpcOrders(functionName: String): List<AdminRecentOrderRpcRow> {
        return runCatching {
            SupabaseClient.client.postgrest
                .rpc(
                    function = functionName,
                    parameters = buildJsonObject {
                        put("limit_count", 100)
                    }
                )
                .decodeList<AdminRecentOrderRpcRow>()
        }.getOrDefault(emptyList())
    }

    private suspend fun loadSalesFallback(columns: Columns): List<AdminOrderListItem> {
        return SupabaseClient.client
            .from("sales")
            .select(columns = columns)
            .decodeList<AdminOrderFallbackRow>()
            .sortedByDescending { it.createdAt }
            .take(100)
            .map { row ->
                AdminOrderListItem(
                    idSale = row.idSale,
                    customerName = "Cliente",
                    total = row.total,
                    status = row.status,
                    cancellationReason = row.cancellationReason?.takeIf { it.isNotBlank() },
                    createdAt = row.createdAt,
                    itemCount = 0
                )
            }
    }

    fun updateOrderStatus(idSale: String, status: String, cancellationReason: String? = null) {
        if (idSale.isBlank() || status.isBlank() || _uiState.value.updatingOrderId != null) return
        val cleanReason = cancellationReason?.trim()

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
                                put("cancellation_reason_value", cleanReason)
                            }
                        )
                }

                _uiState.update { current ->
                    current.copy(
                        updatingOrderId = null,
                        orders = current.orders.map { order ->
                            if (order.idSale == idSale) {
                                order.copy(
                                    status = status,
                                    cancellationReason = cleanReason.takeIf {
                                        status.trim().uppercase() in setOf("CANCELADO", "CANCELADA", "CANCELLED")
                                    }
                                )
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
        cancellationReason = cancellationReason?.takeIf { it.isNotBlank() },
        createdAt = createdAt,
        itemCount = itemCount
    )
}

@Serializable
private data class AdminOrderFallbackRow(
    @SerialName("id_sale")
    val idSale: String = "",
    @SerialName("id_user")
    val userId: String = "",
    val total: Double = 0.0,
    @SerialName("state")
    val status: String = "",
    @SerialName("cancellation_reason")
    val cancellationReason: String? = null,
    @SerialName("created_at")
    val createdAt: String = ""
)
