package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.Order
import com.ldaniel1505.lpzrecords.data.model.OrderItem
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.orders.OrdersViewModel
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Scaffold(
        topBar = { OrdersTopBar(onNavigateBack = onNavigateToProfile) },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = BottomNavTab.ORDERS,
                onHome = onNavigateToHome,
                onSearch = onNavigateToSearch,
                onCart = onNavigateToCart,
                onFavorites = onNavigateToFavorites,
                onProfile = onNavigateToProfile
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LpzRed)
                }
            }

            viewModel.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "No se pudieron cargar tus compras.",
                        fontSize = 14.sp,
                        color = LpzRed
                    )
                }
            }

            viewModel.orders.isEmpty() -> {
                EmptyOrdersState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 18.dp)
                ) {
                    items(viewModel.orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Aún no has realizado ninguna compra",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LpzDark.copy(alpha = 0.55f)
            )
            Text(
                text = "Explora el catálogo y encuentra tu próximo disco.",
                fontSize = 13.sp,
                color = LpzDark.copy(alpha = 0.40f)
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Orden ${order.id.takeLast(8)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark
                    )
                    Text(
                        text = formatDate(order.createdAt),
                        fontSize = 12.sp,
                        color = LpzDark.copy(alpha = 0.52f)
                    )
                }
                StatusPill(status = order.status)
            }

            if (order.items.isEmpty()) {
                Text(
                    text = "Los productos de esta compra aparecerán al actualizar el historial.",
                    fontSize = 13.sp,
                    color = LpzDark.copy(alpha = 0.56f)
                )
            } else {
                order.items.take(2).forEach { item ->
                    OrderPreviewItem(item = item)
                }
            }

            val remainingItems = (order.items.size - 2).coerceAtLeast(0)
            if (remainingItems > 0) {
                Text(
                    text = "+$remainingItems productos más",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed
                )
            }

            if (order.status.isCancelledStatus() && !order.cancellationReason.isNullOrBlank()) {
                Text(
                    text = "Cancelado: ${order.cancellationReason}",
                    fontSize = 12.sp,
                    color = LpzDark.copy(alpha = 0.58f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark.copy(alpha = 0.46f)
                )
                Text(
                    text = money(order.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }
        }
    }
}

@Composable
private fun OrderPreviewItem(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.productTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LpzDark.copy(alpha = 0.10f))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.productTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.artistName,
                fontSize = 12.sp,
                color = LpzDark.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.quantity} x ${item.selectedFormat}",
                fontSize = 11.sp,
                color = LpzDark.copy(alpha = 0.45f)
            )
        }
        Text(
            text = money(item.totalPrice),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LpzRed
        )
    }
}

@Composable
internal fun OrderStatusPill(status: String) {
    val normalizedStatus = status.ifBlank { "SIN ESTADO" }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(orderStatusColor(normalizedStatus).copy(alpha = 0.13f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = normalizedStatus,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = orderStatusColor(normalizedStatus)
        )
    }
}

@Composable
private fun StatusPill(status: String) {
    OrderStatusPill(status = status)
}

internal fun orderStatusColor(status: String): Color = when (status.uppercase(Locale.US)) {
    "ENTREGADO", "ENTREGADA", "COMPLETADO", "COMPLETADA", "PAGADO", "PAGADA" -> Color(0xFF2E7D32)
    "CANCELADO", "CANCELADA", "CANCELLED" -> Color(0xFF757575)
    "ENVIADO", "ENVIADA" -> Color(0xFF1565C0)
    "PROCESANDO" -> Color(0xFFE65100)
    else -> Color(0xFF8B0000)
}

internal fun String.isCancelledOrderStatus(): Boolean {
    return trim().uppercase(Locale.US) in setOf("CANCELADO", "CANCELADA", "CANCELLED")
}

private fun String.isCancelledStatus(): Boolean = isCancelledOrderStatus()

internal fun formatOrderDate(value: String): String {
    return value.take(10).ifBlank { "Fecha pendiente" }
}

private fun formatDate(value: String): String = formatOrderDate(value)

internal fun formatOrderMoney(value: Double): String {
    return "$${String.format(Locale.US, "%.2f", value)}"
}

private fun money(value: Double): String = formatOrderMoney(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = LpzDark
                )
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mis compras",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Cuenta",
                    tint = LpzDark
                )
            }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrdersScreenPreview() {
    OrdersScreen()
}
