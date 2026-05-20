package com.ldaniel1505.lpzrecords.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldaniel1505.lpzrecords.R
import com.ldaniel1505.lpzrecords.ui.theme.*

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStore: () -> Unit
) {
    Surface(color = LpzBeige, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.vinyl),
                    contentDescription = "Logo Vinyl",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "LPZ RECORDS",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = LpzDark
            )

            Text(
                text = "Música al alcance de tu mano",
                fontSize = 18.sp,
                color = LpzDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = onNavigateToStore,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ENTRAR A LA TIENDA", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = BorderStroke(2.dp, LpzRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("INICIAR SESIÓN", color = LpzRed, fontSize = 18.sp)
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        onNavigateToLogin = {},
        onNavigateToStore = {}
    )
}