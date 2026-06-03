package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.data.model.Artist
import com.ldaniel1505.lpzrecords.data.model.Category
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.data.model.Supplier
import com.ldaniel1505.lpzrecords.ui.theme.*
import com.ldaniel1505.lpzrecords.viewmodel.catalog.ArtistViewModel
import com.ldaniel1505.lpzrecords.viewmodel.catalog.CategoryViewModel
import com.ldaniel1505.lpzrecords.viewmodel.catalog.SupplierViewModel
import com.ldaniel1505.lpzrecords.viewmodel.catalogAdmin.ProductControlViewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

@Composable
fun ProductControlScreen(
    onNavigateBack: () -> Unit = {},
    onNavigatetoAddProduct: (() -> Unit)? = null,
    viewModel: ProductControlViewModel = viewModel(),

    ) {
    val products by viewModel.products.collectAsState(emptyList())
    val isLoading = viewModel.isLoading



    var searchQuery     by remember { mutableStateOf("") }
    var productToDesactivate by remember { mutableStateOf<Product?>(null) }
    var productToEdit   by remember { mutableStateOf<Product?>(null) }
    var showAddDialog   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }


    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    it.category?.name?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    // Pregunta si desea eliminar (Desactivar)
    if (productToDesactivate != null) {
        productToDesactivate?.let { product ->
            DeleteConfirmDialog(
                onConfirm = {
                    viewModel.deactivateProduct(product)
                    productToDesactivate = null
                },
                onDismiss = {
                    productToDesactivate = null
                }
            )
        }
    }
    if (showAddDialog || productToEdit != null) {
        ProductFormDialog(
            existingProduct = productToEdit,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },

            onSave = { id, category, artist, supplier, title, description,
                       price, stock, img_url, release_date, active, fechaSegura->

                val productData = Product(
                    id = id.ifBlank { productToEdit?.id ?: java.util.UUID.randomUUID().toString() },
                    fkCategory = category.id,
                    fkArtist = artist.id,
                    fkSupplier = supplier.id,
                    title = title,
                    description = description,
                    price = price,
                    stock = stock,
                    img_url = img_url,
                    release_date = release_date,
                    active = active,
                    created_at = fechaSegura
                )

                if (productToEdit != null) {
                    viewModel.updateProduct(productData)
                } else {
                    viewModel.insertProduct(productData)
                }

                showAddDialog = false
                productToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            AdminSectionTopBar(
                title        = "PRODUCTOS",
                onOpenDrawer = onNavigateBack
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->

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

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    AdminSearchBar(
                        query         = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        modifier = Modifier.size(52.dp),
                        onClick = {
                            if (onNavigatetoAddProduct != null) {
                                onNavigatetoAddProduct()
                            } else {
                                showAddDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LpzRed,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = "Agregar producto",
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (viewModel.errorMessage != null) {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }

                //Muestra estado de stock por color
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LpzRed)
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text      = "Sin resultados para \"$searchQuery\"",
                            fontSize  = 14.sp,
                            color     = LpzDark.copy(alpha = 0.45f),
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
                                onDelete = { productToDesactivate = product }
                            )
                        }
                    }
                }
            }
        }
    }
}

//  Tarjeta de producto, Badge Y Diálogo de eliminación
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AdminProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isOutOfStock = product.stock == 0
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = product.img_url,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(100.dp)
                    .width(100.dp)
            )
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = product.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LpzDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = product.artist?.name?:"", fontSize = 12.sp, color = LpzDark.copy(alpha = 0.50f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "$${String.format(Locale.US, "%.2f", product.price)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LpzRed)
                Spacer(modifier = Modifier.height(4.dp))
                StockBadge(stock = product.stock, isOutOfStock = isOutOfStock)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = LpzDark.copy(alpha = 0.55f),
                    modifier = Modifier.size(22.dp).clickable(onClick = onEdit)
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = LpzRed,
                    modifier = Modifier.size(22.dp).clickable(onClick = onDelete)
                )
            }
        }
    }
}

