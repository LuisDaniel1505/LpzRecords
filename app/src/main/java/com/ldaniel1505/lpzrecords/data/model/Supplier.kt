package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier (
    val id: String,
    val name: String,
    val telephone: String,
    val email: String
)