package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant


@Serializable
data class Product (
    val id: String,
    val category: Category,
    val artist: Artist,
    val Supplier: Supplier? = null,
    val title: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val img_url: String,
    val release_date: String,
    val active: Boolean,
    val created_at: String


)
