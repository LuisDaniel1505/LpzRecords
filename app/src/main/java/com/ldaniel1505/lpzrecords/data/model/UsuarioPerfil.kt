package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioPerfil (
        val id: String,
        val nombre: String,
        val es_admin: Boolean
)