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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel

@Composable
fun AddressFormScreen(
    addressViewModel: AddressViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by addressViewModel.uiState.collectAsState()
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            addressViewModel.consumeSaveSuccess()
            onNavigateBack()
        }
    }

    val validationMessage = when {
        street.isBlank() -> "Ingresa la calle y número."
        city.isBlank() -> "Ingresa la ciudad."
        state.isBlank() -> "Ingresa el estado."
        !InputValidators.isValidPostalCode(postalCode) -> "El código postal debe tener exactamente 5 dígitos."
        else -> null
    }
    val canSave = validationMessage == null && !uiState.isLoading

    Scaffold(
        topBar = {
            FormTopBar(
                title = "NUEVA DIRECCIÓN",
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

            FormTextField(
                label = "Calle y número",
                value = street,
                onValueChange = {
                    street = it.take(InputValidators.STREET_MAX_LENGTH)
                    validationError = null
                },
                capitalization = KeyboardCapitalization.Words
            )
            FormTextField(
                label = "Ciudad",
                value = city,
                onValueChange = {
                    city = it.take(InputValidators.CITY_MAX_LENGTH)
                    validationError = null
                },
                capitalization = KeyboardCapitalization.Words
            )
            FormTextField(
                label = "Estado",
                value = state,
                onValueChange = {
                    state = it.take(InputValidators.STATE_MAX_LENGTH)
                    validationError = null
                },
                capitalization = KeyboardCapitalization.Characters
            )
            FormTextField(
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
                        validationError = validationMessage
                        return@Button
                    }

                    addressViewModel.createAddress(
                        street = street,
                        city = city,
                        state = state,
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
                    text = if (uiState.isLoading) "GUARDANDO" else "GUARDAR DIRECCIÓN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
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
                keyboardType = keyboardType,
                capitalization = capitalization
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
private fun FormTopBar(
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
fun AddressFormScreenPreview() {
    AddressFormScreen()
}
