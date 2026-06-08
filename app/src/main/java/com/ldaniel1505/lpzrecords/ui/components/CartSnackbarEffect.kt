package com.ldaniel1505.lpzrecords.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartUiEvent
import com.ldaniel1505.lpzrecords.viewmodel.cart.CartViewModel

@Composable
fun CartSnackbarEffect(
    cartViewModel: CartViewModel,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(cartViewModel, snackbarHostState) {
        cartViewModel.events.collect { event ->
            if (event is CartUiEvent.Message) {
                snackbarHostState.showSnackbar(event.text)
            }
        }
    }
}
