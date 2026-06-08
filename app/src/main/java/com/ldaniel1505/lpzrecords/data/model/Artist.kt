package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Artist (
    val id: Int,
    val name: String,
    val biography: String? = null,
    val musical_genre: String
)
