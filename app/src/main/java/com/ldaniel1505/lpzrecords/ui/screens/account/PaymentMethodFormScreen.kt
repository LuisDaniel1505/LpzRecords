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
import com.ldaniel1505.lpzrecords.util.InputValidators
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel

@Composable
fun PaymentMethodFormScreen(
    paymentMethodsViewModel: PaymentMethodsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by paymentMethodsViewModel.uiState.collectAsState()
    var cardNumber by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            paymentMethodsViewModel.consumeSaveSuccess()
            onNavigateBack()
        }
    }

    val cleanNumber = cardNumber.filter { it.isDigit() }
    val canSave = InputValidators.isValidSupportedCard(cleanNumber) &&
            InputValidators.isValidCardCvv(cvv) &&
            InputValidators.isValidCardExpiry(expiryDate) &&
            InputValidators.isValidCardHolder(cardHolder) &&
            InputValidators.isValidPostalCode(postalCode) &&
            !uiState.isLoading

    Scaffold(
        topBar = {
            PaymentFormTopBar(
                title = "Nueva tarjeta",
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
                label = "Número de tarjeta",
                value = cardNumber,
                onValueChange = {
                    val digits = it.filter { char -> char.isDigit() }
                    cardNumber = digits.take(InputValidators.CARD_NUMBER_LENGTH)
                    validationError = if (digits.length > InputValidators.CARD_NUMBER_LENGTH) {
                        "Solo se permiten 16 números."
                    } else {
                        null
                    }
                },
                keyboardType = KeyboardType.Number
            )

            PaymentTextField(
                label = "CVV",
                value = cvv,
                onValueChange = {
                    cvv = InputValidators.digitsOnly(it, InputValidators.CARD_CVV_LENGTH)
                    validationError = null
                },
                keyboardType = KeyboardType.Number
            )

            PaymentTextField(
                label = "Fecha de vencimiento (MM/AA)",
                value = expiryDate,
                onValueChange = {
                    expiryDate = formatExpiryDate(it)
                    validationError = null
                },
                keyboardType = KeyboardType.Number
            )

            PaymentTextField(
                label = "Nombre del titular",
                value = cardHolder,
                onValueChange = {
                    cardHolder = it.take(InputValidators.CARD_HOLDER_MAX_LENGTH)
                    validationError = null
                }
            )

            PaymentTextField(
                label = "Código postal",
                value = postalCode,
                onValueChange = {
                    postalCode = InputValidators.digitsOnly(it, InputValidators.POSTAL_CODE_LENGTH)
                    validationError = null
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
                        validationError = when {
                            cleanNumber.length != InputValidators.CARD_NUMBER_LENGTH ->
                                "La tarjeta debe tener exactamente 16 números."
                            !InputValidators.isValidSupportedCard(cleanNumber) ->
                                "El número de tarjeta no es válido."
                            !InputValidators.isValidCardCvv(cvv) ->
                                "El CVV debe tener exactamente 3 dígitos."
                            !InputValidators.isValidCardExpiry(expiryDate) ->
                                "Ingresa una fecha de vencimiento válida en formato MM/AA."
                            !InputValidators.isValidCardHolder(cardHolder) ->
                                "Ingresa el nombre del titular de la tarjeta."
                            else ->
                                "El código postal debe tener exactamente 5 dígitos."
                        }
                        return@Button
                    }

                    paymentMethodsViewModel.createPaymentMethod(
                        cardNumber = cardNumber,
                        cvv = cvv,
                        expiryDate = expiryDate,
                        cardHolder = cardHolder,
                        postalCode = postalCode
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
                    text = if (uiState.isLoading) "Guardando..." else "Guardar tarjeta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Se admiten tarjetas Visa y Mastercard de 16 dígitos. Los datos son simulados; solo se guardarán los últimos cuatro dígitos.",
                fontSize = 12.sp,
                color = LpzDark.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun formatExpiryDate(value: String): String {
    val digits = value.filter { it.isDigit() }.take(4)
    return if (digits.length <= 2) {
        digits
    } else {
        "${digits.take(2)}/${digits.drop(2)}"
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
