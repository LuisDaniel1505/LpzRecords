package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier (
    val id: Int,
    val name: String,
    val telephone: String? = null,
    val email: String? = null
)
