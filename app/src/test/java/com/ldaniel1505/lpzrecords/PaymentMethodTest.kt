package com.ldaniel1505.lpzrecords

import com.ldaniel1505.lpzrecords.data.model.PaymentMethod
import com.ldaniel1505.lpzrecords.data.model.cvvToken
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentMethodTest {

    @Test
    fun cvvTokenOnlyMatchesOriginalCvv() {
        val paymentMethod = PaymentMethod(
            idMethod = "payment-1",
            paymentToken = cvvToken("payment-1", "123")
        )

        assertTrue(paymentMethod.canValidateCvv)
        assertTrue(paymentMethod.matchesCvv("123"))
        assertFalse(paymentMethod.matchesCvv("999"))
    }

    @Test
    fun missingCvvTokenCannotValidateCvv() {
        val paymentMethod = PaymentMethod(idMethod = "payment-1")

        assertFalse(paymentMethod.canValidateCvv)
        assertFalse(paymentMethod.matchesCvv("123"))
    }
}
