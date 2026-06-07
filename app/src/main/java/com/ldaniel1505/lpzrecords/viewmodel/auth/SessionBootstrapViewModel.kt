package com.ldaniel1505.lpzrecords.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.UsuarioPerfil
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SessionBootstrapUiState(
    val isLoading: Boolean = true,
    val destination: SessionDestination? = null,
    val errorMessage: String? = null
)

enum class SessionDestination {
    PUBLIC_HOME,
    CATALOG,
    ADMIN
}

class SessionBootstrapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SessionBootstrapUiState())
    val uiState: StateFlow<SessionBootstrapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                when (status) {
                    SessionStatus.LoadingFromStorage -> {
                        _uiState.value = SessionBootstrapUiState(isLoading = true)
                    }
                    is SessionStatus.Authenticated -> resolveAuthenticatedUser()
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = SessionBootstrapUiState(
                            isLoading = false,
                            destination = SessionDestination.PUBLIC_HOME
                        )
                    }
                    SessionStatus.NetworkError -> {
                        _uiState.value = SessionBootstrapUiState(
                            isLoading = false,
                            errorMessage = "No se pudo restaurar la sesión. Revisa tu conexión."
                        )
                    }
                }
            }
        }
    }

    fun retry() {
        _uiState.value = SessionBootstrapUiState(isLoading = true)
        viewModelScope.launch {
            if (SupabaseClient.client.auth.currentUserOrNull() == null) {
                _uiState.value = SessionBootstrapUiState(
                    isLoading = false,
                    destination = SessionDestination.PUBLIC_HOME
                )
            } else {
                resolveAuthenticatedUser()
            }
        }
    }

    private suspend fun resolveAuthenticatedUser() {
        _uiState.update { it.copy(isLoading = true, destination = null, errorMessage = null) }

        val uid = SupabaseClient.client.auth.currentUserOrNull()?.id
        if (uid == null) {
            _uiState.value = SessionBootstrapUiState(
                isLoading = false,
                destination = SessionDestination.PUBLIC_HOME
            )
            return
        }

        try {
            val profile = withContext(Dispatchers.IO) {
                SupabaseClient.client
                    .from("users")
                    .select {
                        filter { eq("id", uid) }
                    }
                    .decodeSingle<UsuarioPerfil>()
            }

            _uiState.value = SessionBootstrapUiState(
                isLoading = false,
                destination = if (profile.is_admin) {
                    SessionDestination.ADMIN
                } else {
                    SessionDestination.CATALOG
                }
            )
        } catch (_: Exception) {
            _uiState.value = SessionBootstrapUiState(
                isLoading = false,
                errorMessage = "No se pudo cargar el perfil de la sesión guardada."
            )
        }
    }
}
