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
    val totalProducts: Int = 0,
    val totalUsers: Int = 0,
    val usersToday: Int = 0,
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

                    AdminDashboardUiState(
                        totalRevenue = directRevenue.takeIf { it > 0.0 } ?: rpcRevenue,
                        totalProducts = products.size,
                        totalUsers = users.size,
                        usersToday = users.count { it.createdAt.isToday() },
                        recentOrders = loadRecentOrders(
                            usersById = usersById,
                            fallbackSales = sales
                        ),
                        adminName = adminName,
                        adminInitials = adminName.toInitials()
                    )
                }

                _uiState.value = dashboardData
                updateUserChart(dashboardData.totalUsers, dashboardData.usersToday)
                cargarIngresosPorPeriodo(selectedPeriodo)
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las metricas del administrador."
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
            SupabaseClient.client
                .from("sales")
                .select(columns = Columns.raw("id_sale,id_user,total,state,created_at"))
                .decodeList<AdminSaleRow>()
        }.getOrDefault(emptyList())
    }

    private suspend fun loadRecentOrders(
        usersById: Map<String, AdminUserRow>,
        fallbackSales: List<AdminSaleRow>
    ): List<AdminRecentOrder> {
        val rpcOrders = runCatching {
            SupabaseClient.client.postgrest
                .rpc(
                    function = "get_admin_recent_orders",
                    parameters = buildJsonObject {
                        put("limit_count", 5)
                    }
                )
                .decodeList<AdminRecentOrderRpcRow>()
        }.getOrDefault(emptyList())

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
                    createdAt = sale.createdAt
                )
            }
    }

    private fun updateUserChart(totalUsers: Int, usersToday: Int) {
        viewModelScope.launch {
            userChartModelProducer.runTransaction {
                columnSeries {
                    series(totalUsers, usersToday)
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
        createdAt = createdAt,
        itemCount = itemCount
    )
}

private fun String?.isToday(): Boolean {
    val millis = this.toMillisOrNull() ?: return false
    val today = Calendar.getInstance()
    val value = Calendar.getInstance().apply { timeInMillis = millis }
    return today.get(Calendar.YEAR) == value.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == value.get(Calendar.DAY_OF_YEAR)
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
