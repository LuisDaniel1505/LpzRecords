package com.ldaniel1505.lpzrecords.ui.screens.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.catalog.ProductViewModel
import java.util.Locale

@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onBuyNow: (Product) -> Unit = {}
) {
    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }

    val product = viewModel.selectProduct
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LpzBeige)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LpzRed)
                }
            }

            errorMessage != null -> {
                DetailErrorState(
                    message = errorMessage,
                    onNavigateBack = onNavigateBack
                )
            }

            product != null -> {
                ProductDetailContent(
                    product = product,
                    onNavigateBack = onNavigateBack,
                    onAddToCart = {
                        onAddToCart(product)
                        onNavigateToCart()
                    },
                    onBuyNow = { onBuyNow(product) }
                )
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: Product,
    onNavigateBack: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LpzDark)
                .padding(top = 44.dp, bottom = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.White
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2A2A2A),
                shadowElevation = 10.dp
            ) {
                AsyncImage(
                    model = product.img_url,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = product.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark,
                lineHeight = 30.sp
            )

            Text(
                text = product.artist?.name ?: "Artista desconocido",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )

            Text(
                text = money(product.price),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed
            )

            ProductMetaRow(
                category = product.category?.name ?: "Sin categoria",
                stock = product.stock
            )

            Text(
                text = product.description.ifBlank { "Sin descripcion disponible." },
                fontSize = 14.sp,
                color = LpzDark.copy(alpha = 0.68f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LpzDark),
                    border = BorderStroke(1.5.dp, LpzDark)
                ) {
                    Text(
                        text = "Agregar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onBuyNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LpzRed)
                ) {
                    Text(
                        text = "Comprar ahora",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductMetaRow(
    category: String,
    stock: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DetailChip(
            text = category,
            modifier = Modifier.weight(1f)
        )
        DetailChip(
            text = if (stock > 0) "Stock: $stock" else "Sin stock",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = LpzDark.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(start = 8.dp, top = 44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = LpzDark
            )
        }

        Text(
            text = message,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            color = LpzRed,
            textAlign = TextAlign.Center
        )
    }
}

private fun money(value: Double): String {
    return "$${String.format(Locale.US, "%.2f", value)}"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductDetailScreenPreview() {
    ProductDetailScreen(productId = "preview")
}
