package com.ldaniel1505.lpzrecords.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AccountScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToPersonalInfo: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    // TODO (BACKEND): Obtener datos del usuario autenticado desde el ViewModel.
    // val userState by accountViewModel.userState.collectAsState()
    // val userName    = userState.fullName
    // val userInitial = userState.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    // val purchaseCount = userState.purchaseCount
    val userName      = "Diego Careaga" // Placeholder
    val userInitial   = "D"             // Placeholder
    val purchaseCount = 2               // TODO (BACKEND): accountViewModel.purchaseCount

    Scaffold(
        topBar = { AccountTopBar() },
        bottomBar = {
            AccountBottomBar(
                onHome      = onNavigateToHome,
                onSearch    = onNavigateToSearch,
                onCart      = onNavigateToCart,
                onFavorites = onNavigateToFavorites,
                onProfile   = { /* Ya estamos en Perfil */ }
            )
        },
        containerColor = LpzBeige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            //  Avatar
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(LpzDark),
                contentAlignment = Alignment.Center
            ) {
                /*
                 * TODO (BACKEND + COIL): Mostrar foto de perfil del usuario.
                 * Cuando tengas la URL de la foto, reemplaza este bloque con:
                 *
                 *   AsyncImage(
                 *       model = userState.photoUrl,
                 *       contentDescription = userState.fullName,
                 *       contentScale = ContentScale.Crop,
                 *       modifier = Modifier.fillMaxSize().clip(CircleShape)
                 *   )
                 *
                 * Dependencia: implementation("io.coil-kt:coil-compose:2.6.0")
                 */
                Text(
                    text = userInitial,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nombre del usuario
            Text(
                text = userName,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark
            )

            Spacer(modifier = Modifier.height(28.dp))

            //  Sección: Mi Actividad
            SectionHeader(title = "MI ACTIVIDAD")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                onClick = onNavigateToOrders,
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icono "Mis compras"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LpzRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Mis compras",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LpzDark
                        )
                    }
                    // TODO (BACKEND): Reemplazar purchaseCount con dato real del ViewModel
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$purchaseCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LpzDark.copy(alpha = 0.55f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Ver mis compras",
                            tint = LpzDark.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sección: Configuración
            SectionHeader(title = "CONFIGURACION")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ConfigRow(
                        title    = "Información personal",
                        subtitle = "Nombre, correo, teléfono",
                        onClick  = onNavigateToPersonalInfo
                    )
                    RowDivider()
                    ConfigRow(
                        title    = "Direcciones de envío",
                        subtitle = "Selecciona puntos de entrega",
                        onClick  = onNavigateToAddresses
                    )
                    RowDivider()
                    ConfigRow(
                        title    = "Métodos de pago",
                        subtitle = "Tarjetas guardadas",
                        onClick  = onNavigateToPaymentMethods
                    )
                    RowDivider()
                    ConfigRowWithSwitch(
                        title    = "Notificaciones",
                        subtitle = "Avisos de ofertas y envíos",
                        checked  = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            // TODO (BACKEND): accountViewModel.updateNotificationPreference(it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = {
                    // TODO (BACKEND): accountViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, LpzRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LpzRed)
            ) {
                Text(
                    text = "CERRAR SESIÓN",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  COMPONENTES INTERNOS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = LpzRed,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color.Gray.copy(alpha = 0.12f),
        thickness = 1.dp
    )
}

@Composable
private fun ConfigRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            /*
             * TODO: Se va a necesitar que se cambie este Box por el icono real de cada fila, por ejemplo:
             *   Icon(painter = painterResource(R.drawable.ic_person), ...)
             *   Icon(painter = painterResource(R.drawable.ic_location), ...)
             *   Icon(painter = painterResource(R.drawable.ic_credit_card), ...)
             */
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LpzDark)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LpzDark
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = LpzDark.copy(alpha = 0.50f)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Ir a $title",
            tint = LpzDark.copy(alpha = 0.40f),
            modifier = Modifier.size(22.dp)
        )
    }
}


@Composable
private fun ConfigRowWithSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // TODO: Mismo reemplazo de icono que en ConfigRow (ej. Icons.Outlined.Notifications)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LpzDark)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LpzDark
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = LpzDark.copy(alpha = 0.50f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = LpzRed,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.35f)
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTopBar() {
    TopAppBar(
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AccountBottomBar(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color = LpzBeige,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .navigationBarsPadding()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(icon = Icons.Default.Home,          label = "Inicio",    isSelected = false, onClick = onHome)
            BottomNavItem(icon = Icons.Default.Search,        label = "Buscar",    isSelected = false, onClick = onSearch)

            // Botón central del carrito
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(LpzRed)
                    .clickable(onClick = onCart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito de compras",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            BottomNavItem(icon = Icons.Default.FavoriteBorder, label = "Favoritos", isSelected = false, onClick = onFavorites)
            // "Perfil" resaltado en LpzRed porque es la pantalla activa
            BottomNavItem(icon = Icons.Default.Person,          label = "Perfil",   isSelected = true,  onClick = onProfile)
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) LpzRed else LpzDark
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = label, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountScreenPreview() {
    AccountScreen()
}
