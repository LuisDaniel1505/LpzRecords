package com.ldaniel1505.lpzrecords.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ldaniel1505.lpzrecords.ui.theme.LpzBeige
import com.ldaniel1505.lpzrecords.ui.theme.LpzDark
import com.ldaniel1505.lpzrecords.ui.theme.LpzRed

@Composable
fun SessionBootstrapScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = LpzRed)
        } else {
            Text(
                text = errorMessage ?: "No se pudo restaurar la sesion.",
                color = LpzDark,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = LpzRed),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(text = "REINTENTAR", color = Color.White)
            }
            Button(
                onClick = onGoToLogin,
                colors = ButtonDefaults.buttonColors(containerColor = LpzBeige),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "IR A INICIAR SESION", color = LpzRed)
            }
        }
    }
}
