package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun PersonalInfoScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // TODO (BACKEND): Pre-cargar datos del usuario desde el ViewModel.
    // val userState by personalInfoViewModel.userState.collectAsState()
    // LaunchedEffect(Unit) { personalInfoViewModel.loadUserInfo() }
    var fullName by remember { mutableStateOf("Luis Daniel Ontiveros Lares") }
    var email    by remember { mutableStateOf("danidash@ejemplo.com") }
    var phone    by remember { mutableStateOf("+52 612 123 1212") }

    val hasChanges = remember(fullName, email, phone) {
        fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
    }

    Scaffold(
        topBar = { PersonalInfoTopBar(onNavigateBack = onNavigateToProfile) },
        bottomBar = {
            LpzBottomNavBar(
                selectedTab = BottomNavTab.PROFILE,
                onHome      = onNavigateToHome,
                onSearch    = onNavigateToSearch,
                onCart      = onNavigateToCart,
                onFavorites = onNavigateToFavorites,
                onProfile   = onNavigateToProfile
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
                label       = "NOMBRE COMPLETO",
                value       = fullName,
                onValueChange = { fullName = it },
                keyboardType  = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            )

            EditableInfoCard(
                label       = "CORREO ELECTRONICO",
                value       = email,
                onValueChange = { email = it },
                keyboardType  = KeyboardType.Email
            )

            EditableInfoCard(
                label       = "TELEFONO",
                value       = phone,
                onValueChange = { phone = it },
                keyboardType  = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    /*
                     * TODO (BACKEND):
                     * 1. Validar formato de email con Patterns.EMAIL_ADDRESS.
                     * 2. Validar longitud mínima del teléfono.
                     * 3. personalInfoViewModel.saveUserInfo(fullName, email, phone)
                     * 4. Si success -> mostrar Snackbar / navegar atrás.
                     */
                },
                enabled = hasChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = LpzDark,
                    contentColor           = Color.White,
                    disabledContainerColor = LpzDark.copy(alpha = 0.4f),
                    disabledContentColor   = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text       = "GUARDAR CAMBIOS",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTE: Campo editable
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun EditableInfoCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    val focusRequester = remember { FocusRequester() }

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text          = label,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = LpzRed,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value         = value,
                    onValueChange = onValueChange,
                    singleLine    = true,
                    textStyle = TextStyle(
                        fontSize   = 16.sp,
                        color      = LpzDark,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush     = SolidColor(LpzRed),
                    keyboardOptions = KeyboardOptions(
                        keyboardType   = keyboardType,
                        capitalization = capitalization
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )

                IconButton(
                    onClick  = { focusRequester.requestFocus() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Editar $label",
                        tint               = LpzDark.copy(alpha = 0.40f),
                        modifier           = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint               = LpzDark
                )
            }
        },
        title = {
            Box(
                modifier        = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "INF PERSONAL",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LpzDark
                )
            }
        },
        actions = {
            Icon(
                painter            = painterResource(id = R.drawable.vinyl),
                contentDescription = "Logo LPZ Records",
                modifier           = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp),
                tint = Color.Unspecified
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ═══════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PersonalInfoScreenPreview() {
    PersonalInfoScreen()
}