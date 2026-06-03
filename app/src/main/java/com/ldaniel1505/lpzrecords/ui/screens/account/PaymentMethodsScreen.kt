package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.PaymentMethod
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel

@Composable
fun PaymentMethodsScreen(
    paymentMethodsViewModel: PaymentMethodsViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPaymentMethodForm: () -> Unit = {}
) {
    val uiState by paymentMethodsViewModel.uiState.collectAsState()
    var cardToDelete by remember { mutableStateOf<PaymentMethod?>(null) }

    LaunchedEffect(Unit) {
        paymentMethodsViewModel.fetchPaymentMethods()
    }

    cardToDelete?.let { pendingCard ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Eliminar tarjeta",
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            },
            text = {
                Text(
                    text = "Deseas eliminar la tarjeta terminada en ${pendingCard.lastFourDigits}?",
                    color = LpzDark.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    paymentMethodsViewModel.deletePaymentMethod(pendingCard.id)
                    cardToDelete = null
                }) {
                    Text("Eliminar", color = LpzRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancelar", color = LpzDark.copy(alpha = 0.6f))
                }
            }
        )
    }

    Scaffold(
        topBar = { PaymentMethodsTopBar(onNavigateBack = onNavigateToProfile) },
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
            uiState.isLoading && uiState.paymentMethods.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LpzRed)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    uiState.errorMessage?.let { message ->
                        item {
                            Text(
                                text = message,
                                color = LpzRed,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (uiState.paymentMethods.isEmpty()) {
                        item {
                            EmptyPaymentState()
                        }
                    }

                    items(uiState.paymentMethods, key = { it.id }) { card ->
                        CreditCardVisual(
                            card = card,
                            isSelected = uiState.selectedPaymentMethod?.id == card.id,
                            onSelect = { paymentMethodsViewModel.selectPaymentMethod(card) },
                            onDelete = { cardToDelete = card }
                        )
                    }

                    item {
                        AddCardButton(onClick = onNavigateToPaymentMethodForm)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPaymentState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No tienes tarjetas guardadas",
            fontSize = 15.sp,
            color = LpzDark.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun CreditCardVisual(
    card: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val cardGradient = Brush.linearGradient(
        colors = if (isSelected) {
            listOf(LpzRed, Color(0xFF2E2E2E))
        } else {
            listOf(Color(0xFF1C1C1C), Color(0xFF2E2E2E))
        }
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .background(brush = cardGradient, shape = RoundedCornerShape(18.dp))
                .clickable(onClick = onSelect)
                .padding(22.dp)
        ) {
            Text(
                text = card.provider,
                modifier = Modifier.align(Alignment.TopEnd),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            )

            Text(
                text = card.lastFourDigits.toMaskedCardNumber(),
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 19.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isSelected) "SELECCIONADA" else "METODO",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = card.type.uppercase(),
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "USAR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .clickable(onClick = onSelect)
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            )
            Text(
                text = "ELIMINAR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun AddCardButton(onClick: () -> Unit) {
    val dashedColor = LpzDark.copy(alpha = 0.28f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                val strokePx = 1.5.dp.toPx()
                val dashPx = 12.dp.toPx()
                val gapPx = 6.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
                drawRoundRect(
                    color = dashedColor,
                    style = Stroke(width = strokePx, pathEffect = pathEffect),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ ANADIR TARJETA",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = LpzDark.copy(alpha = 0.58f),
            letterSpacing = 0.5.sp
        )
    }
}

private fun String.toMaskedCardNumber(): String = "**** **** ****  $this"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodsTopBar(onNavigateBack: () -> Unit) {
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
                    text = "TARJETAS",
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
fun PaymentMethodsScreenPreview() {
    PaymentMethodsScreen()
}
