package com.ldaniel1505.lpzrecords.ui.screens.checkout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.CartItem
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel
import com.ldaniel1505.lpzrecords.viewmodel.checkout.CheckoutViewModel
import java.util.Locale

@Composable
fun CheckoutScreen(
    checkoutViewModel: CheckoutViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    addressViewModel: AddressViewModel = viewModel(),
    paymentMethodsViewModel: PaymentMethodsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onConfirmOrder: () -> Unit = {}
) {
    LaunchedEffect(cartViewModel) {
        checkoutViewModel.loadFromCart(cartViewModel)
    }

    LaunchedEffect(Unit) {
        addressViewModel.fetchAddresses()
        paymentMethodsViewModel.fetchPaymentMethods()
        checkoutViewModel.loadCustomerData(addressViewModel, paymentMethodsViewModel)
    }

    val uiState by checkoutViewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.orderSuccess) {
        if (uiState.orderSuccess) {
            cartViewModel.clearCart()
            checkoutViewModel.resetOrderSuccess()
            onConfirmOrder()
        }
    }

    if (showConfirmDialog) {
        ConfirmOrderDialog(
            total = uiState.total,
            isSubmitting = uiState.isSubmitting,
            onConfirm = {
                showConfirmDialog = false
                checkoutViewModel.confirmarCompra()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CheckoutTopBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = null,
                onHome = onNavigateToHome,
                onSearch = onNavigateToSearch,
                onCart = onNavigateBack,
                onFavorites = onNavigateToFavorites,
                onProfile = onNavigateToProfile
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                SelectionCard(
                    label = "ENVIAR A",
                    onClick = onNavigateToAddresses
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE8C4B8))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.selectedAddress.label,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LpzDark
                            )
                            Text(
                                text = uiState.selectedAddress.summary,
                                fontSize = 13.sp,
                                color = LpzDark.copy(alpha = 0.58f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Cambiar direccion",
                            tint = LpzDark.copy(alpha = 0.40f)
                        )
                    }
                }

                SelectionCard(
                    label = "PAGAR CON",
                    onClick = onNavigateToPaymentMethods
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 42.dp, height = 28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LpzDark)
                        )
                        Text(
                            text = uiState.selectedPaymentMethod.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LpzDark,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Cambiar metodo de pago",
                            tint = LpzDark.copy(alpha = 0.40f)
                        )
                    }
                }

                OrderSummaryCard(
                    items = uiState.items,
                    subtotal = uiState.subtotal,
                    shippingCost = uiState.shippingCost,
                    total = uiState.total
                )

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = LpzRed
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Surface(
                color = LpzBeige,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = uiState.items.isNotEmpty() && !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (uiState.isSubmitting) "PROCESANDO" else "CONFIRMAR ORDEN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionCard(
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.8.sp
            )
            content()
        }
    }
}

@Composable
private fun OrderSummaryCard(
    items: List<CartItem>,
    subtotal: Double,
    shippingCost: Double,
    total: Double
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "RESUMEN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.8.sp
            )

            if (items.isEmpty()) {
                Text(
                    text = "No hay productos en el carrito.",
                    fontSize = 14.sp,
                    color = LpzDark.copy(alpha = 0.55f)
                )
            } else {
                items.forEach { item ->
                    SummaryItemRow(item = item)
                }
            }

            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            SummaryRow(
                label = "Subtotal",
                value = money(subtotal),
                isHighlighted = false
            )

            SummaryRow(
                label = "Envio",
                value = money(shippingCost),
                isHighlighted = false
            )

            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            SummaryRow(
                label = "Total a pagar",
                value = money(total),
                isHighlighted = true
            )
        }
    }
}

@Composable
private fun SummaryItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.quantity} x ${item.selectedFormat}",
                fontSize = 12.sp,
                color = LpzDark.copy(alpha = 0.55f)
            )
        }
        Text(
            text = money(item.product.price * item.quantity),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = LpzRed
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isHighlighted) 15.sp else 13.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) LpzDark else LpzDark.copy(alpha = 0.55f)
        )
        Text(
            text = value,
            fontSize = if (isHighlighted) 15.sp else 13.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) LpzRed else LpzDark.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun ConfirmOrderDialog(
    total: Double,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Confirmar pedido",
                fontWeight = FontWeight.Bold,
                color = LpzDark
            )
        },
        text = {
            Text(
                text = "Deseas confirmar esta orden por ${money(total)}?",
                color = LpzDark.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onConfirm
            ) {
                Text(
                    text = "Confirmar",
                    color = LpzRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar",
                    color = LpzDark.copy(alpha = 0.6f)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTopBar(onNavigateBack: () -> Unit) {
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
                    text = "PAGAR",
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

private fun money(value: Double): String {
    return "$${String.format(Locale.US, "%.2f", value)}"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CheckoutScreenPreview() {
    CheckoutScreen()
}
