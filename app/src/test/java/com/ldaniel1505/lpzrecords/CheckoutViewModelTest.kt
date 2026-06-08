package com.ldaniel1505.lpzrecords

import com.ldaniel1505.lpzrecords.viewmodel.checkout.CheckoutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CheckoutViewModelTest {

    @Test
    fun invalidCvvIsRejectedBeforeSubmittingOrder() {
        val viewModel = CheckoutViewModel()

        viewModel.confirmarCompra("12")

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("Ingresa un CVV válido de 3 dígitos.", viewModel.uiState.value.errorMessage)
    }
}
