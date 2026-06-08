package com.ldaniel1505.lpzrecords.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.Address
import com.ldaniel1505.lpzrecords.data.model.CreateAddress
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

data class AddressUiState(
    val addresses: List<Address> = emptyList(),
    val selectedAddress: Address? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class AddressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    fun fetchAddresses() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val uid = currentUserIdOrThrow()
                val result = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("addresses")
                        .select {
                            filter { eq("id_user", uid) }
                        }
                        .decodeList<Address>()
                }

                _uiState.update { state ->
                    val selected = state.selectedAddress
                        ?.takeIf { selectedAddress -> result.any { it.id == selectedAddress.id } }
                        ?: result.firstOrNull { it.isDefault }
                        ?: result.firstOrNull()

                    state.copy(
                        addresses = result,
                        selectedAddress = selected,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        addresses = emptyList(),
                        isLoading = false,
                        errorMessage = "No se pudieron cargar tus direcciones."
                    )
                }
            }
        }
    }

    fun createAddress(
        street: String,
        city: String,
        state: String,
        postalCode: String
    ) {
        val cleanStreet = street.trim()
        val cleanCity = city.trim()
        val cleanState = state.trim()
        val cleanPostalCode = postalCode.trim()

        if (
            cleanStreet.isBlank() ||
            cleanCity.isBlank() ||
            cleanState.isBlank() ||
            cleanPostalCode.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Completa todos los campos de dirección.") }
            return
        }
        if (cleanStreet.length > InputValidators.STREET_MAX_LENGTH) {
            _uiState.update { it.copy(errorMessage = "La calle y número no pueden exceder 150 caracteres.") }
            return
        }
        if (cleanCity.length > InputValidators.CITY_MAX_LENGTH) {
            _uiState.update { it.copy(errorMessage = "La ciudad no puede exceder 80 caracteres.") }
            return
        }
        if (cleanState.length > InputValidators.STATE_MAX_LENGTH) {
            _uiState.update { it.copy(errorMessage = "El estado no puede exceder 80 caracteres.") }
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
                val shouldBeDefault = _uiState.value.addresses.isEmpty()

                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("addresses")
                        .insert(
                            CreateAddress(
                                idAddress = UUID.randomUUID().toString(),
                                userId = uid,
                                street = cleanStreet,
                                city = cleanCity,
                                stateAddress = cleanState,
                                postalCode = cleanPostalCode,
                                isDefault = shouldBeDefault
                            )
                        )
                }

                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                fetchAddresses()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo guardar la dirección. Revisa los datos e intenta de nuevo."
                    )
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        if (addressId.isBlank()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val uid = currentUserIdOrThrow()

                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("addresses")
                        .delete {
                            filter {
                                eq("id_address", addressId)
                                eq("id_user", uid)
                            }
                        }
                }

                fetchAddresses()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo eliminar la dirección. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun selectAddress(address: Address) {
        _uiState.update { it.copy(selectedAddress = address) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearState() {
        _uiState.value = AddressUiState()
    }

    private fun currentUserIdOrThrow(): String {
        return SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }
}
