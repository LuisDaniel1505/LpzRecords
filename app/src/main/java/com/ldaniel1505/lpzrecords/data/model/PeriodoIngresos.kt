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
        descripcion = "Ultimas 24 horas",
        rpcFunction = "get_ingresos_por_horas",
        paramName = "horas_atras",
        paramValue = 24
    ),
    SEMANAL(
        label = "Semanal",
        descripcion = "Ultimos 7 dias",
        rpcFunction = "get_ingresos_diarios",
        paramName = "dias_atras",
        paramValue = 7
    ),
    MENSUAL(
        label = "Mensual",
        descripcion = "Ultimos 30 dias",
        rpcFunction = "get_ingresos_diarios",
        paramName = "dias_atras",
        paramValue = 30
    ),
    ANUAL(
        label = "Anual",
        descripcion = "Balance de los ultimos 12 meses",
        rpcFunction = "get_ingresos_mensuales",
        paramName = "meses_atras",
        paramValue = 12
    )
}
