package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════
//  MODELO DE DATOS
//  TODO (BACKEND): Mover a data/model/PaymentCard.kt cuando el backend esté listo.
//  IMPORTANTE: Nunca guardar el número completo de tarjeta en el cliente.
// ═══════════════════════════════════════════════════════════════════════════

data class PaymentCard(
    val id: Int,
    val cardHolder: String,
    val lastFourDigits: String,
    val expiryDate: String,
    val network: CardNetwork
)

enum class CardNetwork(val displayName: String) {
    VISA("VISA"),
    MASTERCARD("MASTERCARD"),
    AMEX("AMEX"),
    OTHER("- - -")
}

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleCards = listOf(
    PaymentCard(
        id             = 1,
        cardHolder     = "Luis D. Ontiveros",
        lastFourDigits = "1234",
        expiryDate     = "12/28",
        network        = CardNetwork.VISA
    )
)

private fun String.toMaskedCardNumber(): String = "•••• •••• ••••  $this"

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun PaymentMethodsScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // TODO (BACKEND): Cargar tarjetas guardadas del usuario desde el ViewModel.
    // val uiState by paymentViewModel.uiState.collectAsState()
    // val cards   = uiState.cards
    val cards = sampleCards

    var cardToDelete by remember { mutableStateOf<PaymentCard?>(null) }

    if (cardToDelete != null) {
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            containerColor   = Color.White,
            title = {
                Text(
                    text       = "Eliminar tarjeta",
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            },
            text = {
                Text(
                    text     = "¿Deseas eliminar la tarjeta terminada en ${cardToDelete?.lastFourDigits}?",
                    color    = LpzDark.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // TODO (BACKEND): paymentViewModel.deleteCard(cardToDelete!!.id)
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
        topBar = { PaymentMethodsTopBar() },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(vertical = 24.dp)
        ) {
            items(cards, key = { it.id }) { card ->
                CreditCardVisual(
                    card     = card,
                    onDelete = { cardToDelete = card }
                )
            }

            if (cards.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = "No tienes tarjetas guardadas",
                            fontSize = 15.sp,
                            color    = LpzDark.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            item {
                AddCardButton(
                    onClick = {
                        // TODO (BACKEND): navController.navigate(Screen.NewCard.route)
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CreditCardVisual(
    card: PaymentCard,
    onDelete: () -> Unit
) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1C1C1C), Color(0xFF2E2E2E))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(18.dp))
                .then(Modifier.background(brush = cardGradient))
                .padding(22.dp)
        ) {
            Text(
                text       = card.network.displayName,
                modifier   = Modifier.align(Alignment.TopEnd),
                color      = Color.White,
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle  = FontStyle.Italic
            )

            Text(
                text       = card.lastFourDigits.toMaskedCardNumber(),
                modifier   = Modifier.align(Alignment.Center),
                color      = Color.White,
                fontSize   = 19.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Row(
                modifier              = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text          = "TITULAR",
                        fontSize      = 9.sp,
                        color         = Color.White.copy(alpha = 0.55f),
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.Medium
                    )
                    Text(
                        // TODO (BACKEND): card.cardHolder vendrá del objeto User autenticado
                        text       = card.cardHolder,
                        fontSize   = 13.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(
                    horizontalAlignment   = Alignment.End,
                    verticalArrangement   = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text          = "VENCE",
                        fontSize      = 9.sp,
                        color         = Color.White.copy(alpha = 0.55f),
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.Medium
                    )
                    Text(
                        // TODO (BACKEND): card.expiryDate vendrá cifrado desde el servidor
                        text       = card.expiryDate,
                        fontSize   = 13.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text       = "ELIMINAR",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = LpzDark.copy(alpha = 0.45f),
                letterSpacing = 0.5.sp,
                modifier   = Modifier
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
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                val strokePx   = 1.5.dp.toPx()
                val dashPx     = 12.dp.toPx()
                val gapPx      = 6.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
                drawRoundRect(
                    color        = dashedColor,
                    style        = Stroke(width = strokePx, pathEffect = pathEffect),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text          = "+ AÑADIR TARJETA",
            fontSize      = 15.sp,
            fontWeight    = FontWeight.Medium,
            color         = LpzDark.copy(alpha = 0.58f),
            letterSpacing = 0.5.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodsTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier         = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "TARJETAS",
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
fun PaymentMethodsScreenPreview() {
    PaymentMethodsScreen()
}