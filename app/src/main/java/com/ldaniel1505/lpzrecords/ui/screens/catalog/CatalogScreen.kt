package com.ldaniel1505.lpzrecords.ui.screens.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.ldaniel1505.lpzrecords.ui.components.CartSnackbarEffect
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.catalog.ProductViewModel
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel

@Composable
fun CatalogScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProduct: (String) -> Unit = {},
    favoriteProductIds: Set<String> = emptySet(),
    onAddToCart: (Product) -> Unit = {},
    onToggleFavorite: (Product) -> Unit = {},
    cartViewModel: CartViewModel = viewModel()
) {
    val viewModel: ProductViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    CartSnackbarEffect(cartViewModel, snackbarHostState)

    val products = viewModel.filteredProducts
    val searchQuery = viewModel.searchQuery
    val selectedCategory = viewModel.selectedCategory
    val categoryOptions = viewModel.categoryOptions

    Scaffold(
        topBar = { CatalogTopBar() },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = null,
                onHome = onNavigateToHome,
                onSearch = onNavigateToSearch,
                onCart = onNavigateToCart,
                onFavorites = onNavigateToFavorites,
                onProfile = onNavigateToProfile
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            CatalogSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CATEGORIAS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )

            Spacer(modifier = Modifier.height(10.dp))

            CategoryRow(
                categories = categoryOptions,
                selected = selectedCategory,
                onSelect = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PRODUCTOS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = LpzRed)
                    }
                }

                viewModel.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.errorMessage ?: "No se pudieron cargar los productos.",
                            color = LpzRed,
                            fontSize = 14.sp
                        )
                    }
                }

                products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No encontramos productos con esos filtros.",
                            color = LpzDark.copy(alpha = 0.55f),
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(products, key = { product -> product.id }) { product ->
                            ProductCard(
                                product = product,
                                isFavorite = favoriteProductIds.contains(product.id),
                                onFavoriteClick = { onToggleFavorite(product) },
                                onAddToCartClick = { onAddToCart(product) },
                                onProductClick = { onNavigateToProduct(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CATALOGO",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogSearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = { Text("Buscar", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun CategoryRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            Surface(
                onClick = { onSelect(category) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) LpzDark else Color.Transparent,
                border = if (!isSelected) BorderStroke(1.dp, LpzDark) else null
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else LpzDark
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onProductClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onProductClick),
        onClick = onProductClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Gray)
            ) {
                AsyncImage(
                    model = product.img_url,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.90f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFavorite) LpzRed else LpzDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 6.dp,
                    top = 8.dp,
                    bottom = 4.dp
                )
            ) {
                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.artist?.name ?: "Artista desconocido",
                    fontSize = 11.sp,
                    color = LpzDark.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category?.name ?: "Sin categoria",
                    fontSize = 11.sp,
                    color = LpzDark.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$${product.price}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        IconButton(
                            onClick = onAddToCartClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Agregar al carrito",
                                tint = LpzDark,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatalogScreenPreview() {
    CatalogScreen()
}
