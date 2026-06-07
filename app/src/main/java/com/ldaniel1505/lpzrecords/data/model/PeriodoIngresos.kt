package com.ldaniel1505.lpzrecords.data.model

enum class PeriodoIngresos(
    val label: String,
    val descripcion: String,
    val rpcFunction: String,
    val paramName: String,
    val paramValue: Int
) {
    DIARIO(
        label = "Hoy",
        descripcion = "Últimas 24 horas",
        rpcFunction = "get_ingresos_por_horas",
        paramName = "horas_atras",
        paramValue = 24
    ),
    SEMANAL(
        label = "Semanal",
        descripcion = "Últimos 7 días",
        rpcFunction = "get_ingresos_diarios",
        paramName = "dias_atras",
        paramValue = 7
    ),
    MENSUAL(
        label = "Mensual",
        descripcion = "Últimos 30 días",
        rpcFunction = "get_ingresos_diarios",
        paramName = "dias_atras",
        paramValue = 30
    ),
    ANUAL(
        label = "Anual",
        descripcion = "Balance de los últimos 12 meses",
        rpcFunction = "get_ingresos_mensuales",
        paramName = "meses_atras",
        paramValue = 12
    )
}
