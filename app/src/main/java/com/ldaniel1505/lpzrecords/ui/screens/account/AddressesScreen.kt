package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════
//  MODELO DE DATOS
//  TODO (BACKEND): Mover a data/model/Address.kt cuando el backend esté listo.
// ═══════════════════════════════════════════════════════════════════════════

data class Address(
    val id: Int,
    val nickname: String,      // Ej: "CASA", "TRABAJO", "OTRO"
    val street: String,        // Calle y colonia
    val cityStateZip: String,  // Ciudad, Estado, C.P.
    val country: String,
    val isDefault: Boolean = false
)

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleAddresses = listOf(
    Address(
        id          = 1,
        nickname    = "CASA",
        street      = "Calle San Fernando, Col. Misiones",
        cityStateZip = "La paz, BCS, C.P. 23083",
        country     = "México",
        isDefault   = true
    )
)

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AddressesScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // TODO (BACKEND): Cargar direcciones del usuario desde el ViewModel.
    // val uiState   by addressesViewModel.uiState.collectAsState()
    // val addresses = uiState.addresses
    val addresses = sampleAddresses

    // Controla cuál dirección está pendiente de eliminar
    var addressToDelete by remember { mutableStateOf<Address?>(null) }

    // ── Diálogo de confirmación de eliminación ─────────────────────────
    if (addressToDelete != null) {
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            containerColor   = Color.White,
            title = {
                Text(
                    text       = "Eliminar dirección",
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            },
            text = {
                Text(
                    text  = "¿Estás seguro de que deseas eliminar la dirección \"${addressToDelete?.nickname}\"? Esta acción no se puede deshacer.",
                    color = LpzDark.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // TODO (BACKEND): addressesViewModel.deleteAddress(addressToDelete!!.id)
                    addressToDelete = null
                }) {
                    Text("Eliminar", color = LpzRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("Cancelar", color = LpzDark.copy(alpha = 0.6f))
                }
            }
        )
    }

    Scaffold(
        topBar = { AddressesTopBar() },
        bottomBar = {
            AddressesBottomBar(
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // ── Tarjetas de dirección ──────────────────────────────────
            items(addresses, key = { it.id }) { address ->
                AddressCard(
                    address  = address,
                    onEdit   = {
                        // TODO (BACKEND): Navegar a pantalla de edición de dirección
                        // navController.navigate(Screen.EditAddress.route + "/${address.id}")
                    },
                    onDelete = { addressToDelete = address }
                )
            }

            // ── Botón para añadir nueva dirección ─────────────────────
            item {
                AddAddressButton(
                    onClick = {
                        // TODO (BACKEND): Navegar a pantalla de nueva dirección
                        // navController.navigate(Screen.NewAddress.route)
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
private fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Fila superior: badge + apodo ───────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (address.isDefault) {
                    // Badge "PREDETERMINADA"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LpzRed
                    ) {
                        Text(
                            text          = "PREDETERMINADA",
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = Color.White,
                            letterSpacing = 0.4.sp,
                            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text       = address.nickname,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Detalles de la dirección ───────────────────────────────
            Text(
                text       = address.street,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = LpzDark
            )
            Text(
                text     = address.cityStateZip,
                fontSize = 12.sp,
                color    = LpzDark.copy(alpha = 0.55f)
            )
            Text(
                text     = address.country,
                fontSize = 12.sp,
                color    = LpzDark.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color     = Color.Gray.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Acciones: Editar / Eliminar ────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                Text(
                    text       = "EDITAR",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzRed,
                    letterSpacing = 0.5.sp,
                    modifier   = Modifier
                        .clickable(onClick = onEdit)
                        .padding(vertical = 2.dp)
                )
                Text(
                    text       = "ELIMINAR",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color      = LpzDark.copy(alpha = 0.55f),
                    letterSpacing = 0.5.sp,
                    modifier   = Modifier
                        .clickable(onClick = onDelete)
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Botón con borde discontinuo para añadir una nueva dirección.
 * El efecto de guiones se logra con [PathEffect.dashPathEffect] pintado
 * directamente en el Canvas, sin necesidad de dependencias externas.
 */
@Composable
private fun AddAddressButton(onClick: () -> Unit) {
    val dashedBorderColor = LpzDark.copy(alpha = 0.30f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                val strokePx  = 1.5.dp.toPx()
                val dashPx    = 12.dp.toPx()
                val gapPx     = 6.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dashPx, gapPx), 0f
                )
                drawRoundRect(
                    color        = dashedBorderColor,
                    style        = Stroke(width = strokePx, pathEffect = pathEffect),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text          = "+ AÑADIR DIRECCION",
            fontSize      = 15.sp,
            fontWeight    = FontWeight.Medium,
            color         = LpzDark.copy(alpha = 0.60f),
            letterSpacing = 0.5.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressesTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier         = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "DIRECCIONES",
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
//  BOTTOM NAVIGATION BAR
//  "Perfil" permanece resaltado al ser una subpantalla del módulo de cuenta.
//  TODO: Extraer a ui/components/LpzBottomNavBar.kt junto con las demás pantallas.
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AddressesBottomBar(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color           = LpzBeige,
        shadowElevation = 12.dp,
        tonalElevation  = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .navigationBarsPadding()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            BottomNavItem(icon = Icons.Default.Home,           label = "Inicio",    isSelected = false, onClick = onHome)
            BottomNavItem(icon = Icons.Default.Search,         label = "Buscar",    isSelected = false, onClick = onSearch)

            // ── Botón central del carrito ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(LpzRed)
                    .clickable(onClick = onCart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito de compras",
                    tint               = Color.White,
                    modifier           = Modifier.size(24.dp)
                )
            }

            BottomNavItem(icon = Icons.Default.FavoriteBorder, label = "Favoritos", isSelected = false, onClick = onFavorites)
            BottomNavItem(icon = Icons.Default.Person,         label = "Perfil",    isSelected = true,  onClick = onProfile)
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) LpzRed else LpzDark
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = label, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddressesScreenPreview() {
    AddressesScreen()
}