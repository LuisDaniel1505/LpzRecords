package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.data.model.AdminRecentOrder
import com.ldaniel1505.lpzrecords.data.model.IngresosPeriodo
import com.ldaniel1505.lpzrecords.data.model.PeriodoIngresos
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.dashboard.AdminDashboardViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    adminInitials: String = "A",
    onOpenDrawer: () -> Unit = {},
    onNavigateToAllOrders: () -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                adminInitials = adminInitials,
                onOpenDrawer = onOpenDrawer
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = LpzRed,
                    fontSize = 13.sp
                )
            }

            RevenueCard(
                totalRevenue = uiState.totalRevenue,
                isLoading = uiState.isLoading
            )

            ProfitCard(
                totalProfit = uiState.totalProfit,
                isLoading = uiState.isLoading
            )

            UsersChartSection(
                totalUsers = uiState.totalUsers,
                usersToday = uiState.usersToday,
                viewModel = viewModel
            )

            RevenueChartSection(viewModel = viewModel)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatCard(
                    value = uiState.totalProducts.toString(),
                    label = "PRODUCTOS",
                    accentColor = Color(0xFFC0D8F0),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.totalUsers.toString(),
                    label = "USUARIOS",
                    accentColor = Color(0xFFE8C4B8),
                    modifier = Modifier.weight(1f)
                )
            }

            RecentOrdersHeader(onNavigateToAllOrders = onNavigateToAllOrders)

            if (uiState.recentOrders.isEmpty() && !uiState.isLoading) {
                EmptyOrdersCard()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.recentOrders.forEach { order ->
                        RecentOrderCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.idSale) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfitCard(
    totalProfit: Double,
    isLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "GANANCIA TOTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark.copy(alpha = 0.50f),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = if (isLoading) "..." else money(totalProfit),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4F0E8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D4F)
                )
            }
        }
    }
}

@Composable
private fun RevenueCard(
    totalRevenue: Double,
    isLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "INGRESOS REGISTRADOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark.copy(alpha = 0.50f),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = if (isLoading) "..." else money(totalRevenue),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF0E8E8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed
                )
            }
        }
    }
}

@Composable
private fun UsersChartSection(
    totalUsers: Int,
    usersToday: Int,
    viewModel: AdminDashboardViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Resumen de usuarios",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = LpzDark
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            label = rememberAxisLabelComponent(),
                            valueFormatter = CartesianValueFormatter { _, x, _ ->
                                when (x.toInt()) {
                                    0 -> "Total"
                                    1 -> "Hoy"
                                    else -> ""
                                }
                            }
                        )
                    ),
                    modelProducer = viewModel.userChartModelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.12f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniMetric(label = "TOTAL", value = totalUsers.toString())
                    MiniMetric(label = "NUEVOS HOY", value = usersToday.toString())
                }
            }
        }
    }
}

