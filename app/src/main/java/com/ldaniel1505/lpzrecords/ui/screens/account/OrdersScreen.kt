package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// ═══════════════════════════════════════════════════════════════════════════
//  MODELOS DE DATOS
//  TODO (BACKEND): Mover a data/model/Order.kt cuando el backend esté listo.
// ═══════════════════════════════════════════════════════════════════════════

data class OrderItem(
    val productName: String,
    val buyerName: String,  // TODO (BACKEND): Vendrá del objeto User autenticado
    val price: Double
)

data class Order(
    val id: Int,
    val status: String,  // "ENVIADO" | "PENDIENTE" | "ENTREGADO" | "CANCELADO"
    val date: String,    // TODO (BACKEND): Usar LocalDate y formatear con DateTimeFormatter
    val items: List<OrderItem>
) {
    val total: Double get() = items.sumOf { it.price }
}

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleOrders = listOf(
    Order(
        id = 1,
        status = "ENVIADO",
        date = "15/05/2026",
        items = listOf(
            OrderItem("Canción 1 - Vinyl", "Diego Careaga", 35.00),
            OrderItem("Canción 1 - Vinyl", "Diego Careaga", 35.00)
        )
    ),
    Order(
        id = 2,
        status = "ENVIADO",
        date = "15/05/2026",
        items = listOf(
            OrderItem("Canción 1 - Vinyl", "Diego Careaga", 35.00)
        )
    )
)

// ── Color dinámico según el estado del pedido ─────────────────────────────────
private fun statusColor(status: String): Color = when (status.uppercase()) {
    "ENVIADO"   -> Color(0xFF8B0000)
    "ENTREGADO" -> Color(0xFF2E7D32)
    "CANCELADO" -> Color(0xFF757575)
    else        -> Color(0xFF8B0000)
}

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun OrdersScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // TODO (BACKEND): Obtener la lista de órdenes del usuario desde el ViewModel.
    // val uiState   by ordersViewModel.uiState.collectAsState()
    // val orders    = uiState.orders
    // val isLoading = uiState.isLoading
    val orders = sampleOrders

    Scaffold(
        topBar = { OrdersTopBar(onNavigateBack = onNavigateToProfile) },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = BottomNavTab.PROFILE,
                onHome      = onNavigateToHome,
                onSearch    = onNavigateToSearch,
                onCart      = onNavigateToCart,
                onFavorites = onNavigateToFavorites,
                onProfile   = onNavigateToProfile
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->

        if (orders.isEmpty()) {
            // ── Estado vacío ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Aún no tienes compras",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LpzDark.copy(alpha = 0.55f)
                    )
                    Text(
                        text = "¡Explora el catálogo y encuentra tu vinilo!",
                        fontSize = 13.sp,
                        color = LpzDark.copy(alpha = 0.40f)
                    )
                }
            }
        } else {
            // ── Lista de órdenes ───────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun OrderCard(order: Order) {
    val cardBackground = Color(0xFF2E2214)
    val labelColor     = Color(0xFF9E8E78)
    val itemNameColor  = Color(0xFFF0E8D8)
    val buyerNameColor = Color(0xFFAA6655)
    val dividerColor   = Color(0xFF4A3828)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Cabecera: Estado + Fecha ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTADO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = order.date,
                    fontSize = 11.sp,
                    color = labelColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = order.status,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor(order.status)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Lista de ítems ─────────────────────────────────────────
            order.items.forEach { item ->
                // TODO (BACKEND): Cada item vendrá del endpoint /orders/{id}/items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = itemNameColor
                        )
                        Text(
                            text = item.buyerName,
                            fontSize = 11.sp,
                            color = buyerNameColor
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", item.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // ── Total ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = itemNameColor,
                    letterSpacing = 1.sp
                )
                Text(
                    // TODO (BACKEND): order.total vendrá calculado desde el servidor
                    text = "$${String.format("%.2f", order.total)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed
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
private fun OrdersTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint               = LpzDark
                )
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MIS COMPRAS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }
        },
        actions = {
            Icon(
                painter = painterResource(id = R.drawable.vinyl),
                contentDescription = "Logo LPZ Records",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp),
                tint = Color.Unspecified
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrdersScreenPreview() {
    OrdersScreen()
}