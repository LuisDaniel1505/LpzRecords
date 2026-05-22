package com.ldaniel1505.lpzrecords.viewmodel.auth


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.UsuarioPerfil
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // ESTADOS: Compose observará estas variables para cambiar la interfaz automáticamente
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Controla si el usuario ya se logueó y qué rol tiene
    var loginSuccessByRole by mutableStateOf<Boolean?>(null) // true = admin, false = cliente, null = no logueado
        private set

    fun loginUsuario(correo: String, contrasena: String) {
        // Inicializamos estados de carga y limpiamos errores previos
        isLoading = true
        errorMessage = null

        // viewModelScope.launch abre un hilo secundario asíncrono (Corrútina)
        // para que la aplicación no se trabe mientras consulta a internet
        viewModelScope.launch {
            try {
                // 1. Autenticación principal en Supabase (Verifica correo y contraseña)
                SupabaseClient.client.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }

                // 2. Extraer el UUID único del usuario que acaba de ingresar
                val uid = SupabaseClient.client.auth.currentUserOrNull()?.id

                if (uid != null) {
                    // 3. Consultar en TU tabla 'usuarios' usando el UUID para obtener el rol
                    val perfil = SupabaseClient.client.postgrest["users"]
                        .select {
                            filter { eq("id", uid) }
                        }.decodeSingle<UsuarioPerfil>()

                    // Éxito: Le asignamos el valor de 'es_admin' (true o false)
                    loginSuccessByRole = perfil.is_admin
                } else {
                    errorMessage = "No se pudo obtener el ID del usuario."
                }

            } catch (e: Exception) {
                // Si la contraseña está mal, el correo no existe o no hay internet, caerá aquí
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                // Quitamos la animación de carga sin importar si falló o funcionó
                isLoading = false
            }
        }
    }

    // Función para limpiar el estado al cerrar sesión o cambiar de pantalla
    fun resetAuthState() {
        loginSuccessByRole = null
        errorMessage = null
    }
}