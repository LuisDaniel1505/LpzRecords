package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Product (
    val id: Int,
    val name: String,
    val release_date: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val artist: Artist,
    val category: Category
)
