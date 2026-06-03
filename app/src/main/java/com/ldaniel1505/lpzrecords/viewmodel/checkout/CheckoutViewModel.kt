package com.ldaniel1505.lpzrecords.viewmodel.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Address
import com.ldaniel1505.lpzrecords.data.model.CartItem
import com.ldaniel1505.lpzrecords.data.model.CreateSale
import com.ldaniel1505.lpzrecords.data.model.CreateSaleDetail
import com.ldaniel1505.lpzrecords.data.model.Order
import com.ldaniel1505.lpzrecords.data.model.OrderItem
import com.ldaniel1505.lpzrecords.data.model.PaymentMethod
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val selectedAddress: Address = defaultAddress,
    val selectedPaymentMethod: PaymentMethod = defaultPaymentMethod,
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val total: Double = 0.0,
    val isSubmitting: Boolean = false,
    val orderSuccess: Boolean = false,
    val createdOrder: Order? = null,
    val errorMessage: String? = null
)

class CheckoutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var cartSyncJob: Job? = null
    private var customerDataJob: Job? = null

    fun loadFromCart(cartViewModel: CartViewModel) {
        if (cartSyncJob != null) return

        cartSyncJob = viewModelScope.launch {
            combine(cartViewModel.cartItems, cartViewModel.totalPrice) { items, subtotal ->
                items to subtotal
            }.collect { (items, subtotal) ->
                val shipping = if (items.isEmpty()) 0.0 else SHIPPING_COST
                _uiState.update {
                    it.copy(
                        items = items,
                        subtotal = subtotal,
                        shippingCost = shipping,
                        total = subtotal + shipping,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun loadCustomerData(
        addressViewModel: AddressViewModel,
        paymentMethodsViewModel: PaymentMethodsViewModel
    ) {
        if (customerDataJob != null) return

        customerDataJob = viewModelScope.launch {
            combine(addressViewModel.uiState, paymentMethodsViewModel.uiState) { addressState, paymentState ->
                addressState to paymentState
            }.collect { (addressState, paymentState) ->
                val selectedAddress = addressState.selectedAddress
                    ?: addressState.addresses.firstOrNull { it.isDefault }
                    ?: addressState.addresses.firstOrNull()
                    ?: defaultAddress

                val selectedPaymentMethod = paymentState.selectedPaymentMethod
                    ?: paymentState.paymentMethods.firstOrNull { it.isDefault }
                    ?: paymentState.paymentMethods.firstOrNull()
                    ?: defaultPaymentMethod

                _uiState.update {
                    it.copy(
                        selectedAddress = selectedAddress,
                        selectedPaymentMethod = selectedPaymentMethod
                    )
                }
            }
        }
    }

    fun confirmarCompra() {
        val currentState = _uiState.value
        if (currentState.items.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Tu carrito esta vacio.") }
            return
        }
        val itemWithoutStock = currentState.items.firstOrNull { it.quantity > it.product.stock }
        if (itemWithoutStock != null) {
            _uiState.update {
                it.copy(
                    errorMessage = "El producto ${itemWithoutStock.product.title} solo tiene ${itemWithoutStock.product.stock} unidades disponibles."
                )
            }
            return
        }
        if (currentState.selectedAddress.idAddress.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Agrega una direccion de envio antes de confirmar.") }
            return
        }
        if (currentState.selectedPaymentMethod.idMethod.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Agrega un metodo de pago antes de confirmar.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val uid = SupabaseClient.client.auth.currentUserOrNull()?.id

            if (uid == null) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "No se pudo identificar al usuario actual."
                    )
                }
                return@launch
            }

            try {
                val saleId = UUID.randomUUID().toString()
                val orderItems = currentState.items.map { item ->
                    OrderItem(
                        productId = item.product.id,
                        productTitle = item.product.title,
                        artistName = item.product.artist?.name ?: "Artista desconocido",
                        quantity = item.quantity,
                        selectedFormat = item.selectedFormat,
                        unitPrice = item.product.price
                    )
                }
                val saleDetails = currentState.items.map { item ->
                    CreateSaleDetail(
                        idSale = saleId,
                        productId = item.product.id,
                        quantity = item.quantity,
                        unitPrice = item.product.price,
                        unitCost = item.product.price
                    )
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("sales")
                        .insert(
                            CreateSale(
                                idSale = saleId,
                                userId = uid,
                                idAddress = currentState.selectedAddress.idAddress,
                                total = currentState.total,
                                status = "PENDIENTE",
                                paymentMethod = currentState.selectedPaymentMethod.displayName
                            )
                        )

                    SupabaseClient.client
                        .from("sales_details")
                        .insert(saleDetails)
                }

                delay(250)

                val order = Order(
                    idSale = saleId,
                    userId = uid,
                    idAddress = currentState.selectedAddress.idAddress,
                    items = orderItems,
                    total = currentState.total,
                    status = "PENDIENTE",
                    paymentMethod = currentState.selectedPaymentMethod.displayName,
                    createdAt = System.currentTimeMillis().toString()
                )

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        orderSuccess = true,
                        createdOrder = order
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "No se pudo confirmar la orden. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun resetOrderSuccess() {
        _uiState.update { it.copy(orderSuccess = false) }
    }

    fun resetCheckoutState() {
        _uiState.value = CheckoutUiState()
        cartSyncJob?.cancel()
        customerDataJob?.cancel()
        cartSyncJob = null
        customerDataJob = null
    }
}

private const val SHIPPING_COST = 5.0

private val defaultAddress = Address(
    street = "Direccion pendiente",
    city = "La Paz",
    stateAddress = "BCS",
    postalCode = "23000",
    isDefault = true
)

private val defaultPaymentMethod = PaymentMethod(
    type = "card",
    brand = "VISA",
    last4 = "1234",
    isDefault = true
)
