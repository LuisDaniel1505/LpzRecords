package com.ldaniel1505.lpzrecords.viewmodel.catalogAdmin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val PRODUCT_COLUMNS = Columns.raw(
    """
    id,
    title,
    description,
    price,
    stock,
    img_url,
    release_date,
    active,
    created_at,
    fk_id_artist,
    fk_id_category,
    fk_id_supplier,
    artist:fk_id_artist (
        id, name, biography, musical_genre
    ),
    category:fk_id_category (
        id, name
    ),
    supplier:fk_id_supplier (
        id, name, telephone, email
    )
    """.trimIndent()
)

class ProductControlViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun fetchProducts() {
        isLoading = true
        errorMessage = null
        successMessage = null
        viewModelScope.launch {
            try {
                _products.value = loadProducts()
            } catch (e: Exception) {
                errorMessage = friendlyError("Error al cargar productos", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun insertProduct(product: Product) {
        errorMessage = null
        successMessage = null
        validateProduct(product)?.let { message ->
            errorMessage = message
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("products").insert(product.toInsertPayload())
                }
                _products.value = loadProducts()
                successMessage = "Producto creado correctamente."
            } catch (e: Exception) {
                errorMessage = friendlyError("Error al crear producto", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProduct(product: Product) {
        val id = product.id
        errorMessage = null
        successMessage = null
        validateProduct(product)?.let { message ->
            errorMessage = message
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .update(product.toUpdatePayload()) {
                            filter { eq("id", id) }
                        }
                }
                _products.value = loadProducts()
                successMessage = "Producto actualizado correctamente."
            } catch (e: Exception) {
                errorMessage = friendlyError("Error al actualizar producto", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun deactivateProduct(product: Product) {
        isLoading = true
        errorMessage = null
        successMessage = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("products")
                        .update(mapOf("active" to false)) {
                            filter {
                                eq("id", product.id)
                            }
                        }
                }
                _products.value = loadProducts()
                successMessage = "Producto ocultado del catalogo."

            } catch (e: Exception) {
                errorMessage = friendlyError("Error al ocultar producto", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    private suspend fun loadProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            SupabaseClient.client
                .from("products")
                .select(columns = PRODUCT_COLUMNS)
                .decodeList<Product>()
        }.sortedWith(
            compareByDescending<Product> { it.active }
                .thenBy { it.title.lowercase() }
        )
    }

    private fun validateProduct(product: Product): String? {
        return when {
            product.title.isBlank() -> "Ingresa el nombre del producto."
            product.price <= 0.0 -> "El precio debe ser mayor a 0."
            product.stock < 0 -> "El stock no puede ser negativo."
            product.fkCategory == null || product.fkCategory <= 0 -> "Selecciona una categoria."
            product.fkArtist == null || product.fkArtist <= 0 -> "Selecciona un artista."
            else -> null
        }
    }

    private fun friendlyError(prefix: String, error: Exception): String {
        val rawMessage = error.localizedMessage.orEmpty()
        val detail = when {
            rawMessage.contains("row-level security", ignoreCase = true) ->
                "El usuario actual no tiene permisos para modificar productos."
            rawMessage.contains("duplicate key", ignoreCase = true) ->
                "Ya existe un producto con ese identificador."
            rawMessage.contains("violates foreign key", ignoreCase = true) ->
                "La categoria, artista o proveedor seleccionado no existe en Supabase."
            rawMessage.contains("Could not find", ignoreCase = true) ->
                "Revisa que la tabla products y sus columnas existan con los nombres esperados."
            else -> "Revisa la conexion y los permisos de Supabase."
        }
        return "$prefix. $detail"
    }

    private fun Product.toInsertPayload(): ProductInsertPayload {
        return ProductInsertPayload(
            id = id,
            fkCategory = fkCategory,
            fkArtist = fkArtist,
            fkSupplier = fkSupplier,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            price = price,
            stock = stock,
            imageUrl = img_url?.trim()?.ifBlank { null },
            releaseDate = release_date.trim().ifBlank { null },
            active = active,
            createdAt = created_at.trim()
        )
    }

    private fun Product.toUpdatePayload(): ProductUpdatePayload {
        return ProductUpdatePayload(
            fkCategory = fkCategory,
            fkArtist = fkArtist,
            fkSupplier = fkSupplier,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            price = price,
            stock = stock,
            imageUrl = img_url?.trim()?.ifBlank { null },
            releaseDate = release_date.trim().ifBlank { null },
            active = active
        )
    }
}

@Serializable
private data class ProductInsertPayload(
    val id: String,
    @SerialName("fk_id_category")
    val fkCategory: Int?,
    @SerialName("fk_id_artist")
    val fkArtist: Int?,
    @SerialName("fk_id_supplier")
    val fkSupplier: Int?,
    val title: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    @SerialName("img_url")
    val imageUrl: String?,
    @SerialName("release_date")
    val releaseDate: String?,
    val active: Boolean,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
private data class ProductUpdatePayload(
    @SerialName("fk_id_category")
    val fkCategory: Int?,
    @SerialName("fk_id_artist")
    val fkArtist: Int?,
    @SerialName("fk_id_supplier")
    val fkSupplier: Int?,
    val title: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    @SerialName("img_url")
    val imageUrl: String?,
    @SerialName("release_date")
    val releaseDate: String?,
    val active: Boolean
)
