package com.ldaniel1505.lpzrecords.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.CreatePaymentMethod
import com.ldaniel1505.lpzrecords.data.model.PaymentMethod
import com.ldaniel1505.lpzrecords.data.model.cvvToken
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.ldaniel1505.lpzrecords.util.InputValidators
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class PaymentMethodsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun fetchPaymentMethods() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val uid = currentUserIdOrThrow()
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("payment_methods")
                        .select {
                            filter { eq("id_user", uid) }
                        }
                        .decodeList<PaymentMethod>()
                }

                _uiState.update { state ->
                    val selected = state.selectedPaymentMethod
                        ?.takeIf { selectedMethod -> result.any { it.id == selectedMethod.id } }
                        ?: result.firstOrNull { it.isDefault }
                        ?: result.firstOrNull()

                    state.copy(
                        paymentMethods = result,
                        selectedPaymentMethod = selected,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        paymentMethods = emptyList(),
                        isLoading = false,
                        errorMessage = "No se pudieron cargar tus tarjetas."
                    )
                }
            }
        }
    }

    fun createPaymentMethod(
        cardNumber: String,
        cvv: String,
        expiryDate: String,
        cardHolder: String,
        postalCode: String
    ) {
        val cleanNumber = cardNumber.filter { it.isDigit() }
        val cleanCvv = cvv.trim()
        val cleanExpiryDate = expiryDate.trim()
        val cleanCardHolder = cardHolder.trim()
        val cleanPostalCode = postalCode.trim()

        if (cleanNumber.length != InputValidators.CARD_NUMBER_LENGTH) {
            _uiState.update { it.copy(errorMessage = "La tarjeta debe tener exactamente 16 números.") }
            return
        }
        if (!InputValidators.isValidSupportedCard(cleanNumber)) {
            _uiState.update { it.copy(errorMessage = "El número de tarjeta no es válido.") }
            return
        }
        if (!InputValidators.isValidCardCvv(cleanCvv)) {
            _uiState.update { it.copy(errorMessage = "El CVV debe tener exactamente 3 dígitos.") }
            return
        }
        if (!InputValidators.isValidCardExpiry(cleanExpiryDate)) {
            _uiState.update { it.copy(errorMessage = "Ingresa una fecha de vencimiento válida en formato MM/AA.") }
            return
        }
        if (!InputValidators.isValidCardHolder(cleanCardHolder)) {
            _uiState.update { it.copy(errorMessage = "Ingresa el nombre del titular de la tarjeta.") }
            return
        }
        if (!InputValidators.isValidPostalCode(cleanPostalCode)) {
            _uiState.update { it.copy(errorMessage = "El código postal debe tener exactamente 5 dígitos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, saveSuccess = false) }

        viewModelScope.launch {
            try {
                val uid = currentUserIdOrThrow()
                val shouldBeDefault = _uiState.value.paymentMethods.isEmpty()
                val provider = InputValidators.cardBrand(cleanNumber) ?: "TARJETA"
                val paymentMethodId = UUID.randomUUID().toString()

                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("payment_methods")
                        .insert(
                            CreatePaymentMethod(
                                idMethod = paymentMethodId,
                                userId = uid,
                                type = "card",
                                last4 = cleanNumber.takeLast(4),
                                brand = provider,
                                paymentToken = cvvToken(paymentMethodId, cleanCvv),
                                isDefault = shouldBeDefault
                            )
                        )
                }

                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                fetchPaymentMethods()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo guardar la tarjeta. Revisa los datos e intenta de nuevo."
                    )
                }
            }
        }
    }

    fun deletePaymentMethod(paymentMethodId: String) {
        if (paymentMethodId.isBlank()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val uid = currentUserIdOrThrow()

                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("payment_methods")
                        .delete {
                            filter {
                                eq("id_method", paymentMethodId)
                                eq("id_user", uid)
                            }
                        }
                }

                fetchPaymentMethods()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo eliminar la tarjeta. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearState() {
        _uiState.value = PaymentMethodsUiState()
    }

    private fun currentUserIdOrThrow(): String {
        return SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }

}
