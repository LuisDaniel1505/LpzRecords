package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminSaleRow(
    @SerialName("id_sale")
    val idSale: String = "",
    @SerialName("id_user")
    val userId: String = "",
    val total: Double = 0.0,
    @SerialName("state")
    val status: String = "",
    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class AdminRecentOrderRpcRow(
    @SerialName("id_sale")
    val idSale: String = "",
    @SerialName("id_user")
    val userId: String = "",
    @SerialName("customer_name")
    val customerName: String? = null,
    val total: Double = 0.0,
    @SerialName("state")
    val status: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("item_count")
    val itemCount: Long = 0
)

@Serializable
data class AdminUserRow(
    val id: String = "",
    val name: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AdminProductRow(
    val id: String = ""
)

data class AdminRecentOrder(
    val idSale: String,
    val customerName: String,
    val total: Double,
    val status: String,
    val createdAt: String,
    val itemCount: Long = 0
)