@Composable
private fun RevenueChartSection(viewModel: AdminDashboardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Ingresos por periodo",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = LpzDark
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodoIngresos.entries.forEach { periodo ->
                val isSelected = viewModel.selectedPeriodo == periodo

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.seleccionarPeriodo(periodo) },
                    label = {
                        Text(
                            text = periodo.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LpzRed,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = LpzDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = LpzDark.copy(alpha = 0.20f),
                        selectedBorderColor = LpzRed
                    )
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = viewModel.selectedPeriodo.descripcion,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark.copy(alpha = 0.45f),
                    letterSpacing = 0.6.sp
                )

                when {
                    viewModel.isLoadingRevenue -> {
                        ChartLoadingState(label = viewModel.selectedPeriodo.label)
                    }

                    viewModel.errorRevenue != null -> {
                        ChartMessageState(message = viewModel.errorRevenue ?: "")
                    }

                    viewModel.ingresosPeriodo.isEmpty() -> {
                        ChartMessageState(message = "Sin ventas en este periodo")
                    }

                    else -> {
                        RevenueLineChart(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueLineChart(viewModel: AdminDashboardViewModel) {
    val labels = viewModel.labelsIngresos
    val bestPeriod = viewModel.ingresosPeriodo.maxByOrNull { it.ingresos }
    val accumulated = viewModel.ingresosPeriodo.sumOf { it.ingresos }
    val showPointLabels = viewModel.ingresosPeriodo.count { it.ingresos > 0.0 } <= 8
    val lineColor = Color(0xFF4B83E6)
    val dataLabelBackground = rememberShapeComponent(
        fill = Fill(Color.White.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(6.dp),
        strokeFill = Fill(LpzDark.copy(alpha = 0.10f)),
        strokeThickness = 1.dp
    )
    val axisLabelBackground = rememberShapeComponent(
        fill = Fill(Color.White.copy(alpha = 0.82f)),
        shape = RoundedCornerShape(5.dp)
    )
    val pointComponent = rememberShapeComponent(
        fill = Fill(Color.White),
        shape = CircleShape,
        strokeFill = Fill(lineColor),
        strokeThickness = 2.dp
    )
    val revenueLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = pointComponent,
                size = 8.dp
            )
        ),
        dataLabel = if (showPointLabels) {
            rememberAxisLabelComponent(
                style = TextStyle(
                    color = LpzDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                padding = Insets(horizontal = 5.dp, vertical = 2.dp),
                background = dataLabelBackground
            )
        } else {
            null
        },
        dataLabelPosition = Position.Vertical.Top,
        dataLabelValueFormatter = CartesianValueFormatter { _, y, _ ->
            moneyChartLabel(y)
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(revenueLine)
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(
                    style = TextStyle(
                        color = LpzDark.copy(alpha = 0.74f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    padding = Insets(horizontal = 4.dp, vertical = 1.dp),
                    background = axisLabelBackground
                ),
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                valueFormatter = CartesianValueFormatter { _, y, _ ->
                    moneyAxis(y)
                },
                itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 })
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(
                    style = TextStyle(
                        color = LpzDark.copy(alpha = 0.74f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                ),
                labelRotationDegrees = if (labels.size > 8) -28f else 0f,
                valueFormatter = CartesianValueFormatter { _, x, _ ->
                    labels.getOrElse(x.toInt()) { "" }
                },
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                    spacing = { axisLabelSpacing(labels.size) }
                )
            )
        ),
        modelProducer = viewModel.revenueModelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

    RevenuePeriodValues(items = viewModel.ingresosPeriodo)

    HorizontalDivider(
        color = Color.Gray.copy(alpha = 0.12f),
        thickness = 1.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "MEJOR PERIODO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.6.sp
            )
            Text(
                text = bestPeriod?.label ?: "-",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LpzDark
            )
            Text(
                text = money(bestPeriod?.ingresos ?: 0.0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "TOTAL ${viewModel.selectedPeriodo.label.uppercase()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.6.sp
            )
            Text(
                text = money(accumulated),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark
            )
        }
    }
}

@Composable
private fun RevenuePeriodValues(items: List<IngresosPeriodo>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LpzDark.copy(alpha = 0.68f)
                )
                Text(
                    text = money(item.ingresos),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed
                )
                Text(
                    text = "${item.totalVentas} ventas",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = LpzDark.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun ChartLoadingState(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                color = LpzRed,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Cargando ${label.lowercase()}...",
                fontSize = 12.sp,
                color = LpzDark.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun ChartMessageState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = LpzDark.copy(alpha = 0.50f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor)
            )
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark.copy(alpha = 0.50f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun MiniMetric(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = LpzDark.copy(alpha = 0.45f),
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LpzDark
        )
    }
}

@Composable
private fun RecentOrdersHeader(onNavigateToAllOrders: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Últimos pedidos",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = LpzDark
        )
        Text(
            text = "VER TODOS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LpzRed,
            modifier = Modifier.clickable(onClick = onNavigateToAllOrders)
        )
    }
}

@Composable
private fun RecentOrderCard(
    order: AdminRecentOrder,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LpzDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = order.customerName.toInitials(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = order.customerName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
                Text(
                    text = order.summaryLine(),
                    fontSize = 12.sp,
                    color = LpzDark.copy(alpha = 0.45f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = money(order.total),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
                Text(
                    text = order.status.ifBlank { "SIN ESTADO" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor(order.status),
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyOrdersCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "No hay pedidos registrados.",
            modifier = Modifier.padding(18.dp),
            color = LpzDark.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    adminInitials: String,
    onOpenDrawer: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = LpzDark
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LPZ RECORDS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
                Text(
                    text = "RESUMEN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed,
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
                    text = adminInitials,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

private fun statusColor(status: String): Color = when (status.uppercase()) {
    "PAGADO", "PAGADA", "COMPLETADO", "COMPLETADA", "ENTREGADO", "ENTREGADA" -> Color(0xFF2E7D32)
    "ENVIADO" -> Color(0xFF1565C0)
    "CANCELADO", "CANCELADA" -> Color(0xFF757575)
    else -> Color(0xFF8B0000)
}

private fun money(value: Double): String {
    return "$${String.format(Locale.US, "%,.2f", value)}"
}

private fun moneyAxis(value: Double): String {
    return "$${String.format(Locale.US, "%,.0f", value)}"
}

private fun moneyChartLabel(value: Double): String {
    return if (value <= 0.0) "" else moneyAxis(value)
}

private fun AdminRecentOrder.summaryLine(): String {
    val date = createdAt.take(10).ifBlank { "Sin fecha" }
    val items = if (itemCount > 0) " - $itemCount art." else ""
    return "ID: ${idSale.takeLast(8)} - $date$items"
}

private fun axisLabelSpacing(labelCount: Int): Int {
    return when {
        labelCount <= 8 -> 1
        labelCount <= 14 -> 2
        labelCount <= 24 -> 3
        else -> 5
    }
}

private fun String.toInitials(): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}
