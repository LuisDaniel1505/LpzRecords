package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioPerfil (
        val id: String,
        val name: String,
        val created_at: String,
        val is_admin: Boolean
)