package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEWS AISLADAS — ProductControlScreen
//  Archivo temporal para desarrollo. No afecta producción.
//  Ruta sugerida: .../ui/screens/admin/ProductControlPreviews.kt
// ═══════════════════════════════════════════════════════════════════════════

// ── 1. Pantalla principal con lista de productos ───────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "① Lista de productos")
@Composable
fun PreviewProductList() {
    ProductControlScreen()
}

// ── 2. Pantalla principal con búsqueda activa (resultados filtrados) ────────
@Preview(showBackground = true, showSystemUi = true, name = "② Búsqueda activa")
@Composable
fun PreviewProductListSearch() {
    // Reutiliza la pantalla entera; en el preview escribe en el campo para ver
    // el filtro. Esta preview muestra el estado inicial vacío del search.
    ProductControlScreen()
}

// ── 3. Solo el diálogo de confirmación de eliminación ──────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "③ Diálogo — Confirmar eliminación")
@Composable
fun PreviewDeleteConfirmDialog() {
    // Forzamos que el diálogo esté visible desde el arranque
    var visible by remember { mutableStateOf(true) }
    if (visible) {
        DeleteConfirmDialog(
            onConfirm = { visible = false },
            onDismiss = { visible = false }
        )
    }
}

// ── 4. Formulario vacío — Nuevo producto ───────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "④ Formulario — Nuevo producto")
@Composable
fun PreviewAddProductDialog() {
    var visible by remember { mutableStateOf(true) }
    if (visible) {
        ProductFormDialog(
            existingProduct = null,
            onDismiss       = { visible = false },
            onSave          = { _, _, _, _, _ -> visible = false }
        )
    }
}

// ── 5. Formulario precargado — Editar producto ─────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "⑤ Formulario — Editar producto")
@Composable
fun PreviewEditProductDialog() {
    var visible by remember { mutableStateOf(true) }
    if (visible) {
        ProductFormDialog(
            existingProduct = AdminProduct(
                id       = 1,
                name     = "Abbey Road",
                artist   = "The Beatles",
                category = "Rock",
                price    = 34.99,
                stock    = 12
            ),
            onDismiss = { visible = false },
            onSave    = { _, _, _, _, _ -> visible = false }
        )
    }
}

// ── 6. Card individual — con stock normal ──────────────────────────────────
@Preview(showBackground = true, name = "⑥ Card — Stock normal")
@Composable
fun PreviewProductCardNormal() {
    AdminProductCard(
        product  = AdminProduct(1, "Abbey Road", "The Beatles", "Rock", 34.99, 12),
        onEdit   = {},
        onDelete = {}
    )
}

// ── 7. Card individual — sin stock ────────────────────────────────────────
@Preview(showBackground = true, name = "⑦ Card — Sin stock")
@Composable
fun PreviewProductCardNoStock() {
    AdminProductCard(
        product  = AdminProduct(2, "Thriller", "Michael Jackson", "Pop", 39.99, 0),
        onEdit   = {},
        onDelete = {}
    )
}

// ── 8. Card individual — stock bajo ───────────────────────────────────────
@Preview(showBackground = true, name = "⑧ Card — Stock bajo")
@Composable
fun PreviewProductCardLowStock() {
    AdminProductCard(
        product  = AdminProduct(3, "Kind of Blue", "Miles Davis", "Jazz", 29.99, 2),
        onEdit   = {},
        onDelete = {}
    )
}
