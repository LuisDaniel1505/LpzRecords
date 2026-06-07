package com.ldaniel1505.lpzrecords.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.UsuarioPerfil
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import com.ldaniel1505.lpzrecords.util.InputValidators
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {

    // ESTADOS: Compose observara estas variables para cambiar la interfaz automaticamente
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Controla si el usuario ya se logueo y que rol tiene
    var loginSuccessByRole by mutableStateOf<Boolean?>(null) // true = admin, false = cliente, null = no logueado
        private set

    var signUpSuccess by mutableStateOf(false)
        private set

    var signUpRequiresEmailConfirmation by mutableStateOf(false)
        private set

    fun signUpUser(name: String, email: String, password: String) {
        errorMessage = null
        signUpSuccess = false
        signUpRequiresEmailConfirmation = false
        loginSuccessByRole = null

        val cleanName = name.trim()
        val cleanEmail = email.trim()

        if (!InputValidators.isValidName(cleanName)) {
            errorMessage = "El nombre debe tener entre 2 y 80 caracteres."
            return
        }
        if (!InputValidators.isValidEmail(cleanEmail)) {
            errorMessage = "Introduce un correo valido."
            return
        }
        if (!InputValidators.isValidPassword(password)) {
            errorMessage = "La contrasena debe tener al menos 6 caracteres."
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = cleanEmail
                    this.password = password
                    this.data = buildJsonObject {
                        put("name", cleanName)
                    }
                }

                if (SupabaseClient.client.auth.currentUserOrNull() != null) {
                    signUpSuccess = true
                } else {
                    signUpRequiresEmailConfirmation = true
                    errorMessage = "Cuenta creada. Revisa tu correo para confirmar el registro e inicia sesión."
                }
            } catch (e: Exception) {
                errorMessage = friendlyAuthError(e, isSignUp = true)
            } finally {
                isLoading = false
            }
        }
    }

    fun SignUpUser(name: String, email: String, password: String) {
        signUpUser(name, email, password)
    }

    fun loginUsuario(correo: String, contrasena: String) {
        errorMessage = null
        signUpSuccess = false
        signUpRequiresEmailConfirmation = false
        loginSuccessByRole = null

        val cleanEmail = correo.trim()
        if (!InputValidators.isValidEmail(cleanEmail)) {
            errorMessage = "Introduce un correo valido."
            return
        }
        if (contrasena.isEmpty()) {
            errorMessage = "Introduce tu contrasena."
            return
        }

        // Inicializamos estados de carga y limpiamos errores previos
        isLoading = true

        // viewModelScope.launch abre un hilo secundario asincrono (corrutina)
        // para que la aplicacion no se trabe mientras consulta a internet
        viewModelScope.launch {
            try {
                // 1. Autenticacion principal en Supabase (verifica correo y contrasena)
                SupabaseClient.client.auth.signInWith(Email) {
                    email = cleanEmail
                    password = contrasena
                }

                // 2. Extraer el UUID unico del usuario que acaba de ingresar
                val uid = SupabaseClient.client.auth.currentUserOrNull()?.id

                if (uid != null) {
                    // 3. Consultar en la tabla users usando el UUID para obtener el rol
                    val perfil = SupabaseClient.client.postgrest["users"]
                        .select {
                            filter { eq("id", uid) }
                        }.decodeSingle<UsuarioPerfil>()

                    // Exito: asignamos el valor de is_admin (true o false)
                    loginSuccessByRole = perfil.is_admin
                } else {
                    errorMessage = "No se pudo obtener el ID del usuario."
                }

            } catch (e: Exception) {
                errorMessage = friendlyAuthError(e, isSignUp = false)
            } finally {
                isLoading = false
            }
        }
    }

    // Función para limpiar el estado al cerrar sesión o cambiar de pantalla
    fun resetAuthState() {
        loginSuccessByRole = null
        signUpSuccess = false
        signUpRequiresEmailConfirmation = false
        errorMessage = null
    }

    private fun friendlyAuthError(error: Exception, isSignUp: Boolean): String {
        val message = error.localizedMessage.orEmpty()
        return when {
            message.contains("already registered", ignoreCase = true) ||
                    message.contains("already exists", ignoreCase = true) ||
                    message.contains("duplicate", ignoreCase = true) ->
                "Ya existe una cuenta con ese correo."
            message.contains("invalid login", ignoreCase = true) ||
                    message.contains("invalid credentials", ignoreCase = true) ->
                "Correo o contrasena incorrectos."
            message.contains("email not confirmed", ignoreCase = true) ->
                "Confirma tu correo antes de iniciar sesión."
            message.contains("network", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ->
                "No se pudo conectar con el servidor. Revisa tu conexion."
            isSignUp -> "No se pudo crear la cuenta. Intenta de nuevo."
            else -> "No se pudo iniciar sesión. Intenta de nuevo."
        }
    }
}
