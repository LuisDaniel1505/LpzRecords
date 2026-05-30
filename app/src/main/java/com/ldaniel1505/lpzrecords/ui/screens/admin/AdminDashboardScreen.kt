package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.ui.theme.*
import com.ldaniel1505.lpzrecords.viewmodel.dashboard.DashboardViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

// ═══════════════════════════════════════════════════════════════════════════
//  MODELOS DE DATOS
//  TODO (BACKEND): Mover a data/model/AdminDashboard.kt cuando el backend esté listo.
// ═══════════════════════════════════════════════════════════════════════════



data class RecentOrder(
    val id: Int,
    val customerName: String,
    val orderId: String,
    val date: String,
    val total: Double,
    val status: OrderStatusAdmin
)

enum class OrderStatusAdmin(val label: String, val color: Color) {
    PENDING("PENDIENTE", Color(0xFF8B0000)),
    SHIPPED("ENVIADO",   Color(0xFF1565C0)),
    DELIVERED("ENTREGADO", Color(0xFF2E7D32)),
    CANCELLED("CANCELADO", Color(0xFF757575))
}

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleRecentOrders = listOf(
    RecentOrder(1, "Luis Ontiveros", "1234", "10 Mayo", 85.00, OrderStatusAdmin.PENDING),
    RecentOrder(2, "Luis Ontiveros", "1234", "10 Mayo", 85.00, OrderStatusAdmin.SHIPPED),
    RecentOrder(3, "Luis Ontiveros", "1234", "10 Mayo", 85.00, OrderStatusAdmin.DELIVERED)
)

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AdminDashboardScreen(
    onNavigateToAllOrders: () -> Unit = {},
    onNavigateToOrderDetail: (Int) -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    // TODO (BACKEND): Obtener métricas del mes desde el ViewModel.
    // val uiState        by adminViewModel.uiState.collectAsState()
    // val monthlyRevenue = uiState.monthlyRevenue
    // val pendingShipments = uiState.pendingShipments
    // val totalProducts  = uiState.totalProducts
    // val recentOrders   = uiState.recentOrders
    val monthlyRevenue   = 4_250.00   // Placeholder
    val pendingShipments = 1           // Placeholder
    val totalProducts    = 6           // Placeholder
    val recentOrders     = sampleRecentOrders

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = { AdminTopBar() },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RevenueCard(monthlyRevenue = monthlyRevenue)

            Text(text = "Resumen de Usuarios", style = MaterialTheme.typography.headlineSmall)

            // GRÁFICO VICO ESTABLE
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(title = { "Usuarios" }),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = CartesianValueFormatter { _, x, _ ->
                            when (x.toInt()) {
                                0 -> "Total"
                                1 -> "Hoy"
                                else -> ""
                            }
                        }
                    )
                ),
                modelProducer = viewModel.chartModelProducer,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )

            // ── Fila de stats ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatCard(
                    value = totalProducts.toString(),
                    label = "PRODUCTOS\nTOTALES",
                    accentColor = Color(0xFFC0D8F0),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Sección: Últimos Pedidos ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ultimos Pedidos", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LpzDark)
                Text(text = "VER TODOS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LpzRed, modifier = Modifier.clickable(onClick = onNavigateToAllOrders))
            }

            // ── Lista de pedidos ─────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recentOrders.forEach { order ->
                    RecentOrderCard(order = order, onClick = { onNavigateToOrderDetail(order.id) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun RevenueCard(monthlyRevenue: Double) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text          = "INGRESOS DEL MES",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = LpzDark.copy(alpha = 0.50f),
                    letterSpacing = 0.8.sp
                )
                Text(
                    // TODO (BACKEND): adminViewModel.monthlyRevenue
                    text       = "$${String.format("%,.2f", monthlyRevenue)}",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            }
            // Placeholder de ícono/gráfica
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0E8E8))
                // TODO: Reemplazar con un ícono de tendencia o mini-gráfica
            )
        }
    }
}

@Composable

private fun StatCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = modifier
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Placeholder de ícono con color de acento
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor)
                // TODO: Reemplazar con ícono real (truck/package)
            )
            Text(
                // TODO (BACKEND): adminViewModel.pendingShipments / totalProducts
                text       = value,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = LpzDark
            )
            Text(
                text          = label,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = LpzDark.copy(alpha = 0.50f),
                letterSpacing = 0.5.sp,
                lineHeight    = 15.sp
            )
        }
    }
}

@Composable
private fun RecentOrderCard(
    order: RecentOrder,
    onClick: () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Placeholder de foto de perfil / portada de orden
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LpzDark)
                // TODO (BACKEND + COIL): AsyncImage con foto del cliente o portada del producto
            )

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    // TODO (BACKEND): order.customerName vendrá del objeto User asociado
                    text       = order.customerName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
                Text(
                    text     = "ID: ${order.orderId} · ${order.date}",
                    fontSize = 12.sp,
                    color    = LpzDark.copy(alpha = 0.45f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    // TODO (BACKEND): order.total vendrá calculado desde el servidor
                    text       = "$${String.format("%.2f", order.total)}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
                Text(
                    text          = order.status.label,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = order.status.color,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar() {
    // TODO (BACKEND): Obtener initiales y nombre del admin autenticado.
    // val adminInitials = adminViewModel.currentAdmin.initials
    val adminInitials = "AD" // Placeholder

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { /* TODO: Abrir menú lateral (DrawerLayout) */ }) {
                Icon(
                    imageVector        = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint               = LpzDark
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "LPZ RECORDS",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
                Text(
                    text       = "ADMIN",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzRed,
                    letterSpacing = 1.sp
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LpzDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    // TODO (BACKEND): adminViewModel.currentAdmin.initials
                    text       = adminInitials,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}