package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ldaniel1505.lpzrecords.data.model.Order
import com.ldaniel1505.lpzrecords.data.model.OrderItem
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed

@Composable
fun OrderDetailScreen(
    order: Order?,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            OrderDetailTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró esta compra.",
                    fontSize = 15.sp,
                    color = LpzDark.copy(alpha = 0.58f)
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 18.dp)
        ) {
            item {
                OrderSummary(order = order)
            }

            item {
                SectionTitle(title = "Productos")
            }

            if (order.items.isEmpty()) {
                item {
                    DetailCard {
                        Text(
                            text = "Los productos de esta compra aparecerán al actualizar el historial.",
                            fontSize = 14.sp,
                            color = LpzDark.copy(alpha = 0.58f)
                        )
                    }
                }
            } else {
                items(order.items, key = { "${it.productId}-${it.selectedFormat}" }) { item ->
                    OrderDetailItem(item = item)
                }
            }

            item {
                SectionTitle(title = "Pago")
            }

            item {
                PaymentSummary(order = order)
            }
        }
    }
}

@Composable
private fun OrderSummary(order: Order) {
    DetailCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Orden ${order.id.takeLast(8)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
                Text(
                    text = formatOrderDate(order.createdAt),
                    fontSize = 13.sp,
                    color = LpzDark.copy(alpha = 0.55f)
                )
            }
            OrderStatusPill(status = order.status)
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                fontSize = 14.sp,
                color = LpzDark.copy(alpha = 0.58f)
            )
            Text(
                text = formatOrderMoney(order.total),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )
        }

        if (order.status.isCancelledOrderStatus() && !order.cancellationReason.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Razón de cancelación",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark.copy(alpha = 0.48f)
                )
                Text(
                    text = order.cancellationReason,
                    fontSize = 13.sp,
                    color = LpzDark.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun OrderDetailItem(item: OrderItem) {
    DetailCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.productTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LpzDark.copy(alpha = 0.10f))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.productTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.artistName,
                    fontSize = 13.sp,
                    color = LpzDark.copy(alpha = 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.quantity} x ${item.selectedFormat}",
                    fontSize = 12.sp,
                    color = LpzDark.copy(alpha = 0.48f)
                )
                Text(
                    text = "Precio unitario: ${formatOrderMoney(item.unitPrice)}",
                    fontSize = 12.sp,
                    color = LpzDark.copy(alpha = 0.48f)
                )
            }

            Text(
                text = formatOrderMoney(item.totalPrice),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )
        }
    }
}

@Composable
private fun PaymentSummary(order: Order) {
    DetailCard {
        DetailRow(
            label = "Método",
            value = order.paymentMethod?.takeIf { it.isNotBlank() } ?: "No especificado"
        )
        DetailRow(
            label = "Productos",
            value = order.items.sumOf { it.quantity }.toString()
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f), thickness = 1.dp)
        DetailRow(
            label = "Total pagado",
            value = formatOrderMoney(order.total),
            highlight = true
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (highlight) 15.sp else 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) LpzDark else LpzDark.copy(alpha = 0.55f)
        )
        Text(
            text = value,
            fontSize = if (highlight) 15.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) LpzRed else LpzDark
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = LpzDark.copy(alpha = 0.56f),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailTopBar(onNavigateBack: () -> Unit) {
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
            Text(
                text = "Detalle de compra",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrderDetailScreenPreview() {
    OrderDetailScreen(
        order = Order(
            idSale = "00000000-0000-0000-0000-123456789abc",
            total = 49.99,
            status = "PENDIENTE",
            paymentMethod = "VISA •••• 1234",
            createdAt = "2026-06-05",
            items = listOf(
                OrderItem(
                    productId = "1",
                    productTitle = "Disco de prueba",
                    artistName = "LPZ Records",
                    quantity = 1,
                    selectedFormat = "Vinilo",
                    unitPrice = 49.99
                )
            )
        )
    )
}
