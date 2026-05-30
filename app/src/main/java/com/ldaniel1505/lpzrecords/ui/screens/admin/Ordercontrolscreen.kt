package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════════════
//  MODELOS DE DATOS
//  TODO (BACKEND): Mover a data/model/AdminOrder.kt cuando el backend esté listo.
// ═══════════════════════════════════════════════════════════════════════════

enum class OrderStatus(val displayName: String) {
    PENDIENTE("PENDIENTE"),
    ENVIADO("ENVIADO"),
    PROCESANDO("PROCESANDO"),
    ENTREGADO("ENTREGADO")
}

private fun OrderStatus.displayBgColor(): Color = when (this) {
    OrderStatus.PENDIENTE  -> Color(0xFFFFCDD2)
    OrderStatus.ENVIADO    -> Color(0xFFBBDEFB)
    OrderStatus.PROCESANDO -> Color(0xFFFFE0B2)
    OrderStatus.ENTREGADO  -> Color(0xFFC8E6C9)
}

private fun OrderStatus.displayTextColor(): Color = when (this) {
    OrderStatus.PENDIENTE  -> Color(0xFFB71C1C)
    OrderStatus.ENVIADO    -> Color(0xFF1565C0)
    OrderStatus.PROCESANDO -> Color(0xFFE65100)
    OrderStatus.ENTREGADO  -> Color(0xFF2E7D32)
}

data class AdminOrder(
    val id: Int,
    val date: String,          // TODO (BACKEND): Usar LocalDate y formatear con DateTimeFormatter
    val clientName: String,    // TODO (BACKEND): Vendrá del JOIN con la tabla users
    val total: Double,         // TODO (BACKEND): Calculado desde el servidor (sum de order_items)
    val itemCount: Int,        // TODO (BACKEND): Conteo real de order_items
    val status: OrderStatus
)

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleAdminOrders = listOf(
    AdminOrder(1234, "20 MAYO", "Diego Careaga", 86.00, 2, OrderStatus.PENDIENTE),
    AdminOrder(1235, "20 MAYO", "Diego Careaga", 86.00, 2, OrderStatus.ENVIADO),
    AdminOrder(1236, "20 MAYO", "Diego Careaga", 86.00, 2, OrderStatus.ENTREGADO)
)

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun OrderControlScreen(
    onOpenDrawer: () -> Unit = {}
    // TODO (BACKEND): Inyectar ViewModel:
    // viewModel: OrderControlViewModel = viewModel()
) {
    // TODO (BACKEND): Reemplazar con estado del ViewModel:
    // val uiState by viewModel.uiState.collectAsState()
    // val orders    = uiState.orders
    // val isLoading = uiState.isLoading
    var orders by remember { mutableStateOf(sampleAdminOrders) }

    Scaffold(
        topBar         = { AdminSectionTopBar(title = "ENVÍOS Y PEDIDOS", onOpenDrawer = onOpenDrawer) },
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawSunburstBackground() }
        ) {
            if (orders.isEmpty()) {
                // ── Estado vacío ───────────────────────────────────────
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = "No hay pedidos registrados",
                        fontSize = 15.sp,
                        color    = LpzDark.copy(alpha = 0.45f)
                    )
                }
            } else {
                // ── Lista de pedidos ───────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding      = PaddingValues(vertical = 16.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        AdminOrderCard(
                            order          = order,
                            onStatusChange = { newStatus ->
                                // TODO (BACKEND): viewModel.updateOrderStatus(order.id, newStatus)
                                // En el backend, esto haría un PATCH a /orders/{id} con { status: newStatus }
                                orders = orders.map {
                                    if (it.id == order.id) it.copy(status = newStatus) else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TARJETA DE PEDIDO
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AdminOrderCard(
    order: AdminOrder,
    onStatusChange: (OrderStatus) -> Unit
) {

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text       = "ID: ${order.id}",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark
                    )
                    Text(
                        text     = order.date,
                        fontSize = 11.sp,
                        color    = LpzDark.copy(alpha = 0.50f)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        // TODO (BACKEND): order.total vendrá calculado del servidor
                        text       = "$${"%.2f".format(order.total)}",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark
                    )
                    Text(
                        // TODO (BACKEND): order.itemCount vendrá del conteo real de order_items
                        text     = "${order.itemCount} ART.",
                        fontSize = 11.sp,
                        color    = LpzDark.copy(alpha = 0.50f)
                    )
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f), thickness = 1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text          = "CLIENTE",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = LpzDark.copy(alpha = 0.45f),
                    letterSpacing = 0.8.sp
                )
                // TODO (BACKEND): order.clientName vendrá del JOIN con la tabla users
                Text(
                    text       = order.clientName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = LpzDark
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text          = "ESTADO DEL PEDIDO",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = LpzDark.copy(alpha = 0.45f),
                    letterSpacing = 0.8.sp
                )

                if (!isExpanded) {
                    OrderStatusChip(
                        status          = order.status,
                        inSelectorMode  = false,
                        modifier        = Modifier.fillMaxWidth(),
                        onClick         = { isExpanded = true }
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OrderStatus.entries.forEach { status ->
                            OrderStatusChip(
                                status              = status,
                                inSelectorMode      = true,
                                isCurrentSelection  = status == order.status,
                                modifier            = Modifier.fillMaxWidth(),
                                onClick             = {
                                    // TODO (BACKEND): viewModel.updateOrderStatus(order.id, status)
                                    onStatusChange(status)
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun OrderStatusChip(
    status: OrderStatus,
    inSelectorMode: Boolean,
    modifier: Modifier = Modifier,
    isCurrentSelection: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor   = if (inSelectorMode) Color(0xFFFFCDD2) else status.displayBgColor()
    val textColor = if (inSelectorMode) Color(0xFFB71C1C) else status.displayTextColor()

    val borderStroke: BorderStroke? = if (inSelectorMode && isCurrentSelection)
        BorderStroke(2.dp, Color(0xFFB71C1C))
    else null

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape  = RoundedCornerShape(10.dp),
        color  = bgColor,
        border = borderStroke
    ) {
        Text(
            text          = status.displayName,
            fontSize      = 13.sp,
            fontWeight    = if (inSelectorMode && isCurrentSelection) FontWeight.ExtraBold
            else FontWeight.Bold,
            color         = textColor,
            letterSpacing = 1.sp,
            textAlign     = TextAlign.Center,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  FONDO: Patrón de rayos retro (sunburst) — igual que FavoritesScreen
// ═══════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSunburstBackground() {
    drawRect(color = Color(0xFFF4F1E6))

    val centerX   = size.width / 2f
    val centerY   = size.height / 2f
    val radius    = size.width.coerceAtLeast(size.height) * 1.4f
    val rayCount  = 24
    val angleStep = (2 * Math.PI / rayCount).toFloat()
    val rayColorA = Color(0xFFEDE8D5)
    val rayColorB = Color(0xFFF4F1E6)

    for (i in 0 until rayCount) {
        val startAngle = i * angleStep - angleStep / 2f
        val endAngle   = startAngle + angleStep

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(centerX + radius * cos(startAngle), centerY + radius * sin(startAngle))
            lineTo(centerX + radius * cos(endAngle),   centerY + radius * sin(endAngle))
            close()
        }

        drawPath(path = path, color = if (i % 2 == 0) rayColorA else rayColorB)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrderControlScreenPreview() {
    OrderControlScreen()
}