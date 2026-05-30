package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════════════
//  MODELOS DE DATOS LOCALES
//  TODO (BACKEND): Reemplazar con las entidades reales de
//  data/model/Product.kt (el mismo que usa CatalogScreen / ProductDetail)
// ═══════════════════════════════════════════════════════════════════════════

data class AdminProduct(
    val id: Int,
    val name: String,
    val artist: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String? = null,       // TODO (BACKEND + COIL): AsyncImage
    val coverColor: Color = Color(0xFF1A1A1A)
)

// ── Datos de ejemplo — eliminar cuando el ViewModel provea datos reales ──────
private val sampleAdminProducts = listOf(
    AdminProduct(1, "Album 123", "Artista", "Rock",      50.00, 12, coverColor = Color(0xFF1A1A1A)),
    AdminProduct(2, "Album 123", "Artista", "Jazz",      50.00, 5,  coverColor = Color(0xFF1A1A1A)),
    AdminProduct(3, "Album 123", "Artista", "Classical", 50.00, 0,  coverColor = Color(0xFF1A1A1A))
)

// ── Categorías disponibles para el dropdown ───────────────────────────────
private val availableCategories = listOf("Rock", "Jazz", "Pop", "Classical", "Funk", "Blues", "Otro")

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun ProductControlScreen(
    onNavigateBack: () -> Unit = {}
    // TODO (BACKEND): Inyectar ViewModel:
    // viewModel: ProductControlViewModel = viewModel()
) {
    // TODO (BACKEND): Reemplazar con estado del ViewModel:
    // val uiState  by viewModel.uiState.collectAsState()
    // val products = uiState.products
    var products by remember { mutableStateOf(sampleAdminProducts) }

    var searchQuery     by remember { mutableStateOf("") }
    var productToDelete by remember { mutableStateOf<AdminProduct?>(null) }
    var productToEdit   by remember { mutableStateOf<AdminProduct?>(null) }
    var showAddDialog   by remember { mutableStateOf(false) }

    // ── Filtro local ───────────────────────────────────────────────────────
    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    // ── Diálogo: Confirmar eliminación ─────────────────────────────────────
    if (productToDelete != null) {
        DeleteConfirmDialog(
            onConfirm = {
                // TODO (BACKEND): viewModel.deleteProduct(productToDelete!!.id)
                products = products.filter { it.id != productToDelete!!.id }
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }

    // ── Diálogo: Agregar / Editar producto ─────────────────────────────────
    if (showAddDialog || productToEdit != null) {
        ProductFormDialog(
            existingProduct = productToEdit,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { name, artist, category, price, stock ->
                if (productToEdit != null) {
                    // TODO (BACKEND): viewModel.updateProduct(...)
                    products = products.map { p ->
                        if (p.id == productToEdit!!.id)
                            p.copy(name = name, artist = artist, category = category, price = price, stock = stock)
                        else p
                    }
                } else {
                    // TODO (BACKEND): viewModel.createProduct(...)
                    val newId = (products.maxOfOrNull { it.id } ?: 0) + 1
                    products = products + AdminProduct(newId, name, artist, category, price, stock)
                }
                showAddDialog = false
                productToEdit = null
            }
        )
    }

    Scaffold(
        topBar         = { ProductControlTopBar() },
        containerColor = Color.Transparent
    ) { innerPadding ->

        // ── Fondo sunburst (igual que FavoritesScreen) ─────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawSunburstBackground() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // ── Fila: Búsqueda + Botón "+" ─────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    AdminSearchBar(
                        query         = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            // TODO (BACKEND): viewModel.searchProducts(it)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Botón "+" cuadrado rojo
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LpzRed)
                            .clickable { showAddDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = "Agregar producto",
                            tint               = Color.White,
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Lista de productos ─────────────────────────────────────
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = "Sin resultados para \"$searchQuery\"",
                            fontSize = 14.sp,
                            color    = LpzDark.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding      = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            AdminProductCard(
                                product  = product,
                                onEdit   = { productToEdit = product },
                                onDelete = { productToDelete = product }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TARJETA DE PRODUCTO (ADMIN)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AdminProductCard(
    product: AdminProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOutOfStock = product.stock == 0

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Portada cuadrada negra ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(product.coverColor)
            ) {
                /*
                 * TODO (BACKEND + COIL): Reemplazar este Box con:
                 *   AsyncImage(
                 *       model            = product.imageUrl,
                 *       contentDescription = product.name,
                 *       contentScale     = ContentScale.Crop,
                 *       modifier         = Modifier.fillMaxSize()
                 *   )
                 * Dependencia: implementation("io.coil-kt:coil-compose:2.6.0")
                 */
            }

            // ── Info del producto ──────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = product.name,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = product.artist,
                    fontSize = 12.sp,
                    color    = LpzDark.copy(alpha = 0.50f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text       = "$${"%.2f".format(product.price)}",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzRed
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Badge de stock ─────────────────────────────────────
                StockBadge(stock = product.stock, isOutOfStock = isOutOfStock)
            }

            // ── Acciones: editar / eliminar ────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono editar
                Icon(
                    imageVector        = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint               = LpzDark.copy(alpha = 0.55f),
                    modifier           = Modifier
                        .size(22.dp)
                        .clickable(onClick = onEdit)
                )
                // Ícono eliminar (papelera en rojo)
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint               = LpzRed,
                    modifier           = Modifier
                        .size(22.dp)
                        .clickable(onClick = onDelete)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  BADGE DE STOCK
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StockBadge(stock: Int, isOutOfStock: Boolean) {
    // Figma: verde cuando hay stock, rojo cuando no hay, amarillo cuando es bajo (≤3)
    val isLowStock = !isOutOfStock && stock <= 3
    val bgColor = when {
        isOutOfStock -> Color(0xFFFFCDD2)
        isLowStock   -> Color(0xFFFFF9C4)
        else         -> Color(0xFFC8E6C9)
    }
    val textColor = when {
        isOutOfStock -> Color(0xFFB71C1C)
        isLowStock   -> Color(0xFFF57F17)
        else         -> Color(0xFF2E7D32)
    }
    val label = "Stock $stock"

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text          = label,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = textColor,
            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            letterSpacing = 0.3.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  DIÁLOGO: CONFIRMAR ELIMINACIÓN
//  Diseño del Figma: fondo blanco, título centrado "¿Seguro que desea eliminar?",
//  botón NO (rojo) y SI (verde) lado a lado, sin ícono de cierre.
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // ── Pregunta centrada ──────────────────────────────────
                Text(
                    text       = "¿Seguro que\ndesea eliminar?",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark,
                    textAlign  = TextAlign.Center,
                    lineHeight = 30.sp
                )

                // ── Botones NO / SI ────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón NO — rojo
                    Button(
                        onClick  = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LpzRed)
                    ) {
                        Text(
                            text       = "NO",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }

                    // Botón SI — verde
                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(
                            text       = "SI",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  DIÁLOGO: FORMULARIO AGREGAR / EDITAR PRODUCTO
//  Diseño del Figma: hoja inferior sobre fondo desenfocado, título "Nuevo Producto"
//  con X de cierre, campos con borde redondeado blanco, dropdown de Categoría,
//  botón CONFIRMAR rojo full-width.
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    existingProduct: AdminProduct?,
    onDismiss: () -> Unit,
    onSave: (name: String, artist: String, category: String, price: Double, stock: Int) -> Unit
) {
    val isEditing = existingProduct != null

    var name           by remember { mutableStateOf(existingProduct?.name     ?: "") }
    var artist         by remember { mutableStateOf(existingProduct?.artist   ?: "") }
    var selectedCategory by remember { mutableStateOf(existingProduct?.category ?: availableCategories.first()) }
    var priceStr       by remember { mutableStateOf(if (existingProduct != null) "%.2f".format(existingProduct.price) else "0.00") }
    var stockStr       by remember { mutableStateOf(existingProduct?.stock?.toString() ?: "0") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && artist.isNotBlank() &&
            priceStr.toDoubleOrNull() != null &&
            stockStr.toIntOrNull() != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EA))
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Cabecera: título + X ───────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = if (isEditing) "Editar Producto" else "Nuevo Producto",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark
                    )
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint               = LpzDark.copy(alpha = 0.55f),
                        modifier           = Modifier
                            .size(22.dp)
                            .clickable(onClick = onDismiss)
                    )
                }

                // ── Campo: Nombre del producto ─────────────────────────
                FormFieldWhite(
                    label       = "Nombre del producto",
                    value       = name,
                    placeholder = "Ej. La playa album",
                    onValueChange = { name = it }
                )

                // ── Campo: Tipo de producto ────────────────────────────
                FormFieldWhite(
                    label       = "Tipo de producto",
                    value       = artist,
                    placeholder = "Album",
                    onValueChange = { artist = it }
                )

                // ── Fila: Precio + Stock ───────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormFieldWhite(
                        label         = "Precio (\$)",
                        value         = priceStr,
                        placeholder   = "0.00",
                        onValueChange = { priceStr = it },
                        modifier      = Modifier.weight(1f)
                    )
                    FormFieldWhite(
                        label         = "Stock",
                        value         = stockStr,
                        placeholder   = "0",
                        onValueChange = { stockStr = it },
                        modifier      = Modifier.weight(1f)
                    )
                }

                // ── Dropdown: Categoría ────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text       = "Categoría",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color      = LpzDark.copy(alpha = 0.70f)
                    )
                    ExposedDropdownMenuBox(
                        expanded        = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value         = selectedCategory,
                            onValueChange = {},
                            readOnly      = true,
                            trailingIcon  = {
                                Icon(
                                    imageVector        = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expandir",
                                    tint               = LpzDark
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape  = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor      = LpzDark.copy(alpha = 0.25f),
                                unfocusedBorderColor    = LpzDark.copy(alpha = 0.15f)
                            ),
                            textStyle = TextStyle(
                                fontSize   = 15.sp,
                                color      = LpzDark,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        ExposedDropdownMenu(
                            expanded        = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier        = Modifier.background(Color.White)
                        ) {
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text  = cat,
                                            fontSize = 14.sp,
                                            color = LpzDark
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        dropdownExpanded = false
                                        // TODO (BACKEND): viewModel.onCategorySelected(cat)
                                    }
                                )
                            }
                        }
                    }
                }

                // TODO (BACKEND + COIL): Agregar picker de imagen:
                // ImagePickerField(onImageSelected = { uri -> viewModel.uploadImage(uri) })

                Spacer(modifier = Modifier.height(4.dp))

                // ── Botón CONFIRMAR full-width ─────────────────────────
                Button(
                    onClick = {
                        // TODO (BACKEND): Delegar la lógica al ViewModel antes de cerrar
                        if (isValid) {
                            onSave(
                                name.trim(),
                                artist.trim(),
                                selectedCategory,
                                priceStr.toDouble(),
                                stockStr.toInt()
                            )
                        }
                    },
                    enabled  = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = LpzRed,
                        disabledContainerColor = LpzRed.copy(alpha = 0.45f)
                    )
                ) {
                    Text(
                        text          = "CONFIRMAR",
                        fontSize      = 16.sp,
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
//  CAMPO DE FORMULARIO — fondo blanco, borde sutil (estilo Figma)
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormFieldWhite(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = label,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = LpzDark.copy(alpha = 0.70f)
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            singleLine    = true,
            placeholder   = {
                Text(
                    text     = placeholder,
                    fontSize = 14.sp,
                    color    = LpzDark.copy(alpha = 0.35f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor      = LpzDark.copy(alpha = 0.25f),
                unfocusedBorderColor    = LpzDark.copy(alpha = 0.15f),
                focusedTextColor        = LpzDark,
                unfocusedTextColor      = LpzDark
            ),
            textStyle = TextStyle(
                fontSize   = 15.sp,
                fontWeight = FontWeight.Normal,
                color      = LpzDark
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  BARRA DE BÚSQUEDA
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.height(52.dp),
        placeholder   = { Text("Buscar", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon   = {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = "Buscar",
                tint               = Color.Gray,
                modifier           = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint               = Color.Gray,
                        modifier           = Modifier.size(17.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape      = RoundedCornerShape(14.dp),
        colors     = TextFieldDefaults.colors(
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
//  Diseño Figma: ☰ a la izquierda, "LPZ RECORDS / ADMIN" centrado,
//  avatar circular "AD" a la derecha.
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductControlTopBar() {
    Column {
        TopAppBar(
            navigationIcon = {
                // Ícono de menú hamburguesa (≡)
                IconButton(onClick = { /* TODO: abrir drawer de admin si lo implementas */ }) {
                    Icon(
                        imageVector        = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint               = LpzDark
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "LPZ RECORDS",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark
                    )
                    Text(
                        text       = "ADMIN",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzRed,
                        letterSpacing = 1.sp
                    )
                }
            },
            actions = {

                Box(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LpzDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "AD",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
        )
        HorizontalDivider(color = LpzDark.copy(alpha = 0.15f), thickness = 1.dp)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  FONDO: Patrón de rayos retro (sunburst) — igual que FavoritesScreen
// ═══════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSunburstBackground() {
    drawRect(color = Color(0xFFF4F1E6))

    val centerX   = size.width / 2f
    val centerY   = size.height / 2f
    val radius    = size.width.coerceAtLeast(size.height) * 1.4f
    val rayCount  = 24
    val angleStep = (2 * Math.PI / rayCount).toFloat()

    val rayColorA = Color(0xFFEDE8D5)
    val rayColorB = Color(0xFFF4F1E6)

    for (i in 0 until rayCount) {
        val startAngle = i * angleStep - angleStep / 2f
        val endAngle   = startAngle + angleStep

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(centerX + radius * cos(startAngle), centerY + radius * sin(startAngle))
            lineTo(centerX + radius * cos(endAngle),   centerY + radius * sin(endAngle))
            close()
        }

        drawPath(path = path, color = if (i % 2 == 0) rayColorA else rayColorB)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductControlScreenPreview() {
    ProductControlScreen()
}