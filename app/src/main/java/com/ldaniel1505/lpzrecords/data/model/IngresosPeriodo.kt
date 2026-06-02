package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IngresosPeriodo(

    // Timestamp del inicio del período (día/semana/mes según la función).
    val periodo: String,

    // Etiqueta legible para el eje X de la gráfica.
    val label: String,

    // Número de ventas en ese período (sin contar CANCELADO)
    @SerialName("total_ventas")
    val totalVentas: Long,

    // Suma de sales.total en ese período
    val ingresos: Double
)

