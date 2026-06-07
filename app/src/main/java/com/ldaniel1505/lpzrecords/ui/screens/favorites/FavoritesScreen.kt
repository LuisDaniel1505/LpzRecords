package com.ldaniel1505.lpzrecords.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.CartSnackbarEffect
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.favorites.FavoritesViewModel
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProduct: (String) -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onToggleFavorite: (Product) -> Unit = {},
    cartViewModel: CartViewModel = viewModel()
) {
    val favorites by favoritesViewModel.favoriteProducts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    CartSnackbarEffect(cartViewModel, snackbarHostState)

    Scaffold(
        topBar = { FavoritesTopBar() },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = BottomNavTab.FAVORITES,
                onHome = onNavigateToHome,
                onSearch = onNavigateToSearch,
                onCart = onNavigateToCart,
                onFavorites = {},
                onProfile = onNavigateToProfile
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawSunburstBackground() }
        ) {
            if (favorites.isEmpty()) {
                EmptyFavoritesState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(favorites, key = { it.id }) { product ->
                        FavoriteCard(
                            product = product,
                            onProductClick = { onNavigateToProduct(product.id) },
                            onRemoveFavorite = { onToggleFavorite(product) },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = LpzDark.copy(alpha = 0.25f),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "Aún no tienes favoritos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LpzDark.copy(alpha = 0.55f)
            )
            Text(
                text = "Guarda los discos que te gusten.",
                fontSize = 13.sp,
                color = LpzDark.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun FavoriteCard(
    product: Product,
    onProductClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        onClick = onProductClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(LpzDark)
            ) {
                AsyncImage(
                    model = product.img_url,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Quitar de favoritos",
                        tint = LpzRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = product.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.artist?.name ?: "Artista desconocido",
                    fontSize = 11.sp,
                    color = LpzDark.copy(alpha = 0.50f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = money(product.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzRed
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                            .clickable(onClick = onAddToCart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Agregar al carrito",
                            tint = LpzDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSunburstBackground() {
    drawRect(color = Color(0xFFF4F1E6))

    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.width.coerceAtLeast(size.height) * 1.4f
    val rayCount = 24
    val angleStep = (2 * Math.PI / rayCount).toFloat()

    val rayColorA = Color(0xFFEDE8D5)
    val rayColorB = Color(0xFFF4F1E6)

    for (i in 0 until rayCount) {
        val startAngle = i * angleStep - angleStep / 2f
        val endAngle = startAngle + angleStep

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(centerX + radius * cos(startAngle), centerY + radius * sin(startAngle))
            lineTo(centerX + radius * cos(endAngle), centerY + radius * sin(endAngle))
            close()
        }

        drawPath(path = path, color = if (i % 2 == 0) rayColorA else rayColorB)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesTopBar() {
    Column {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FAVORITOS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark,
                        modifier = Modifier.padding(start = 28.dp)
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
        HorizontalDivider(color = LpzDark, thickness = 1.dp)
    }
}

private fun money(value: Double): String {
    return "$${String.format(Locale.US, "%.2f", value)}"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    FavoritesScreen()
}
