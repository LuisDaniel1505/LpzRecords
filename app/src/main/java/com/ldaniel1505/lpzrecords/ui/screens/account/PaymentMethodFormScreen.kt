package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel

@Composable
fun PaymentMethodFormScreen(
    paymentMethodsViewModel: PaymentMethodsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by paymentMethodsViewModel.uiState.collectAsState()
    var cardNumber by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            paymentMethodsViewModel.consumeSaveSuccess()
            onNavigateBack()
        }
    }

    val cleanNumber = cardNumber.filter { it.isDigit() }
    val canSave = cleanNumber.length == CARD_NUMBER_LENGTH && !uiState.isLoading

    Scaffold(
        topBar = {
            PaymentFormTopBar(
                title = "NUEVA TARJETA",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PaymentTextField(
                label = "Numero de tarjeta",
                value = cardNumber,
                onValueChange = {
                    val digits = it.filter { char -> char.isDigit() }
                    cardNumber = digits.take(CARD_NUMBER_LENGTH)
                    validationError = if (digits.length > CARD_NUMBER_LENGTH) {
                        "Solo se permiten 16 numeros."
                    } else {
                        null
                    }
                },
                keyboardType = KeyboardType.Number
            )

            validationError?.let { message ->
                Text(text = message, color = LpzRed, fontSize = 13.sp)
            }

            uiState.errorMessage?.let { message ->
                Text(text = message, color = LpzRed, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (!canSave) {
                        validationError = "La tarjeta debe tener exactamente 16 numeros."
                        return@Button
                    }

                    paymentMethodsViewModel.createPaymentMethod(
                        cardNumber = cardNumber
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (uiState.isLoading) "GUARDANDO" else "GUARDAR TARJETA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Ingresa 16 numeros. Solo se guardaran los ultimos cuatro digitos.",
                fontSize = 12.sp,
                color = LpzDark.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PaymentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentFormTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = LpzDark
                )
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }
        },
        actions = {
            Icon(
                painter = painterResource(id = R.drawable.vinyl),
                contentDescription = "Logo LPZ Records",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp),
                tint = Color.Unspecified
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PaymentMethodFormScreenPreview() {
    PaymentMethodFormScreen()
}

private const val CARD_NUMBER_LENGTH = 16
