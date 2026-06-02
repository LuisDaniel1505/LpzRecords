package com.ldaniel1505.lpzrecords.data.model

enum class PeriodoIngresos(
    // Etiqueta que se muestra en el chip de la UI (ej. "Mensual")
    val label: String,

    // Descripción corta debajo del chip o en la tarjeta (ej. "Últimos 30 días")
    val descripcion: String,

    // Nombre exacto de la función RPC en Supabase
    val rpcFunction: String,

    // Nombre del parámetro que acepta esa función
    val paramName: String,

    // Valor que se le pasa al parámetro
    val paramValue: Int
) {
    DIARIO(
        label       = "Hoy",
        descripcion = "Últimas 24 horas",
        rpcFunction = "get_ingresos_por_horas",
        paramName   = "horas_atras",
        paramValue  = 24
    ),
    SEMANAL(
        label       = "Semanal",
        descripcion = "Últimos 7 días",
        rpcFunction = "get_ingresos_diarios",
        paramName   = "dias_atras",
        paramValue  = 7
    ),
    MENSUAL(
        label       = "Mensual",
        descripcion = "Últimos 30 días",
        rpcFunction = "get_ingresos_diarios",
        paramName   = "dias_atras",
        paramValue  = 30
    ),
    ANUAL(
        label       = "Anual",
        descripcion = "Balance de los últimos 12 meses",
        rpcFunction = "get_ingresos_mensuales", // <-- Agrupado por mes entero
        paramName   = "meses_atras",
        paramValue  = 12
    )
}