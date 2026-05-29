package com.ldaniel1505.lpzrecords.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.ui.theme.*
import com.ldaniel1505.lpzrecords.viewmodel.catalog.ProductViewModel

// ═══════════════════════════════════════════════════════════════════════════
//  MODELO DE DATOS
// ═══════════════════════════════════════════════════════════════════════════

data class Review(
    val id: Int,
    val author: String,
    val comment: String,
    val rating: Int
)

private val sampleReviews: List<Review> = emptyList()

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Disparar la carga del producto al entrar usando el ID de la ruta
    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }

    // Obtenemos los estados que ya maneja tu ProductViewModel
    val producto = viewModel.selectProduct
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val reviews = sampleReviews

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fondo: mitad oscura + mitad beige ─────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.42f)
                    .background(Color(0xFF1A1A1A))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(LpzBeige)
            )
        }

        // ── Contenido principal con scroll ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Sección superior oscura (imagen del disco) ─────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(top = 48.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // ── Botón atrás ────────────────────────────────────────
                IconButton(
                    onClick  = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint               = Color.White
                    )
                }

                VinylPlaceholder()
            }

            // ── CONTROL DE ESTADOS DE CARGA / ERROR ─────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LpzRed)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage, color = LpzRed, textAlign = TextAlign.Center)
                }
            } else if (producto != null) {
                // Si ya cargó y el objeto no es nulo, pintamos la info real de Supabase
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LpzBeige)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ── Título del producto ────────────────────────────────
                    Text(
                        text       = producto.name,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark,
                        lineHeight = 30.sp
                    )

                    // ── Nombre del artista (Aprovechando el JOIN de tu relación fk_artist) ──────
                    Text(
                        text       = producto.artist?.name ?: "Artista Desconocido",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzRed,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Precio ─────────────────────────────────────────────
                    Text(
                        text       = "$${String.format("%.2f", producto.price)}",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Descripción ────────────────────────────────────────
                    Text(
                        text       = producto.description ?: "Sin descripción disponible.",
                        fontSize   = 14.sp,
                        color      = LpzDark.copy(alpha = 0.65f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Sección Opiniones ──────────────────────────────────
                    Text(
                        text          = "OPINIONES",
                        fontSize      = 13.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = LpzDark.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (reviews.isEmpty()) {
                        Card(
                            shape     = RoundedCornerShape(10.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier  = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text      = "No hay opiniones disponibles",
                                fontSize  = 13.sp,
                                color     = LpzDark.copy(alpha = 0.40f),
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        reviews.forEach { review ->
                            ReviewCard(review = review)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Botones de acción ──────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón: Al Carrito
                        OutlinedButton(
                            onClick = { onNavigateToCart() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LpzDark),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, LpzDark)
                        ) {
                            Text(
                                text       = "Al Carrito",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Botón: Comprar ahora
                        Button(
                            onClick = { /* TODO: ir directo a checkout */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LpzRed)
                        ) {
                            Text(
                                text       = "Comprar ahora",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun VinylPlaceholder() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .shadow(elevation = 24.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color(0xFF1C1008)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A1F10)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LpzRed),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = review.author,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = LpzDark
            )
            Text(
                text     = review.comment,
                fontSize = 13.sp,
                color    = LpzDark.copy(alpha = 0.65f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductDetailScreenPreview() {
    // Le pasamos un ID de prueba cualquiera para el Preview
    ProductDetailScreen(productId = 1)
}