package com.ldaniel1505.lpzrecords.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════
//  MODELOS DE DATOS LOCALES
//  TODO (BACKEND): Estos datos vendrán del ViewModel, que los obtiene de
//  la sesión del usuario y del carrito activo.
// ═══════════════════════════════════════════════════════════════════════════

data class CheckoutAddress(
    val id: Int,
    val nickname: String,
    val isDefault: Boolean
)

data class CheckoutCard(
    val id: Int,
    val network: String,        // "VISA", "MASTERCARD", etc.
    val lastFourDigits: String
)

data class CheckoutOrderItem(
    val productName: String,
    val price: Double
)

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleAddress = CheckoutAddress(
    id        = 1,
    nickname  = "Casa (Predeterminada)",
    isDefault = true
)

private val sampleCard = CheckoutCard(
    id             = 1,
    network        = "VISA",
    lastFourDigits = "1234"
)

private val sampleOrderItems = listOf(
    CheckoutOrderItem(productName = "Disco 1", price = 50.00)
)

private const val SHIPPING_COST = 5.00

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onConfirmOrder: () -> Unit = {}
) {
    // TODO (BACKEND): Cargar desde checkoutViewModel.uiState.collectAsState()
    // val uiState      by checkoutViewModel.uiState.collectAsState()
    // val address      = uiState.selectedAddress
    // val card         = uiState.selectedCard
    // val orderItems   = uiState.items
    // val shippingCost = uiState.shippingCost
    val address    = sampleAddress
    val card       = sampleCard
    val orderItems = sampleOrderItems

    val subtotal = orderItems.sumOf { it.price }
    val total    = subtotal + SHIPPING_COST

    // Controla el diálogo de confirmación
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmOrderDialog(
            total      = total,
            onConfirm  = {
                showConfirmDialog = false
                // TODO (BACKEND): checkoutViewModel.placeOrder() → esperar respuesta antes de navegar
                onConfirmOrder()
            },
            onDismiss  = { showConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CheckoutTopBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab   = null,
                onHome        = onNavigateToHome,
                onSearch      = onNavigateToSearch,
                onCart        = onNavigateBack, // Regresa al carrito
                onFavorites   = onNavigateToFavorites,
                onProfile     = onNavigateToProfile
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Secciones scrolleables ─────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ── Tarjeta: Dirección de envío ────────────────────────
                SelectionCard(
                    label       = "ENVIAR A",
                    onClick     = onNavigateToAddresses
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        // Placeholder de ícono de dirección
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE8C4B8))
                            // TODO (BACKEND + COIL): Reemplazar por ícono de mapa o foto
                        )
                        Text(
                            text       = address.nickname,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = LpzDark,
                            modifier   = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector        = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Cambiar dirección",
                            tint               = LpzDark.copy(alpha = 0.40f)
                        )
                    }
                }

                // ── Tarjeta: Método de pago ────────────────────────────
                SelectionCard(
                    label   = "PAGAR CON",
                    onClick = onNavigateToPaymentMethods
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        // Placeholder visual de tarjeta bancaria
                        Box(
                            modifier = Modifier
                                .size(42.dp, 28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LpzDark)
                            // TODO: Reemplazar con logo de red (VISA/MC) usando painterResource
                        )
                        Text(
                            text       = "${card.network} terminación ${card.lastFourDigits}",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = LpzDark,
                            modifier   = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector        = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Cambiar método de pago",
                            tint               = LpzDark.copy(alpha = 0.40f)
                        )
                    }
                }

                // ── Tarjeta: Resumen del pedido ────────────────────────
                OrderSummaryCard(
                    items        = orderItems,
                    subtotal     = subtotal,
                    shippingCost = SHIPPING_COST,
                    total        = total
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Botón Confirmar (fijo en la parte inferior) ────────────
            Surface(
                color           = LpzBeige,
                shadowElevation = 8.dp,
                tonalElevation  = 0.dp
            ) {
                Button(
                    onClick  = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text          = "CONFIRMAR",
                        fontSize      = 17.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Tarjeta genérica con etiqueta superior y contenido personalizable.
 * Usada tanto para "ENVIAR A" como para "PAGAR CON".
 */
@Composable
private fun SelectionCard(
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text          = label,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.8.sp
            )
            content()
        }
    }
}

@Composable
private fun OrderSummaryCard(
    items: List<CheckoutOrderItem>,
    subtotal: Double,
    shippingCost: Double,
    total: Double
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text          = "RESUMEN",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.8.sp
            )

            // ── Ítems del pedido ───────────────────────────────────────
            // TODO (BACKEND): Cada item vendrá de checkoutViewModel.uiState.items
            items.forEach { item ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = item.productName,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark
                    )
                    Text(
                        text       = "$${String.format("%.2f", item.price)}",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzRed
                    )
                }
            }

            HorizontalDivider(
                color     = Color.Gray.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // ── Subtotal ───────────────────────────────────────────────
            SummaryRow(
                label = "Subtotal",
                value = "$${String.format("%.2f", subtotal)}",
                isHighlighted = false
            )

            // ── Envío ──────────────────────────────────────────────────
            // TODO (BACKEND): El costo de envío vendrá del servidor según la dirección
            SummaryRow(
                label = "Envío",
                value = "$${String.format("%.2f", shippingCost)}",
                isHighlighted = false
            )

            HorizontalDivider(
                color     = Color.Gray.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // ── Total ──────────────────────────────────────────────────
            SummaryRow(
                label         = "Total a pagar",
                value         = "$${String.format("%.2f", total)}",
                isHighlighted = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            fontSize   = if (isHighlighted) 15.sp else 13.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color      = if (isHighlighted) LpzDark else LpzDark.copy(alpha = 0.55f)
        )
        Text(
            text       = value,
            fontSize   = if (isHighlighted) 15.sp else 13.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color      = if (isHighlighted) LpzRed else LpzDark.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun ConfirmOrderDialog(
    total: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        title = {
            Text(
                text       = "Confirmar pedido",
                fontWeight = FontWeight.Bold,
                color      = LpzDark
            )
        },
        text = {
            Text(
                text     = "¿Deseas realizar el pago de $${String.format("%.2f", total)}? Esta acción procesará tu orden.",
                color    = LpzDark.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text       = "Confirmar",
                    color      = LpzRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = "Cancelar",
                    color = LpzDark.copy(alpha = 0.6f)
                )
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTopBar(onNavigateBack: () -> Unit) {
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
                modifier         = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "PAGAR",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            }
        },
        actions = {
            Icon(
                painter            = painterResource(id = R.drawable.vinyl),
                contentDescription = "Logo LPZ Records",
                modifier           = Modifier
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
fun CheckoutScreenPreview() {
    CheckoutScreen()
}