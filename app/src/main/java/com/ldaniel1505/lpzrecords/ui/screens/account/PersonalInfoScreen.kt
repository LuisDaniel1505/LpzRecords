package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.util.InputValidators
import com.ldaniel1505.lpzrecords.viewmodel.profile.ProfileViewModel

@Composable
fun PersonalInfoScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val profileState by profileViewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    LaunchedEffect(profileState.name, profileState.email, profileState.phone) {
        fullName = profileState.name
        email = profileState.email
        phone = profileState.phone
    }

    val hasChanges = remember(fullName, email, phone, profileState.name, profileState.email, profileState.phone) {
        InputValidators.isValidName(fullName) &&
                InputValidators.isValidOptionalPhone(phone) &&
                (
                        fullName != profileState.name ||
                                phone != profileState.phone
                        )
    }
    val localValidationMessage = when {
        fullName.isNotBlank() && !InputValidators.isValidName(fullName) ->
            "El nombre debe tener entre 2 y 80 caracteres."
        phone.isNotBlank() && !InputValidators.isValidOptionalPhone(phone) ->
            "El teléfono debe tener exactamente 10 dígitos."
        else -> null
    }

    Scaffold(
        topBar = { PersonalInfoTopBar(onNavigateBack = onNavigateToProfile) },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = null,
                onHome = onNavigateToHome,
                onSearch = onNavigateToSearch,
                onCart = onNavigateToCart,
                onFavorites = onNavigateToFavorites,
                onProfile = onNavigateToProfile
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            EditableInfoCard(
                label = "NOMBRE COMPLETO",
                value = fullName,
                onValueChange = { fullName = it.take(InputValidators.NAME_MAX_LENGTH) },
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            )

            EditableInfoCard(
                label = "CORREO ELECTROÓNICO",
                value = email,
                onValueChange = {},
                keyboardType = KeyboardType.Email,
                editable = false
            )

            EditableInfoCard(
                label = "TELÉFONO",
                value = phone,
                onValueChange = {
                    phone = InputValidators.digitsOnly(it, InputValidators.PHONE_LENGTH)
                },
                keyboardType = KeyboardType.Phone
            )

            localValidationMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = LpzRed
                )
            }

            profileState.errorMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = LpzRed
                )
            }

            profileState.successMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = LpzDark.copy(alpha = 0.72f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    profileViewModel.updateProfile(
                        name = fullName,
                        email = email,
                        phone = phone
                    )
                },
                enabled = hasChanges && !profileState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LpzDark,
                    contentColor = Color.White,
                    disabledContainerColor = LpzDark.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "GUARDAR CAMBIOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EditableInfoCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    editable: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LpzRed,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = !editable,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = LpzDark,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(LpzRed),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        capitalization = capitalization
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )

                if (editable) {
                    IconButton(
                        onClick = { focusRequester.requestFocus() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar $label",
                            tint = LpzDark.copy(alpha = 0.40f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoTopBar(onNavigateBack: () -> Unit) {
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
                    text = "INFO PERSONAL",
                    fontSize = 22.sp,
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
fun PersonalInfoScreenPreview() {
    PersonalInfoScreen()
}
