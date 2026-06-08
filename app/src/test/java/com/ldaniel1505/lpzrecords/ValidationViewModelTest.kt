package com.ldaniel1505.lpzrecords

import com.ldaniel1505.lpzrecords.data.model.Product
import com.ldaniel1505.lpzrecords.viewmodel.account.AddressViewModel
import com.ldaniel1505.lpzrecords.viewmodel.account.PaymentMethodsViewModel
import com.ldaniel1505.lpzrecords.viewmodel.auth.AuthViewModel
import com.ldaniel1505.lpzrecords.viewmodel.catalogAdmin.ProductControlViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidationViewModelTest {

    @Test
    fun authRejectsInvalidRegistrationBeforeNetworkCall() {
        val viewModel = AuthViewModel()

        viewModel.signUpUser(name = "D", email = "correo-invalido", password = "123")

        assertEquals("El nombre debe tener entre 2 y 80 caracteres.", viewModel.errorMessage)
    }

    @Test
    fun addressRejectsInvalidPostalCodeBeforeNetworkCall() {
        val viewModel = AddressViewModel()

        viewModel.createAddress(
            street = "Calle 1",
            city = "La Paz",
            state = "BCS",
            postalCode = "2300"
        )

        assertEquals(
            "El código postal debe tener exactamente 5 dígitos.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun paymentMethodRejectsInvalidLuhnNumberBeforeNetworkCall() {
        val viewModel = PaymentMethodsViewModel()

        viewModel.createPaymentMethod(
            cardNumber = "4111111111111112",
            cvv = "123",
            expiryDate = "12/30",
            cardHolder = "Daniel Lopez",
            postalCode = "23000"
        )

        assertEquals("El número de tarjeta no es válido.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun productRejectsInvalidUrlBeforeNetworkCall() {
        val viewModel = ProductControlViewModel()

        viewModel.insertProduct(
            Product(
                id = "product-1",
                title = "Disco",
                description = "",
                price = 100.0,
                stock = 1,
                img_url = "imagen-sin-protocolo",
                release_date = "",
                active = true,
                created_at = "",
                fkCategory = 1,
                fkArtist = 1,
                unitCost = 60.0
            )
        )

        assertEquals(
            "La URL de la imagen debe comenzar con http:// o https://.",
            viewModel.errorMessage
        )
    }
}
