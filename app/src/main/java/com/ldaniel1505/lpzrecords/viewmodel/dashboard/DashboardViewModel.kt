package com.ldaniel1505.lpzrecords.viewmodel.dashboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.DashboardData
import com.ldaniel1505.lpzrecords.data.model.IngresosPeriodo
import com.ldaniel1505.lpzrecords.data.model.PeriodoIngresos
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
class DashboardViewModel : ViewModel() {
    val chartModelProducer = CartesianChartModelProducer()
    val revenueModelProducer = CartesianChartModelProducer()

    var selectedPeriodo by mutableStateOf(PeriodoIngresos.DIARIO)
        private set

    // Lista de datos crudos (para "MEJOR MES", "ACUMULADO", etc.)
    var ingresosPeriodo by mutableStateOf<List<IngresosPeriodo>>(emptyList())
        private set

    // Las etiquetas del eje X ("Ene 2025", "Feb 2025", …)
    var labelsIngresos by mutableStateOf<List<String>>(emptyList())
        private set

    // Estados de carga y error para la sección de ingresos
    var isLoadingRevenue by mutableStateOf(false)
        private set
    var errorRevenue by mutableStateOf<String?>(null)
        private set

    fun loadData() {
        viewModelScope.launch {
            try {
                val response = SupabaseClient.client.from("view_dashboard_actividad").select().decodeSingle<DashboardData>()
                Log.d("DashboardDebug", "Datos recibidos: $response")

                chartModelProducer.runTransaction {
                    columnSeries {
                        series(response.total_usuarios, response.nuevos_hoy)
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardDebug", "Error al cargar: ${e.message}")
            }
        }
    }

    fun seleccionarPeriodo(periodo: PeriodoIngresos) {
        if (periodo == selectedPeriodo && ingresosPeriodo.isNotEmpty()) return
        selectedPeriodo = periodo
        cargarIngresosPorPeriodo(periodo)
    }

    // Lógica principal de carga
    private fun cargarIngresosPorPeriodo(periodo: PeriodoIngresos) {
        isLoadingRevenue = true
        errorRevenue     = null
        ingresosPeriodo  = emptyList()

        viewModelScope.launch {
            try {
                val resultado: List<IngresosPeriodo> = withContext(Dispatchers.IO) {
                    SupabaseClient.client.postgrest
                        .rpc(
                            function   = periodo.rpcFunction,
                            parameters = buildJsonObject {
                                put(periodo.paramName, periodo.paramValue)
                            }
                        )
                        .decodeList<IngresosPeriodo>()
                }

                Log.d("DashboardDebug", "${periodo.label}: ${resultado.size} puntos")

                ingresosPeriodo = resultado
                labelsIngresos  = resultado.map { it.label }

                if (resultado.isNotEmpty()) {
                    revenueModelProducer.runTransaction {
                        lineSeries {
                            series(resultado.map { it.ingresos.toFloat() })
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardDebug", "Error ${periodo.label}: ${e.message}")
                errorRevenue = "No se pudieron cargar los datos: ${e.localizedMessage}"
            } finally {
                isLoadingRevenue = false
            }
        }
    }
}
