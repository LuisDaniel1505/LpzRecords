package com.ldaniel1505.lpzrecords.viewmodel.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.AdminProductRow
import com.ldaniel1505.lpzrecords.data.model.AdminRecentOrder
import com.ldaniel1505.lpzrecords.data.model.AdminRecentOrderRpcRow
import com.ldaniel1505.lpzrecords.data.model.AdminSaleRow
import com.ldaniel1505.lpzrecords.data.model.AdminUserRow
import com.ldaniel1505.lpzrecords.data.model.IngresosPeriodo
import com.ldaniel1505.lpzrecords.data.model.PeriodoIngresos
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AdminDashboardUiState(
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalProducts: Int = 0,
    val totalUsers: Int = 0,
    val usersToday: Int = 0,
    val usersLastSevenDays: Int = 0,
    val userRegistrationsLastSevenDays: List<Int> = List(7) { 0 },
    val userRegistrationLabels: List<String> = emptyList(),
    val recentOrders: List<AdminRecentOrder> = emptyList(),
    val adminName: String = "Administrador",
    val adminInitials: String = "A",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminDashboardViewModel : ViewModel() {
    val userChartModelProducer = CartesianChartModelProducer()
    val revenueModelProducer = CartesianChartModelProducer()

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    var selectedPeriodo by mutableStateOf(PeriodoIngresos.ANUAL)
        private set

    var ingresosPeriodo by mutableStateOf<List<IngresosPeriodo>>(emptyList())
        private set

    var labelsIngresos by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoadingRevenue by mutableStateOf(false)
        private set

    var errorRevenue by mutableStateOf<String?>(null)
        private set

    fun loadDashboard() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val dashboardData = withContext(Dispatchers.IO) {
                    val sales = loadSalesSafely()
                    val users = SupabaseClient.client
                        .from("users")
                        .select(columns = Columns.raw("id,name,created_at"))
                        .decodeList<AdminUserRow>()

                    val products = SupabaseClient.client
                        .from("products")
                        .select(columns = Columns.raw("id"))
                        .decodeList<AdminProductRow>()

                    val usersById = users.associateBy { it.id }
                    val adminName = usersById[currentUser?.id]?.name
                        ?: currentUser?.email
                        ?: "Administrador"
                    val directRevenue = sales
                        .filter { it.status.countsAsRevenue() }
                        .sumOf { it.total }
                    val rpcRevenue = loadAnnualRevenueTotal()
                    val totalProfit = loadTotalProfit()
                    val userRegistrationSummary = buildUserRegistrationSummary(users)

                    AdminDashboardUiState(
                        totalRevenue = directRevenue.takeIf { it > 0.0 } ?: rpcRevenue,
                        totalProfit = totalProfit,
                        totalProducts = products.size,
                        totalUsers = users.size,
                        usersToday = userRegistrationSummary.dailyCounts.lastOrNull() ?: 0,
                        usersLastSevenDays = userRegistrationSummary.dailyCounts.sum(),
                        userRegistrationsLastSevenDays = userRegistrationSummary.dailyCounts,
                        userRegistrationLabels = userRegistrationSummary.labels,
                        recentOrders = loadRecentOrders(
                            usersById = usersById,
                            fallbackSales = sales
                        ),
                        adminName = adminName,
                        adminInitials = adminName.toInitials()
                    )
                }

                _uiState.value = dashboardData
                updateUserChart(dashboardData.userRegistrationsLastSevenDays)
                cargarIngresosPorPeriodo(selectedPeriodo)
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las métricas del administrador."
                    )
                }
            }
        }
    }

    fun seleccionarPeriodo(periodo: PeriodoIngresos) {
        if (periodo == selectedPeriodo && ingresosPeriodo.isNotEmpty()) return
        selectedPeriodo = periodo
        cargarIngresosPorPeriodo(periodo)
    }

    private suspend fun loadSalesSafely(): List<AdminSaleRow> {
        return runCatching {
            loadSales(Columns.raw("id_sale,id_user,total,state,cancellation_reason,created_at"))
        }.getOrElse {
            runCatching {
                loadSales(Columns.raw("id_sale,id_user,total,state,created_at"))
            }.getOrDefault(emptyList())
        }
    }

    private suspend fun loadSales(columns: Columns): List<AdminSaleRow> {
        return SupabaseClient.client
            .from("sales")
            .select(columns = columns)
            .decodeList<AdminSaleRow>()
    }

    private suspend fun loadRecentOrders(
        usersById: Map<String, AdminUserRow>,
        fallbackSales: List<AdminSaleRow>
    ): List<AdminRecentOrder> {
        val rpcOrders = loadRecentOrdersRpc("get_admin_recent_orders_v2")
            .ifEmpty { loadRecentOrdersRpc("get_admin_recent_orders") }

        if (rpcOrders.isNotEmpty()) {
            return rpcOrders.map { it.toAdminRecentOrder() }
        }

        return fallbackSales
            .sortedByDescending { it.createdAt }
            .take(5)
            .map { sale ->
                AdminRecentOrder(
                    idSale = sale.idSale,
                    customerName = usersById[sale.userId]?.name ?: "Cliente",
                    total = sale.total,
                    status = sale.status,
                    cancellationReason = sale.cancellationReason,
                    createdAt = sale.createdAt
                )
            }
    }

    private suspend fun loadRecentOrdersRpc(functionName: String): List<AdminRecentOrderRpcRow> {
        return runCatching {
            SupabaseClient.client.postgrest
                .rpc(
                    function = functionName,
                    parameters = buildJsonObject {
                        put("limit_count", 5)
                    }
                )
                .decodeList<AdminRecentOrderRpcRow>()
        }.getOrDefault(emptyList())
    }

    private fun updateUserChart(dailyCounts: List<Int>) {
        viewModelScope.launch {
            userChartModelProducer.runTransaction {
                columnSeries {
                    series(dailyCounts)
                }
            }
        }
    }

    private fun cargarIngresosPorPeriodo(periodo: PeriodoIngresos) {
        isLoadingRevenue = true
        errorRevenue = null
        ingresosPeriodo = emptyList()
        labelsIngresos = emptyList()

        viewModelScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest
                        .rpc(
                            function = periodo.rpcFunction,
                            parameters = buildJsonObject {
                                put(periodo.paramName, periodo.paramValue)
                            }
                        )
                        .decodeList<IngresosPeriodo>()
                }

                ingresosPeriodo = resultado
                labelsIngresos = resultado.map { it.label }

                if (resultado.isNotEmpty()) {
                    revenueModelProducer.runTransaction {
                        lineSeries {
                            series(resultado.map { it.ingresos.toFloat() })
                        }
                    }
                }
            } catch (_: Exception) {
                errorRevenue = "No se pudieron cargar los ingresos."
            } finally {
                isLoadingRevenue = false
            }
        }
    }

    private suspend fun loadAnnualRevenueTotal(): Double {
        return runCatching {
            SupabaseClient.client.postgrest
                .rpc(
                    function = PeriodoIngresos.ANUAL.rpcFunction,
                    parameters = buildJsonObject {
                        put(PeriodoIngresos.ANUAL.paramName, PeriodoIngresos.ANUAL.paramValue)
                    }
                )
                .decodeList<IngresosPeriodo>()
                .sumOf { it.ingresos }
        }.getOrDefault(0.0)
    }

    private suspend fun loadTotalProfit(): Double {
        return runCatching {
            SupabaseClient.client.postgrest
                .rpc(function = "get_admin_total_profit")
                .decodeAs<Double>()
        }.getOrDefault(0.0)
    }
}

