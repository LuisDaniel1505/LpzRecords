package com.ldaniel1505.lpzrecords.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
//  MODELO DE DATOS
//  TODO (BACKEND): Mover a data/model/CartItem.kt cuando el backend esté listo.
// ═══════════════════════════════════════════════════════════════════════════

data class CartItem(
    val id: Int,
    val productName: String,
    val artistName: String,
    val price: Double,
    val imageUrl: String? = null  // TODO (BACKEND + COIL): Cargar con AsyncImage
)

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleCartItems = listOf(
    CartItem(id = 1, productName = "Album 123", artistName = "Artista", price = 50.00),
    CartItem(id = 2, productName = "Album 123", artistName = "Artista", price = 50.00),
    CartItem(id = 3, productName = "Album 123", artistName = "Artista", price = 50.00)
)

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun CartScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {}
) {
    // TODO (BACKEND): Obtener ítems del carrito desde el ViewModel.
    // val uiState   by cartViewModel.uiState.collectAsState()
    // val cartItems = uiState.items
    // val isLoading = uiState.isLoading
    var cartItems by remember { mutableStateOf(sampleCartItems) }

    // TODO (BACKEND): El total debe calcularse en el ViewModel, no en la UI.
    val total = cartItems.sumOf { it.price }

    Scaffold(
        topBar = {
            CartTopBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab   = null, // El carrito es el botón central, siempre en rojo
                onHome        = onNavigateToHome,
                onSearch      = onNavigateToSearch,
                onCart        = { /* Ya estamos en el carrito */ },
                onFavorites   = onNavigateToFavorites,
                onProfile     = onNavigateToProfile
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->

        if (cartItems.isEmpty()) {
            // ── Estado vacío ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text       = "Tu carrito está vacío",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = LpzDark.copy(alpha = 0.55f)
                    )
                    Text(
                        text     = "¡Agrega vinilos desde el catálogo!",
                        fontSize = 13.sp,
                        color    = LpzDark.copy(alpha = 0.40f)
                    )
                }
            }
        } else {
            // ── Contenido principal ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ── Lista de ítems ───────────────────────
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding      = PaddingValues(vertical = 16.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemCard(
                            item     = item,
                            onDelete = {
                                // TODO (BACKEND): cartViewModel.removeItem(item.id)
                                cartItems = cartItems.filter { it.id != item.id }
                            }
                        )
                    }
                }

                // ── Panel inferior: Total + Botón ──────────────────────
                CartCheckoutPanel(
                    total       = total,
                    onCheckout  = {
                        // TODO (BACKEND): cartViewModel.checkout() → navegar a pantalla de pago
                        onNavigateToCheckout()
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
private fun CartItemCard(
    item: CartItem,
    onDelete: () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Portada del álbum ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LpzDark)
            ) {
                /*
                 * TODO (BACKEND + COIL): Reemplazar este Box con:
                 *   AsyncImage(
                 *       model = item.imageUrl,
                 *       contentDescription = item.productName,
                 *       contentScale = ContentScale.Crop,
                 *       modifier = Modifier.fillMaxSize()
                 *   )
                 * Dependencia: implementation("io.coil-kt:coil-compose:2.6.0")
                 */
            }

            // ── Info del producto ──────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = item.productName,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
                Text(
                    text     = item.artistName,
                    fontSize = 12.sp,
                    color    = LpzDark.copy(alpha = 0.50f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = "$${String.format("%.2f", item.price)}",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzRed
                )
            }

            // ── Botón eliminar ─────────────────────────────────────────
            IconButton(
                onClick  = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = "Eliminar ${item.productName}",
                    tint               = LpzRed,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CartCheckoutPanel(
    total: Double,
    onCheckout: () -> Unit
) {
    Surface(
        color           = LpzBeige,
        shadowElevation = 8.dp,
        tonalElevation  = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Fila Total ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Total",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
                Text(
                    // TODO (BACKEND): El total debe venir del ViewModel, no calcularse aquí.
                    text       = "$${String.format("%.2f", total)}",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            }

            // ── Botón Pagar ────────────────────────────────────────────
            Button(
                onClick  = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                shape  = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text          = "PAGAR AHORA",
                    fontSize      = 17.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White,
                    letterSpacing = 1.sp
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
private fun CartTopBar(onNavigateBack: () -> Unit) {
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
                    text       = "CARRITO",
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
fun CartScreenPreview() {
    CartScreen()
}