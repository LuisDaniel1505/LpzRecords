package com.ldaniel1505.lpzrecords.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.viewmodel.auth.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    // Modificamos esto: Ahora avisa hacia afuera si el que entró es administrador o no
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    // Pedimos el ViewModel como parámetro de la pantalla
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // --- ESTADOS LOCALES ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validacionLocalError by remember { mutableStateOf("") }

    // --- EFECTO DE ESCUCHA (LaunchedEffect) ---
    // LaunchedEffect se queda vigilando 'viewModel.loginSuccessByRole'.
    // En cuanto cambie de 'null' a true/false (porque Supabase respondió), ejecuta la navegación.
    LaunchedEffect(viewModel.loginSuccessByRole) {
        viewModel.loginSuccessByRole?.let { isAdmin ->
            onLoginSuccess(isAdmin)
            viewModel.resetAuthState() // Limpiamos el estado para que no se quede en un bucle al regresar
        }
    }
    Surface(
        color = LpzBeige,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(40.dp))

                Text(
                    text = "LOGIN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )

                Icon(
                    painter = painterResource(id = R.drawable.vinyl), // TODO: Cambiar por logo circular
                    contentDescription = "Mini Logo",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- CONTENEDOR CENTRAL ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DCC3)) // Beige un poco más oscuro para el contraste
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "CORREO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark
                    )

                    // TODO (BACKEND): Aquí se necesita conexión para recibir/validar datos
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("correo@gmail.com", color = Color.Gray) },
                        textStyle = TextStyle(color = Color.Black),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CONTRASEÑA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark
                    )

                    // TODO (BACKEND): Conectar con lógica de cifrado o Firebase Auth
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("••••••••••••", color = Color.Gray) },
                        textStyle = TextStyle(color = Color.Black),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),

                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // --- MENSAJES DE ERROR (Local o de Supabase) ---
                    val errorAMostrar = validacionLocalError.ifEmpty { viewModel.errorMessage ?: "" }
                    if (errorAMostrar.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorAMostrar,
                            color = LpzRed, // O usa tu color rojo del tema
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // --- BOTÓN INICIAR SESIÓN ---
                    Button(
                        onClick = {
                            // 1. Validación local para evitar peticiones innecesarias a internet
                            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                                validacionLocalError = "Por favor, introduce correo y contraseña."
                            } else {
                                // 2. Si están llenos, llamamos a la función de Supabase
                                viewModel.loginUsuario(email.trim(), password.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isLoading // Desactiva el botón para evitar doble clic
                    ) {
                        // Si el ViewModel dice que está cargando, dibuja un círculo de carga
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "INICIAR SESIÓN",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- LINK A REGISTRO ---
            TextButton(
                onClick = onNavigateToSignUp,
                enabled = !viewModel.isLoading // Evita salir de la pantalla si está cargando
            ) {
                Text(
                    text = "¿Nuevo aquí? Crea una cuenta",
                    color = LpzDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToSignUp = {},
        onLoginSuccess = {}
    )
}