private data class UserRegistrationSummary(
    val dailyCounts: List<Int>,
    val labels: List<String>
)

private fun buildUserRegistrationSummary(users: List<AdminUserRow>): UserRegistrationSummary {
    val today = Calendar.getInstance()
    val days = (6 downTo 0).map { daysAgo ->
        Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
    }
    val registrationDays = users
        .mapNotNull { user -> user.createdAt.toMillisOrNull() }
        .map { millis -> Calendar.getInstance().apply { timeInMillis = millis }.dayKey() }

    return UserRegistrationSummary(
        dailyCounts = days.map { day -> registrationDays.count { it == day.dayKey() } },
        labels = days.map { day -> day.shortDayLabel() }
    )
}

private fun String.countsAsRevenue(): Boolean {
    return trim().uppercase() !in setOf("CANCELADO", "CANCELADA", "CANCELLED")
}

private fun AdminRecentOrderRpcRow.toAdminRecentOrder(): AdminRecentOrder {
    return AdminRecentOrder(
        idSale = idSale,
        customerName = customerName?.takeIf { it.isNotBlank() } ?: "Cliente",
        total = total,
        status = status,
        cancellationReason = cancellationReason,
        createdAt = createdAt,
        itemCount = itemCount
    )
}

private fun Calendar.dayKey(): Pair<Int, Int> {
    return get(Calendar.YEAR) to get(Calendar.DAY_OF_YEAR)
}

private fun Calendar.shortDayLabel(): String {
    return when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lun"
        Calendar.TUESDAY -> "Mar"
        Calendar.WEDNESDAY -> "Mie"
        Calendar.THURSDAY -> "Jue"
        Calendar.FRIDAY -> "Vie"
        Calendar.SATURDAY -> "Sab"
        else -> "Dom"
    }
}

private fun String?.toMillisOrNull(): Long? {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val normalized = value.replace("Z", "+00:00")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).parse(normalized)?.time
        }.getOrNull()
    }
}

private fun String.toInitials(): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "A" }
}
