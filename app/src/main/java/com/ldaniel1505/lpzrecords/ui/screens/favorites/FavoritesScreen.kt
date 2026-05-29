package com.ldaniel1505.lpzrecords.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════════════
//  MODELO DE DATOS
//  TODO (BACKEND): Mover a data/model/FavoriteItem.kt cuando el backend esté listo.
// ═══════════════════════════════════════════════════════════════════════════

data class FavoriteItem(
    val id: Int,
    val productName: String,
    val artistName: String,
    val price: Double,
    val imageUrl: String? = null,       // TODO (BACKEND + COIL): Cargar con AsyncImage
    val coverColor: Color = Color(0xFF3B2B1A) // Placeholder de color de portada
)

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleFavorites = listOf(
    FavoriteItem(
        id          = 1,
        productName = "Hey Jude",
        artistName  = "The Beatles",
        price       = 29.99,
        coverColor  = Color(0xFF3B2B1A)
    ),
    FavoriteItem(
        id          = 2,
        productName = "Hey Jude",
        artistName  = "The Beatles",
        price       = 29.99,
        coverColor  = LpzRed
    )
)

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun FavoritesScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // TODO (BACKEND): Obtener la lista de favoritos del usuario desde el ViewModel.
    // val uiState   by favoritesViewModel.uiState.collectAsState()
    // val favorites = uiState.favorites
    // val isLoading = uiState.isLoading
    var favorites by remember { mutableStateOf(sampleFavorites) }

    Scaffold(
        topBar = { FavoritesTopBar() },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = BottomNavTab.FAVORITES,
                onHome      = onNavigateToHome,
                onSearch    = onNavigateToSearch,
                onCart      = onNavigateToCart,
                onFavorites = { /* Pantalla activa, no navegar */ },
                onProfile   = onNavigateToProfile
            )
        },
        containerColor = Color.Transparent // Transparente para mostrar el fondo con rayos
    ) { innerPadding ->

        // ── Fondo con patrón de rayos retro ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawSunburstBackground() }
        ) {
            if (favorites.isEmpty()) {
                // ── Estado vacío ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint               = LpzDark.copy(alpha = 0.25f),
                            modifier           = Modifier.size(56.dp)
                        )
                        Text(
                            text       = "Aún no tienes favoritos",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color      = LpzDark.copy(alpha = 0.55f)
                        )
                        Text(
                            text     = "¡Guarda los vinilos que te gusten!",
                            fontSize = 13.sp,
                            color    = LpzDark.copy(alpha = 0.38f)
                        )
                    }
                }
            } else {
                // ── Grid de favoritos ──────────────────────────────────
                LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    modifier              = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 14.dp),
                    verticalArrangement   = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding        = PaddingValues(vertical = 16.dp)
                ) {
                    items(favorites, key = { it.id }) { item ->
                        FavoriteCard(
                            item          = item,
                            onRemoveFavorite = {
                                // TODO (BACKEND): favoritesViewModel.removeFavorite(item.id)
                                favorites = favorites.filter { it.id != item.id }
                            },
                            onAddToCart = {
                                // TODO (BACKEND): cartViewModel.addToCart(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun FavoriteCard(
    item: FavoriteItem,
    onRemoveFavorite: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {

            // ── Portada del disco ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(item.coverColor)
            ) {
                /*
                 * TODO (BACKEND + COIL): Reemplazar este Box con:
                 *   AsyncImage(
                 *       model            = item.imageUrl,
                 *       contentDescription = item.productName,
                 *       contentScale     = ContentScale.Crop,
                 *       modifier         = Modifier.fillMaxSize()
                 *   )
                 * Dependencia: implementation("io.coil-kt:coil-compose:2.6.0")
                 */

                // ── Botón de corazón (quitar de favoritos) ─────────────
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.90f))
                        .clickable(onClick = onRemoveFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        // TODO (BACKEND): Cambiar entre Favorite/FavoriteBorder
                        // según el estado real del ViewModel
                        imageVector        = Icons.Default.Favorite,
                        contentDescription = "Quitar de favoritos",
                        tint               = LpzRed,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            // ── Info del producto ──────────────────────────────────────
            Column(
                modifier = Modifier.padding(
                    start  = 10.dp,
                    end    = 6.dp,
                    top    = 8.dp,
                    bottom = 8.dp
                )
            ) {
                Text(
                    text       = item.productName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = item.artistName,
                    fontSize = 11.sp,
                    color    = LpzDark.copy(alpha = 0.50f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "$${String.format("%.2f", item.price)}",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzRed
                    )

                    // ── Botón agregar al carrito ───────────────────────
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                            .clickable(onClick = onAddToCart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ShoppingCart,
                            contentDescription = "Añadir al carrito",
                            tint               = LpzDark,
                            modifier           = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  FONDO: Patrón de rayos retro (sunburst)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Dibuja el patrón de rayos retro que se ve en el fondo del diseño Figma.
 * Alterna entre dos tonos de beige para crear el efecto de rayas.
 */
private fun DrawScope.drawSunburstBackground() {
    // Color base del fondo
    drawRect(color = Color(0xFFF4F1E6))

    val centerX   = size.width / 2f
    val centerY   = size.height / 2f
    val radius    = size.width.coerceAtLeast(size.height) * 1.4f
    val rayCount  = 24
    val angleStep = (2 * Math.PI / rayCount).toFloat()

    val rayColorA = Color(0xFFEDE8D5) // Beige ligeramente más oscuro
    val rayColorB = Color(0xFFF4F1E6) // Beige base

    for (i in 0 until rayCount) {
        val startAngle = i * angleStep - angleStep / 2f
        val endAngle   = startAngle + angleStep

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(
                centerX + radius * cos(startAngle),
                centerY + radius * sin(startAngle)
            )
            lineTo(
                centerX + radius * cos(endAngle),
                centerY + radius * sin(endAngle)
            )
            close()
        }

        drawPath(
            path  = path,
            color = if (i % 2 == 0) rayColorA else rayColorB
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesTopBar() {
    Column{
        TopAppBar(
            title = {
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "FAVORITOS",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark,
                        modifier = Modifier.padding(start = 28.dp)
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
        HorizontalDivider(color = LpzDark, thickness = 1.dp)
    }

}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    FavoritesScreen()
}