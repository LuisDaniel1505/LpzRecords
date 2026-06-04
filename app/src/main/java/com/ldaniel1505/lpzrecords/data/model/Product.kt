package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant


@Serializable
data class Product (
    val id: String,
    @SerialName("category")
    val category: Category? = null,
    @SerialName("artist")
    val artist: Artist? = null,
    @SerialName("supplier")
    val supplier: Supplier? = null,
    val title: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val img_url: String?,
    val release_date: String,
    var active: Boolean,
    val created_at: String,

    @SerialName("fk_id_category")
    val fkCategory: Int? = null,
    @SerialName("fk_id_artist")
    val fkArtist: Int? = null,
    @SerialName("fk_id_supplier")
    val fkSupplier: Int? = null,
    @SerialName("unit_cost")
    val unitCost: Double = 0.0,
)
