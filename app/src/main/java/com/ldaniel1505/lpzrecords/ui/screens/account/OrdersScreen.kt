package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.Order
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
    onNavigateToProfile: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Scaffold(
        topBar = { OrdersTopBar(onNavigateBack = onNavigateToProfile) },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = BottomNavTab.PROFILE,
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    items(viewModel.orders, key = { it.id }) { order ->
                        OrderCard(order = order)
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
                text = "Aun no has realizado ninguna compra",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LpzDark.copy(alpha = 0.55f)
            )
            Text(
                text = "Explora el catalogo y encuentra tu proximo disco.",
                fontSize = 13.sp,
                color = LpzDark.copy(alpha = 0.40f)
            )
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    val cardBackground = Color(0xFF2E2214)
    val labelColor = Color(0xFF9E8E78)
    val itemNameColor = Color(0xFFF0E8D8)
    val dividerColor = Color(0xFF4A3828)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    text = formatDate(order.createdAt),
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

            if (order.items.isEmpty()) {
                Text(
                    text = "Orden ${order.id.takeLast(8)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = itemNameColor
                )
            } else {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = itemNameColor
                            )
                            Text(
                                text = "${item.quantity} x ${item.selectedFormat}",
                                fontSize = 11.sp,
                                color = labelColor
                            )
                        }
                        Text(
                            text = money(item.totalPrice),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LpzRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

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
                    text = money(order.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed
                )
            }
        }
    }
}

private fun statusColor(status: String): Color = when (status.uppercase()) {
    "ENTREGADO" -> Color(0xFF2E7D32)
    "CANCELADO" -> Color(0xFF757575)
    "ENVIADO" -> Color(0xFF8B0000)
    else -> Color(0xFF8B0000)
}

private fun formatDate(value: String): String {
    return value.take(10).ifBlank { "Fecha pendiente" }
}

private fun money(value: Double): String {
    return "$${String.format(Locale.US, "%.2f", value)}"
}

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrdersScreenPreview() {
    OrdersScreen()
}
