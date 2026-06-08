package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val productId: String,
    val productTitle: String,
    val artistName: String,
    val quantity: Int,
    val selectedFormat: String,
    val unitPrice: Double,
    val imageUrl: String? = null
) {
    val totalPrice: Double
        get() = unitPrice * quantity
}

@Serializable
data class Order(
    @SerialName("id_sale")
    val idSale: String = "",
    @SerialName("id_user")
    val userId: String = "",
    @SerialName("id_address")
    val idAddress: String? = null,
    val total: Double = 0.0,
    @SerialName("state")
    val status: String = "PENDIENTE",
    @SerialName("payment_method")
    val paymentMethod: String? = null,
    @SerialName("cancellation_reason")
    val cancellationReason: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    val items: List<OrderItem> = emptyList()
) {
    val id: String
        get() = idSale
}

@Serializable
data class CreateSale(
    @SerialName("id_sale")
    val idSale: String,
    @SerialName("id_user")
    val userId: String,
    @SerialName("id_address")
    val idAddress: String,
    val total: Double,
    @SerialName("state")
    val status: String = "PENDIENTE",
    @SerialName("payment_method")
    val paymentMethod: String? = null
)

@Serializable
data class CreateSaleDetail(
    @SerialName("id_sale")
    val idSale: String,
    @SerialName("id_product")
    val productId: String,
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: Double,
    @SerialName("unit_cost")
    val unitCost: Double
)
