package com.ldaniel1505.lpzrecords.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed

enum class BottomNavTab {
    HOME,
    SEARCH,
    CART,
    FAVORITES,
    ORDERS
}

@Composable
fun LpzBottomNavBar(
    selectedTab: BottomNavTab?,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit
) {
    Column{
        HorizontalDivider(color = LpzDark, thickness = 1.dp)
        Surface(
            color = LpzBeige,
            shadowElevation = 12.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(90.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LpzNavItem(
                    icon       = Icons.Default.Home,
                    label      = "Inicio",
                    isSelected = selectedTab == BottomNavTab.HOME,
                    onClick    = onHome
                )
                LpzNavItem(
                    icon       = Icons.Default.Search,
                    label      = "Buscar",
                    isSelected = selectedTab == BottomNavTab.SEARCH,
                    onClick    = onSearch
                )

                // ── Botón central del carrito (siempre en LpzRed) ─────────
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(LpzRed)
                        .clickable(onClick = onCart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.ShoppingCart,
                        contentDescription = "Carrito de compras",
                        tint               = Color.White,
                        modifier           = Modifier.size(24.dp)
                    )
                }

                LpzNavItem(
                    icon       = Icons.Default.FavoriteBorder,
                    label      = "Favoritos",
                    isSelected = selectedTab == BottomNavTab.FAVORITES,
                    onClick    = onFavorites
                )
                LpzNavItem(
                    icon       = Icons.AutoMirrored.Filled.List,
                    label      = "Compras",
                    isSelected = selectedTab == BottomNavTab.ORDERS,
                    onClick    = onProfile
                )
            }
        }
    }

}

@Composable
private fun LpzNavItem(
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
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = tint,
            modifier           = Modifier.size(22.dp)
        )
        Text(
            text       = label,
            fontSize   = 10.sp,
            color      = tint,
            fontWeight = FontWeight.Medium
        )
    }
}
