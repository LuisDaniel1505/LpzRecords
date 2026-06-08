package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    @SerialName("id_address")
    val idAddress: String = "",
    @SerialName("id_user")
    val userId: String = "",
    val street: String = "",
    val city: String = "",
    @SerialName("state_addres")
    val stateAddress: String? = null,
    @SerialName("postal_code")
    val postalCode: String? = null,
    @SerialName("default")
    val isDefault: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val id: String
        get() = idAddress

    val label: String
        get() = if (isDefault) "Principal" else "Direccion"

    val recipientName: String
        get() = ""

    val state: String
        get() = stateAddress.orEmpty()

    val summary: String
        get() = listOf(street, city, stateAddress, postalCode)
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
}

@Serializable
data class CreateAddress(
    @SerialName("id_address")
    val idAddress: String,
    @SerialName("id_user")
    val userId: String,
    val street: String,
    val city: String,
    @SerialName("state_addres")
    val stateAddress: String? = null,
    @SerialName("postal_code")
    val postalCode: String? = null,
    @SerialName("default")
    val isDefault: Boolean = false
)
