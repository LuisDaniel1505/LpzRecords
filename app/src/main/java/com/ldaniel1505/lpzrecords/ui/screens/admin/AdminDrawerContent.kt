package com.ldaniel1505.lpzrecords.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.theme.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════
//  MODELO: Secciones del menú admin
// ═══════════════════════════════════════════════════════════════════════════

enum class AdminSection(
    val label: String,
    val icon: ImageVector
) {
    RESUMEN(   "Resumen",         Icons.Default.Home),
    PRODUCTOS( "Productos",       Icons.Default.List),
    ENVIOS(    "Envíos y Pedidos", Icons.Default.ShoppingCart)
}

// ═══════════════════════════════════════════════════════════════════════════
//  CONTENIDO DEL DRAWER
//  Se usa dentro de ModalNavigationDrawer en la pantalla host del admin.
//  No tiene su propio Scaffold para que el drawer pueda solaparse
//  correctamente sobre el contenido.
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AdminDrawerContent(
    selectedSection: AdminSection,
    onSectionSelected: (AdminSection) -> Unit,
    onLogout: () -> Unit
    // TODO (BACKEND): Inyectar datos del usuario autenticado:
    // adminName: String, adminRole: String
) {
    // Fondo oscuro — toda la superficie del drawer
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(LpzDark)
            .padding(horizontal = 0.dp)
    ) {

        // ── Cabecera: Logo + nombre + rol ──────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 52.dp, bottom = 28.dp, end = 22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Círculo con ícono de vinilo / logo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LpzRed),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Reemplazar con painterResource(R.drawable.vinyl)
                // cuando quieras usar el logo real:
                // Icon(painter = painterResource(R.drawable.vinyl),
                //      contentDescription = null, tint = Color.Unspecified,
                //      modifier = Modifier.size(28.dp))
                Icon(
                    painter            = painterResource(id = R.drawable.vinyl),
                    contentDescription = "Logo LPZ Records",
                    modifier           = Modifier
                        .size(38.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text       = "LPZ",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            Text(
                // TODO (BACKEND): Reemplazar con el rol real del usuario autenticado
                text          = "PANEL DE CONTROL",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = LpzRed,
                letterSpacing = 0.8.sp
            )
        }

        HorizontalDivider(
            color     = Color.White.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier  = Modifier.padding(horizontal = 22.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Items del menú ─────────────────────────────────────────────
        AdminSection.entries.forEach { section ->
            DrawerMenuItem(
                section    = section,
                isSelected = section == selectedSection,
                onClick    = {
                    onSectionSelected(section)
                    // TODO (BACKEND): registrar navegación en analytics si aplica
                }
            )
        }

        // ── Empuja "Cerrar Sesión" al fondo ───────────────────────────
        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            color     = Color.White.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier  = Modifier.padding(horizontal = 22.dp)
        )

        // ── Cerrar Sesión ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // TODO (BACKEND): adminViewModel.logout()
                    onLogout()
                }
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.ExitToApp,
                contentDescription = "Cerrar sesión",
                tint               = Color.White.copy(alpha = 0.55f),
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text       = "Cerrar Sesión",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  ITEM DEL MENÚ
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun DrawerMenuItem(
    section: AdminSection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor   = if (isSelected) LpzRed               else Color.Transparent
    val textColor = if (isSelected) Color.White           else Color.White.copy(alpha = 0.70f)
    val iconTint  = if (isSelected) Color.White           else Color.White.copy(alpha = 0.55f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = section.icon,
            contentDescription = section.label,
            tint               = iconTint,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text       = section.label,
            fontSize   = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = textColor
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PANTALLA HOST DEL ADMIN (ModalNavigationDrawer)
//  Este es el Composable raíz que envuelve toda la zona admin.
//  Maneja el drawer + la navegación entre secciones.
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AdminHostScreen(
    onLogout: () -> Unit = {}
    // TODO (BACKEND): Inyectar AdminHostViewModel = viewModel()
) {
    val drawerState     = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope           = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(AdminSection.RESUMEN) }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            AdminDrawerContent(
                selectedSection    = selectedSection,
                onSectionSelected  = { section ->
                    selectedSection = section
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.50f)
    ) {
        // ── Contenido según la sección activa ──────────────────────────
        when (selectedSection) {
            AdminSection.RESUMEN   -> AdminDashboardScreen(
                onOpenDrawer = {
                    scope.launch { drawerState.open() }
                },
                onNavigateToAllOrders = {
                    selectedSection = AdminSection.ENVIOS
                }
            )
            
            AdminSection.PRODUCTOS -> ProductControlScreen(
                onNavigateBack = {
                    scope.launch { drawerState.open() }
                }
            )
            AdminSection.ENVIOS    -> OrderControlScreen(
                onOpenDrawer = {
                    scope.launch { drawerState.open() }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PLACEHOLDERS de secciones aún no implementadas
//  TODO (BACKEND): Reemplazar con AdminResumenScreen y AdminEnviosScreen reales
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AdminResumenPlaceholder(onOpenDrawer: () -> Unit) {
    Scaffold(
        topBar         = { AdminSectionTopBar(title = "RESUMEN", onOpenDrawer = onOpenDrawer) },
        containerColor = LpzBeige
    ) { padding ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text     = "Sección Resumen\n— próximamente —",
                color    = LpzDark.copy(alpha = 0.40f),
                fontSize = 15.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun AdminEnviosPlaceholder(onOpenDrawer: () -> Unit) {
    Scaffold(
        topBar         = { AdminSectionTopBar(title = "ENVÍOS Y PEDIDOS", onOpenDrawer = onOpenDrawer) },
        containerColor = LpzBeige
    ) { padding ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text     = "Sección Envíos y Pedidos\n— próximamente —",
                color    = LpzDark.copy(alpha = 0.40f),
                fontSize = 15.sp,
                lineHeight = 24.sp
            )
        }
    }
}

// ── TopBar reutilizable para las secciones del admin ──────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSectionTopBar(title: String, onOpenDrawer: () -> Unit) {
    Column {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector        = Icons.Default.Menu,
                        contentDescription = "Abrir menú",
                        tint               = LpzDark
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "LPZ RECORDS",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LpzDark
                    )
                    Text(
                        text          = "ADMIN",
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = LpzRed,
                        letterSpacing = 1.sp
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LpzDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        // TODO (BACKEND): Mostrar iniciales reales del admin autenticado
                        text       = "AD",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = LpzBeige)
        )
        HorizontalDivider(color = LpzDark.copy(alpha = 0.15f), thickness = 1.dp)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  PREVIEWS
// ═══════════════════════════════════════════════════════════════════════════

// ① Drawer solo (abierto)
@Preview(showBackground = true, showSystemUi = true, name = "① Drawer abierto")
@Composable
fun PreviewAdminDrawerOpen() {
    AdminDrawerContent(
        selectedSection   = AdminSection.RESUMEN,
        onSectionSelected = {},
        onLogout          = {}
    )
}

// ② Drawer con "Productos" seleccionado
@Preview(showBackground = true, showSystemUi = true, name = "② Drawer — Productos activo")
@Composable
fun PreviewAdminDrawerProductos() {
    AdminDrawerContent(
        selectedSection   = AdminSection.PRODUCTOS,
        onSectionSelected = {},
        onLogout          = {}
    )
}

// ③ Drawer con "Envíos y Pedidos" seleccionado
@Preview(showBackground = true, showSystemUi = true, name = "③ Drawer — Envíos activo")
@Composable
fun PreviewAdminDrawerEnvios() {
    AdminDrawerContent(
        selectedSection   = AdminSection.ENVIOS,
        onSectionSelected = {},
        onLogout          = {}
    )
}

// ④ Pantalla host completa (drawer cerrado por defecto)
@Preview(showBackground = true, showSystemUi = true, name = "④ AdminHostScreen completa")
@Composable
fun PreviewAdminHostScreen() {
    AdminHostScreen()
}
