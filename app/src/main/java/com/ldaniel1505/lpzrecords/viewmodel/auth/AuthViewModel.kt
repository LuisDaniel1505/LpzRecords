package com.ldaniel1505.lpzrecords.viewmodel.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldaniel1505.lpzrecords.data.model.CreateUserProfile
import com.ldaniel1505.lpzrecords.data.model.UsuarioPerfil
import com.ldaniel1505.lpzrecords.data.network.SupabaseClient
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

    fun signUpUser(name: String, email: String, password: String) {
        isLoading = true
        errorMessage = null
        signUpSuccess = false
        loginSuccessByRole = null

        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.data = buildJsonObject {
                        put("name", name.trim())
                    }
                }

                val uid = SupabaseClient.client.auth.currentUserOrNull()?.id

                if (uid == null) {
                    errorMessage = "No se pudo obtener el ID del usuario registrado."
                    return@launch
                }

                val profile = CreateUserProfile(
                    id = uid,
                    name = name.trim(),
                    is_admin = false
                )

                SupabaseClient.client.postgrest["users"].insert(profile)
                signUpSuccess = true

            } catch (e: Exception) {
                Log.e("SupabaseError", "Error Completo: ${e.message}")
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                // Quitamos la animacion de carga sin importar si fallo o funciono
                isLoading = false
            }
        }
    }

    fun SignUpUser(name: String, email: String, password: String) {
        signUpUser(name, email, password)
    }

    fun loginUsuario(correo: String, contrasena: String) {
        // Inicializamos estados de carga y limpiamos errores previos
        isLoading = true
        errorMessage = null
        signUpSuccess = false
        loginSuccessByRole = null

        // viewModelScope.launch abre un hilo secundario asincrono (corrutina)
        // para que la aplicacion no se trabe mientras consulta a internet
        viewModelScope.launch {
            try {
                // 1. Autenticacion principal en Supabase (verifica correo y contrasena)
                SupabaseClient.client.auth.signInWith(Email) {
                    email = correo
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
                // Si la contrasena esta mal, el correo no existe o no hay internet, caera aqui
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                // Quitamos la animacion de carga sin importar si fallo o funciono
                isLoading = false
            }
        }
    }

    // Funcion para limpiar el estado al cerrar sesion o cambiar de pantalla
    fun resetAuthState() {
        loginSuccessByRole = null
        signUpSuccess = false
        errorMessage = null
    }
}
