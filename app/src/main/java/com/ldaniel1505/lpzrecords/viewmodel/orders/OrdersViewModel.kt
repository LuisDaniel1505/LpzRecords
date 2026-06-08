package com.ldaniel1505.lpzrecords.viewmodel.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Order
import com.ldaniel1505.lpzrecords.data.model.OrderItem
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
                    loadOrders(uid)
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

    fun clearState() {
        isLoading = false
        errorMessage = null
        orders = emptyList()
    }

    fun selectedOrder(orderId: String): Order? {
        return orders.firstOrNull { it.id == orderId }
    }

    private suspend fun loadOrders(uid: String): List<Order> {
        val rpcOrders = runCatching {
            SupabaseClient.client.postgrest
                .rpc(function = "get_user_orders_with_items_v1")
                .decodeAs<List<Order>>()
        }.getOrDefault(emptyList())

        if (rpcOrders.isNotEmpty()) {
            return addMissingOrderItems(rpcOrders)
        }

        val newColumns = Columns.raw(
            "id_sale,id_user,id_address,total,state,payment_method,cancellation_reason,created_at"
        )
        val legacyColumns = Columns.raw(
            "id_sale,id_user,id_address,total,state,payment_method,created_at"
        )

        val orders = runCatching {
            selectOrders(uid, newColumns)
        }.getOrElse {
            selectOrders(uid, legacyColumns)
        }

        return addMissingOrderItems(orders)
    }

    private suspend fun selectOrders(uid: String, columns: Columns): List<Order> {
        return SupabaseClient.client
            .from("sales")
            .select(columns = columns) {
                filter {
                    eq("id_user", uid)
                }
            }
            .decodeList<Order>()
    }

    private suspend fun addMissingOrderItems(orders: List<Order>): List<Order> {
        return orders.map { order ->
            if (order.items.isNotEmpty()) {
                order
            } else {
                val items = runCatching { loadOrderItems(order.id) }.getOrDefault(emptyList())
                order.copy(items = items)
            }
        }
    }

    private suspend fun loadOrderItems(orderId: String): List<OrderItem> {
        val columns = Columns.raw(
            """
            id_sale,
            id_product,
            quantity,
            unit_price,
            product:id_product (
                title,
                img_url,
                artist:fk_id_artist (
                    name
                ),
                category:fk_id_category (
                    name
                )
            )
            """.trimIndent()
        )

        return SupabaseClient.client
            .from("sales_details")
            .select(columns = columns) {
                filter {
                    eq("id_sale", orderId)
                }
            }
            .decodeList<SaleDetailWithProduct>()
            .map { detail ->
                OrderItem(
                    productId = detail.productId,
                    productTitle = detail.product?.title?.takeIf { it.isNotBlank() } ?: "Producto",
                    artistName = detail.product?.artist?.name?.takeIf { it.isNotBlank() }
                        ?: "Artista desconocido",
                    quantity = detail.quantity,
                    selectedFormat = detail.product?.category?.name?.takeIf { it.isNotBlank() }
                        ?: "Formato fisico",
                    unitPrice = detail.unitPrice,
                    imageUrl = detail.product?.imageUrl
                )
            }
    }
}

@Serializable
private data class SaleDetailWithProduct(
    @SerialName("id_sale")
    val orderId: String,
    @SerialName("id_product")
    val productId: String,
    val quantity: Int = 0,
    @SerialName("unit_price")
    val unitPrice: Double = 0.0,
    val product: OrderProductSnapshot? = null
)

@Serializable
private data class OrderProductSnapshot(
    val title: String = "",
    @SerialName("img_url")
    val imageUrl: String? = null,
    val artist: OrderArtistSnapshot? = null,
    val category: OrderCategorySnapshot? = null
)

@Serializable
private data class OrderArtistSnapshot(
    val name: String = ""
)

@Serializable
private data class OrderCategorySnapshot(
    val name: String = ""
)
