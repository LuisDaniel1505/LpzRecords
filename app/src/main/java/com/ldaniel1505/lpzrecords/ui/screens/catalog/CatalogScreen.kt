package com.ldaniel1505.lpzrecords.ui.screens.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.ui.theme.*
import com.ldaniel1505.lpzrecords.viewmodel.catalog.ProductViewModel

// ═══════════════════════════════════════════════════════════════════════════
//  MODELO DE DATOS
//  TODO (BACKEND): Mover a data/model/Product.kt cuando el backend esté listo.
//  Reemplazar [placeholderColor] con [imageUrl: String]
// ═══════════════════════════════════════════════════════════════════════════

/*

    //Ya no se usa, se usa el modelo Product

data class ProductItem(
    val id: Int,
    val title: String,
    val artist: String,
    val price: String,
    val placeholderColor: Color, // TODO (BACKEND): Cambiar a imageUrl: String
    val isFavorite: Boolean = false
)
*/

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────


private val catalogCategories = listOf("Todos", "Rock", "Jazz", "Pop", "Classical", "Funk")

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun CatalogScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProduct: (Int) -> Unit = {}
) {

    val viewModel: ProductViewModel = viewModel()


    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    val sampleProducts = viewModel.products
    val products = sampleProducts


    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    // TODO (BACKEND): Reemplazar con datos del ViewModel:
    // val uiState by catalogViewModel.uiState.collectAsState()
    // val products = uiState.products

    Scaffold(
        topBar = { CatalogTopBar() },
        bottomBar = {
            CatalogBottomBar(
                onHome = onNavigateToHome,
                onSearch = onNavigateToSearch,
                onCart = onNavigateToCart,
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Barra de búsqued
            CatalogSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    // TODO (BACKEND): catalogViewModel.onSearchQueryChanged(it)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sección Categorías
            Text(
                text = "CATEGORIAS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )
            Spacer(modifier = Modifier.height(10.dp))
            CategoryRow(
                categories = catalogCategories,
                selected = selectedCategory,
                onSelect = {
                    selectedCategory = it
                    // TODO (BACKEND): catalogViewModel.filterByCategory(it)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Header Novedades
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NOVEDADES",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed
                )
                IconButton(
                    onClick = { /* TODO: navController.navigate(Screen.Novedades.route) */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Ver todas las novedades",
                        tint = LpzRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Productos cargados: ${viewModel.products.size}")
            Text("Error: ${viewModel.errorMessage ?: "ninguno"}")
            // Grid de Productos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(products, key = { product -> product.id },
                    ) { product ->
                    ProductCard(
                        product = product,
                        onFavoriteClick = {
                            // TODO (BACKEND): catalogViewModel.toggleFavorite(product.id)
                        },
                        onAddToCartClick = {
                            // TODO (BACKEND): catalogViewModel.addToCart(product.id)
                        },
                        onProductClick = { onNavigateToProduct(product.id) }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

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
    onFavoriteClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onProductClick: () -> Unit
) {
    Card(
        onClick = onProductClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Portada del disco
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Gray)
            ) {
                /*
                 * TODO Esta es la parte del backend y las instrucciones:
                 * Teniendo URLs de imagen, reemplaza este Box por:
                 *
                 *   AsyncImage(
                 *       model = product.imageUrl,
                 *       contentDescription = product.title,
                 *       contentScale = ContentScale.Crop,
                 *       modifier = Modifier.fillMaxSize()
                 *   )
                 *
                 * Y se agrega a build.gradle (app):
                 *   implementation("io.coil-kt:coil-compose:2.6.0")
                 */

                /* Botón de favorito flotante sobre la imagen
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.30f))
                ) {
                    Icon(
                        imageVector = if (product.isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (product.isFavorite) "Quitar favorito" else "Agregar a favoritos",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            */
            }
                // Info del producto
                Column(
                    modifier = Modifier.padding(
                        start = 10.dp, end = 6.dp, top = 8.dp, bottom = 4.dp
                    )
                ) {
                    Text(
                        text = product.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                    text = product.artist.name,
                    fontSize = 11.sp,
                    color = LpzDark.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                    )
                    Text(
                    text = product.category.name,
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
                        Text(
                            text = "" + product.price,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LpzDark
                        )
                        IconButton(
                            onClick = onAddToCartClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Añadir al carrito",
                                tint = LpzDark,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
//  BOTTOM NAVIGATION BAR
//  TODO: Extraer a ui/components/LpzBottomNavBar.kt cuando otras pantallas
//  también necesiten esta barra (HomeScreen, FavoritesScreen, ProfileScreen…).
//  Este se va a cambiar a un componente para no estar repitiendo codigo
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatalogScreenPreview() {
    CatalogScreen()
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = LpzDark,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = LpzDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CatalogBottomBar(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color = LpzBeige,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .navigationBarsPadding()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(icon = Icons.Default.Home, label = "Inicio", onClick = onHome)
            BottomNavItem(icon = Icons.Default.Search, label = "Buscar", onClick = onSearch)

            // Botón central del carrito
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(LpzRed)
                    .clickable(onClick = onCart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito de compras",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            BottomNavItem(
                icon = Icons.Default.FavoriteBorder,
                label = "Favoritos",
                onClick = onFavorites
            )
            BottomNavItem(icon = Icons.Default.Person, label = "Perfil", onClick = onProfile)
        }
    }
}