@Composable
private fun StockBadge(stock: Int, isOutOfStock: Boolean) {
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

    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Text(
            text          = "Stock $stock",
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = textColor,
            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Text(text = "¿Seguro que\ndesea eliminar?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LpzDark, textAlign = TextAlign.Center, lineHeight = 30.sp)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = LpzRed)) {
                        Text(text = "NO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                        Text(text = "SI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

//Formato de formulario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    existingProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (
        id: String, category: Category, artist: Artist, supplier: Supplier,
        title: String, description: String, price: Double, stock: Int,
        img_url: String, release_date: String, active: Boolean, created_at:String
    ) -> Unit,
) {

    val viewModelCategory: CategoryViewModel = viewModel()
    val viewModelArtist: ArtistViewModel = viewModel()
    val viewModelSupplier: SupplierViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModelSupplier.loadSupplier()
        viewModelCategory.loadCategories()
        viewModelArtist.loadArtist()
    }

    val isEditing = existingProduct != null
    val availableCategories = viewModelCategory.categories
    val availableArtist = viewModelArtist.artists
    val availableSupplier = viewModelSupplier.suppliers

    var selectedCategory by remember(existingProduct) {
        mutableStateOf(existingProduct?.category ?: Category(id = 0, name = "Seleccione una categoría"))
    }
    var selectedArtist by remember(existingProduct) {
        mutableStateOf(existingProduct?.artist ?: Artist(id = 0, name = "", biography = "", musical_genre = ""))
    }
    var selectedSupplier by remember(existingProduct) {
        mutableStateOf(existingProduct?.supplier ?: Supplier(id = 0, name = "",  telephone = "", email = ""))
    }
    var title            by remember(existingProduct) { mutableStateOf(existingProduct?.title ?: "") }
    var description      by remember(existingProduct) { mutableStateOf(existingProduct?.description ?: "") }
    var priceStr         by remember(existingProduct) { mutableStateOf(if (isEditing) String.format(Locale.US, "%.2f", existingProduct!!.price) else "") }
    var stockStr         by remember(existingProduct) { mutableStateOf(existingProduct?.stock?.toString() ?: "") }
    var img_url          by remember(existingProduct) { mutableStateOf(existingProduct?.img_url ?: "") }
    var release_date    by remember(existingProduct) { mutableStateOf(existingProduct?.release_date ?: "") }
    var active           by remember(existingProduct) { mutableStateOf(existingProduct?.active ?: true) }
    var created_at       by remember(existingProduct) { mutableStateOf(existingProduct?.created_at ?: "")}

    var dropdownExpandedSupplier by remember { mutableStateOf(false) }
    var dropdownExpandedArtist by remember { mutableStateOf(false) }
    var dropdownExpandedCategory by remember { mutableStateOf(false) }



    // Validación del formulario mejorada
    val isValid = title.isNotBlank() &&
            priceStr.toDoubleOrNull() != null &&
            stockStr.toIntOrNull() != null &&
            release_date.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera del Diálogo
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(text = if (isEditing) "Editar Producto" else "Nuevo Producto", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LpzDark)
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint               = LpzDark.copy(alpha = 0.55f),
                        modifier           = Modifier.size(22.dp).clickable(onClick = onDismiss)
                    )
                }

                // Campos de Texto Principales
                FormFieldWhite(label = "Nombre del producto", value = title, placeholder = "Ej. Abbey Road", onValueChange = { title = it })
                FormFieldWhite(label = "Descripción", value = description, placeholder = "Ej. Edición especial de aniversario", onValueChange = { description = it })
                FormFieldWhite(label = "URL de la Imagen", value = img_url, placeholder = "Ej. https://link-de-imagen.com/foto.jpg", onValueChange = { img_url = it })

                // Selector de Fecha de Lanzamiento
                val context = LocalContext.current

                // Función interna que despliega el calendario nativo sin romperse
                fun mostrarCalendarioNativo() {
                    val calendar = java.util.Calendar.getInstance()

                    // Si el producto ya tiene fecha, hacemos que el calendario se abra en ese día
                    if (release_date.isNotBlank()) {
                        try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            sdf.parse(release_date)?.let { calendar.time = it }
                        } catch (e: Exception) {
                            // Si falla el parseo, usa la fecha de hoy por defecto
                        }
                    }

                    val anio = calendar.get(java.util.Calendar.YEAR)
                    val mes = calendar.get(java.util.Calendar.MONTH)
                    val dia = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                    // Crear y mostrar el diálogo clásico de Android
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            // Al seleccionar la fecha, la formateamos directamente a YYYY-MM-DD
                            val nuevaFecha = java.util.Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            release_date = formatter.format(nuevaFecha.time)
                        },
                        anio,
                        mes,
                        dia
                    ).show()

                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Fecha de lanzamiento",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = LpzDark.copy(alpha = 0.70f)
                    )

                    // Aquí abre el Box PADRE
                    Box(modifier = Modifier.fillMaxWidth()) {

                        OutlinedTextField(
                            value = release_date,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Seleccione la fecha",
                                    color = LpzDark.copy(alpha = 0.4f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha",
                                    tint = LpzDark.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = LpzDark.copy(alpha = 0.25f),
                                unfocusedBorderColor = LpzDark.copy(alpha = 0.15f)
                            ),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = LpzDark,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        // El Box HIJO (capa invisible) DEBE estar aquí adentro para que 'matchParentSize' funcione
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { mostrarCalendarioNativo() }
                        )
                    }
                }

                // Fila de Precio y Stock
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormFieldWhite(label = "Precio (\$)", value = priceStr, placeholder = "0.00", onValueChange = { priceStr = it }, modifier = Modifier.weight(1f))
                    FormFieldWhite(label = "Stock", value = stockStr, placeholder = "0", onValueChange = { stockStr = it }, modifier = Modifier.weight(1f))
                }

                //Para seleccionar los Suppliers registrados
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Proveedores", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LpzDark.copy(alpha = 0.70f))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = selectedSupplier.name,
                            onValueChange = {},
                            readOnly      = true,
                            trailingIcon  = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar Proveedores",
                                    tint = LpzDark.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor      = LpzDark.copy(alpha = 0.25f),
                                unfocusedBorderColor    = LpzDark.copy(alpha = 0.15f)
                            ),
                            textStyle = TextStyle(fontSize = 15.sp, color = LpzDark, fontWeight = FontWeight.Medium)
                        )

                        // Capa invisible para capturar el click de apertura de manera limpia
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { dropdownExpandedSupplier = !dropdownExpandedSupplier }
                        )

                        DropdownMenu(
                            expanded         = dropdownExpandedSupplier,
                            onDismissRequest = { dropdownExpandedSupplier = false },
                            modifier         = Modifier.background(Color.White)
                        ) {
                            availableSupplier.forEach { supObj ->
                                DropdownMenuItem(
                                    text = { Text(text = supObj.name, fontSize = 14.sp, color = LpzDark) },
                                    onClick = {
                                        selectedSupplier = supObj
                                        dropdownExpandedSupplier = false
                                    }
                                )
                            }
                        }
                    }
                }
                //Para seleccionar los Artistas registrados
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Artistas", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LpzDark.copy(alpha = 0.70f))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = selectedArtist?.name ?: "",
                            onValueChange = {},
                            readOnly      = true,
                            trailingIcon  = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar Artistas",
                                    tint = LpzDark.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor      = LpzDark.copy(alpha = 0.25f),
                                unfocusedBorderColor    = LpzDark.copy(alpha = 0.15f)
                            ),
                            textStyle = TextStyle(fontSize = 15.sp, color = LpzDark, fontWeight = FontWeight.Medium)
                        )

                        // Capa invisible para capturar el click de apertura de manera limpia
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { dropdownExpandedArtist = !dropdownExpandedArtist }
                        )

                        DropdownMenu(
                            expanded         = dropdownExpandedArtist,
                            onDismissRequest = { dropdownExpandedArtist = false },
                            modifier         = Modifier.background(Color.White)
                        ) {
                            availableArtist.forEach { artObj ->
                                DropdownMenuItem(
                                    text = { Text(text = artObj.name, fontSize = 14.sp, color = LpzDark) },
                                    onClick = {
                                        selectedArtist = artObj
                                        dropdownExpandedArtist = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de Categoría (Menú Desplegable - Corregido sin duplicados)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Categoría", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LpzDark.copy(alpha = 0.70f))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = selectedCategory.name,
                            onValueChange = {},
                            readOnly      = true,
                            trailingIcon  = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar categorías",
                                    tint = LpzDark.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor      = LpzDark.copy(alpha = 0.25f),
                                unfocusedBorderColor    = LpzDark.copy(alpha = 0.15f)
                            ),
                            textStyle = TextStyle(fontSize = 15.sp, color = LpzDark, fontWeight = FontWeight.Medium)
                        )

                        // Capa invisible para capturar el click de apertura de manera limpia
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { dropdownExpandedCategory = !dropdownExpandedCategory }
                        )

                        DropdownMenu(
                            expanded         = dropdownExpandedCategory,
                            onDismissRequest = { dropdownExpandedCategory = false },
                            modifier         = Modifier.background(Color.White)
                        ) {
                            availableCategories.forEach { catObj ->
                                DropdownMenuItem(
                                    text = { Text(text = catObj.name, fontSize = 14.sp, color = LpzDark) },
                                    onClick = {
                                        selectedCategory = catObj
                                        dropdownExpandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Switch de Disponibilidad (Corregido para manejar Boolean de forma nativa)
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Disponibilidad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LpzDark)
                        Text(text = if (active) "Producto activo en catálogo" else "Producto ocultado", fontSize = 12.sp, color = LpzDark.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = LpzRed,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botón de Confirmación
                Button(
                    onClick = {
                        if (isValid) {
                            //  Usamos el estado real que cambia con los Dropdowns
                            val artistObj   = selectedArtist ?: Artist(id = 0, name = "No tiene nombre", biography = "", musical_genre = "")
                            val supplierObj = selectedSupplier

                           if(img_url.isBlank()){
                                img_url = "https://static.vecteezy.com/system/resources/thumbnails/009/314/864/small/vinyl-record-vector-illustration-isolated-on-white-background-free-png.png"
                            } else {
                                img_url.trim()
                            }

                            val timestampSeguro = if (created_at.isBlank() || created_at == "fechaSegura") {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                                }
                                sdf.format(java.util.Date())
                            } else {
                                created_at.trim()
                            }

                            onSave(
                                existingProduct?.id ?: "",
                                selectedCategory,
                                artistObj,
                                supplierObj,
                                title.trim(),
                                description.trim(),
                                priceStr.toDoubleOrNull() ?: 0.0,
                                stockStr.toIntOrNull() ?: 0,
                                img_url,
                                release_date.trim(), // Ya garantizamos con isValid que no irá vacío
                                active,
                                timestampSeguro // <--- Enviamos el timestamp limpio a Supabase
                            )
                        }
                    },
                    enabled  = isValid,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = LpzRed,
                        disabledContainerColor = LpzRed.copy(alpha = 0.45f)
                    )
                ) {
                    Text(text = "CONFIRMAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                }
            }
        }
    }
}

//Componentes adicionales

@Composable
private fun FormFieldWhite(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LpzDark.copy(alpha = 0.70f))
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            singleLine    = true,
            placeholder   = { Text(text = placeholder, fontSize = 14.sp, color = LpzDark.copy(alpha = 0.35f)) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = LpzDark.copy(alpha = 0.25f),
                unfocusedBorderColor = LpzDark.copy(alpha = 0.15f),
                focusedTextColor = LpzDark,
                unfocusedTextColor = LpzDark
            ),
            textStyle = TextStyle(fontSize = 15.sp, color = LpzDark)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value         = query,
        onValueChange = onQueryChange, // <--- Aquí estaba el error
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
        trailingIcon  = {
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

private fun DrawScope.drawSunburstBackground() {
    drawRect(color = Color(0xFFF4F1E6))

    val centerX   = size.width / 2f
    val centerY   = size.height / 2f
    val radius    = size.width.coerceAtLeast(size.height) * 1.4f
    val rayCount  = 24
    val angleStep = (2 * Math.PI / rayCount).toFloat()
    val rayColorA = Color(0xFFEDE8D5)
    val rayColorB = Color(0xFFF4F1E6)

    // Reutilización de un único Path estructural para evitar instanciación masiva en el hilo de renderizado
    val path = Path()

    for (i in 0 until rayCount) {
        val startAngle = i * angleStep - angleStep / 2f
        val endAngle   = startAngle + angleStep

        path.reset()
        path.moveTo(centerX, centerY)
        path.lineTo(centerX + radius * cos(startAngle), centerY + radius * sin(startAngle))
        path.lineTo(centerX + radius * cos(endAngle),   centerY + radius * sin(endAngle))
        path.close()

        drawPath(path = path, color = if (i % 2 == 0) rayColorA else rayColorB)
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductControlScreenPreview() {
    ProductControlScreen()
}