package com.ldaniel1505.lpzrecords.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.UsuarioPerfil
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

data class ProfileUiState(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val logoutSuccess: Boolean = false,
    val isUnauthenticated: Boolean = false
) {
    val displayName: String
        get() = name.ifBlank { email.ifBlank { "Cliente LPZ" } }

    val initials: String
        get() = displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "?" }
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                isUnauthenticated = false
            )
        }

        viewModelScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val uid = user?.id

                if (uid == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo identificar al usuario actual.",
                            isUnauthenticated = true
                        )
                    }
                    return@launch
                }

                val profile = withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("users")
                        .select {
                            filter {
                                eq("id", uid)
                            }
                        }
                        .decodeSingle<UsuarioPerfil>()
                }

                _uiState.update {
                    it.copy(
                        uid = uid,
                        name = profile.name,
                        email = user.email ?: "",
                        phone = profile.phone.orEmpty(),
                        isAdmin = profile.is_admin,
                        isLoading = false,
                        errorMessage = null,
                        successMessage = null,
                        isUnauthenticated = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar tu perfil."
                    )
                }
            }
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        phone: String
    ) {
        val currentState = _uiState.value
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()

        if (currentState.uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "No se pudo identificar al usuario actual.") }
            return
        }
        if (!InputValidators.isValidName(cleanName)) {
            _uiState.update {
                it.copy(errorMessage = "El nombre debe tener entre 2 y 80 caracteres.")
            }
            return
        }
        if (!cleanEmail.equals(currentState.email, ignoreCase = true)) {
            _uiState.update {
                it.copy(errorMessage = "El correo no se puede cambiar desde esta pantalla.")
            }
            return
        }
        if (!InputValidators.isValidOptionalPhone(cleanPhone)) {
            _uiState.update {
                it.copy(errorMessage = "El telefono debe tener exactamente 10 digitos.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client
                        .from("users")
                        .update(
                            mapOf(
                                "name" to cleanName,
                                "phone" to cleanPhone.ifBlank { null }
                            )
                        ) {
                            filter {
                                eq("id", currentState.uid)
                            }
                        }
                }

                _uiState.update {
                    it.copy(
                        name = cleanName,
                        phone = cleanPhone,
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Cambios guardados."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron guardar los cambios. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun consumeSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun logout() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, logoutSuccess = false) }

        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                clearProfile()
                _uiState.update { it.copy(logoutSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cerrar sesion."
                    )
                }
            }
        }
    }

    fun consumeLogoutSuccess() {
        _uiState.update { it.copy(logoutSuccess = false) }
    }

    fun clearProfile() {
        _uiState.value = ProfileUiState()
    }

    fun createPaymentMethod() {
        viewModelScope.launch {
            try {
                // Pendiente: se implementara con los formularios de metodos de pago.
            } catch (_: Exception) {
            }
        }
    }

    fun vincularTarjeta(id: String) {
        viewModelScope.launch {
            try {
                id.ifBlank { return@launch }
                // Pendiente: se implementara con los formularios de metodos de pago.
            } catch (_: Exception) {
            }
        }
    }
}
