package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.data.model.Address
import com.ldaniel1505.lpzrecords.ui.components.BottomNavTab
import com.ldaniel1505.lpzrecords.ui.components.LpzBottomNavBar
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel

@Composable
fun AddressesScreen(
    addressViewModel: AddressViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAddressForm: () -> Unit = {}
) {
    val uiState by addressViewModel.uiState.collectAsState()
    var addressToDelete by remember { mutableStateOf<Address?>(null) }

    LaunchedEffect(Unit) {
        addressViewModel.fetchAddresses()
    }

    addressToDelete?.let { pendingAddress ->
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Eliminar dirección",
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar la dirección \"${pendingAddress.summary}\"?",
                    color = LpzDark.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    addressViewModel.deleteAddress(pendingAddress.id)
                    addressToDelete = null
                }) {
                    Text("Eliminar", color = LpzRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("Cancelar", color = LpzDark.copy(alpha = 0.6f))
                }
            }
        )
    }

    Scaffold(
        topBar = { AddressesTopBar(onNavigateBack = onNavigateToProfile) },
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
        when {
            uiState.isLoading && uiState.addresses.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LpzRed)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    uiState.errorMessage?.let { message ->
                        item {
                            Text(
                                text = message,
                                color = LpzRed,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (uiState.addresses.isEmpty()) {
                        item {
                            EmptyAddressState()
                        }
                    }

                    items(uiState.addresses, key = { it.id }) { address ->
                        AddressCard(
                            address = address,
                            isSelected = uiState.selectedAddress?.id == address.id,
                            onSelect = { addressViewModel.selectAddress(address) },
                            onDelete = { addressToDelete = address }
                        )
                    }

                    item {
                        AddAddressButton(onClick = onNavigateToAddressForm)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAddressState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No tienes direcciones guardadas",
            fontSize = 15.sp,
            color = LpzDark.copy(alpha = 0.50f)
        )
    }
}

@Composable
private fun AddressCard(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (address.isDefault || isSelected) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) LpzDark else LpzRed
                    ) {
                        Text(
                            text = if (isSelected) "SELECCIONADA" else "PREDETERMINADA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = address.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = address.street,
                fontSize = 13.sp,
                color = LpzDark.copy(alpha = 0.66f)
            )
            Text(
                text = listOfNotNull(
                    address.city,
                    address.state.takeIf { it.isNotBlank() },
                    address.postalCode?.takeIf { it.isNotBlank() }?.let { "C.P. $it" }
                ).joinToString(", "),
                fontSize = 12.sp,
                color = LpzDark.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.12f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                Text(
                    text = "USAR",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LpzRed,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .clickable(onClick = onSelect)
                        .padding(vertical = 2.dp)
                )
                Text(
                    text = "ELIMINAR",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = LpzDark.copy(alpha = 0.55f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .clickable(onClick = onDelete)
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AddAddressButton(onClick: () -> Unit) {
    val dashedBorderColor = LpzDark.copy(alpha = 0.30f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                val strokePx = 1.5.dp.toPx()
                val dashPx = 12.dp.toPx()
                val gapPx = 6.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
                drawRoundRect(
                    color = dashedBorderColor,
                    style = Stroke(width = strokePx, pathEffect = pathEffect),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ Añadir dirección",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = LpzDark.copy(alpha = 0.60f),
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressesTopBar(onNavigateBack: () -> Unit) {
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
                    text = "DIRECCIONES",
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
fun AddressesScreenPreview() {
    AddressesScreen()
}
