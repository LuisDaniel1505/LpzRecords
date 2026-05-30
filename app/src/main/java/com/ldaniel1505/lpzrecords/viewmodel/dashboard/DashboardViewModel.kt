package com.ldaniel1505.lpzrecords.viewmodel.dashboard

import DashboardData
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    val chartModelProducer = CartesianChartModelProducer()

    fun loadData() {
        viewModelScope.launch {
            try {
                val response = SupabaseClient.client.from("view_dashboard_actividad").select().decodeSingle<DashboardData>()
                Log.d("DashboardDebug", "Datos recibidos: $response") // <-- MIRA EL LOGCAT

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
}