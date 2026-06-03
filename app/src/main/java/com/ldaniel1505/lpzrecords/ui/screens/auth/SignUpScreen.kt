package com.ldaniel1505.lpzrecords.ui.screens.auth

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.theme.*
import com.ldaniel1505.lpzrecords.viewmodel.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (isAdmin: Boolean) -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    LaunchedEffect(viewModel.signUpSuccess) {
        if (viewModel.signUpSuccess) {
            onSignUpSuccess(false)
            viewModel.resetAuthState()
        }
    }

    var validationError by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isNameValid(name: String): Boolean {
        return name.trim().isNotEmpty()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
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
                    text = "REGISTRO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )

                Icon(
                    painter = painterResource(id = R.drawable.vinyl),
                    contentDescription = "Mini Logo",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DCC3))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "USUARIO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark
                    )

                    TextField(
                        value = username,
                        onValueChange = {
                            username = it
                            validationError = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textStyle = TextStyle(color = Color.Black),
                        placeholder = { Text("Nombre", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "CORREO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark
                    )

                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validationError = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textStyle = TextStyle(color = Color.Black),
                        placeholder = { Text("correo@gmail.com", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "CONTRASEÑA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpzDark
                    )

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            validationError = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textStyle = TextStyle(color = Color.Black),
                        placeholder = { Text("••••••••••••", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val errorAMostrar = validationError.ifEmpty { viewModel.errorMessage ?: "" }
                    if (errorAMostrar.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorAMostrar,
                            color = LpzRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val cleanName = username.trim()
                            val cleanEmail = email.trim()

                            if (!isNameValid(cleanName)) {
                                validationError = "Por favor, introduce tu nombre."
                            } else if (!isValidEmail(email = cleanEmail)) {
                                validationError = "Por favor, introduce un correo válido."
                            } else if (!isPasswordValid(password = password)) {
                                validationError = "Error en los datos ingresados"
                            } else {
                                validationError = ""
                                viewModel.signUpUser(cleanName, cleanEmail, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "REGISTRARSE",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = LpzDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigateToLogin = {},
        onSignUpSuccess = {}
    )
}
