package com.ldaniel1505.lpzrecords

import com.ldaniel1505.lpzrecords.util.InputValidators
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class InputValidatorsTest {

    @Test
    fun phoneIsOptionalOrExactlyTenDigits() {
        assertTrue(InputValidators.isValidOptionalPhone(""))
        assertTrue(InputValidators.isValidOptionalPhone("6121234567"))
        assertFalse(InputValidators.isValidOptionalPhone("612123456"))
        assertFalse(InputValidators.isValidOptionalPhone("61212345678"))
        assertFalse(InputValidators.isValidOptionalPhone("612ABC4567"))
    }

    @Test
    fun postalCodeRequiresFiveDigits() {
        assertTrue(InputValidators.isValidPostalCode("23000"))
        assertFalse(InputValidators.isValidPostalCode("2300"))
        assertFalse(InputValidators.isValidPostalCode("230000"))
        assertFalse(InputValidators.isValidPostalCode("23A00"))
    }

    @Test
    fun cardRequiresSupportedBrandAndLuhn() {
        assertTrue(InputValidators.isValidSupportedCard("4111111111111111"))
        assertTrue(InputValidators.isValidSupportedCard("5555555555554444"))
        assertFalse(InputValidators.isValidSupportedCard("4111111111111112"))
        assertFalse(InputValidators.isValidSupportedCard("378282246310005"))
    }

    @Test
    fun simulatedCardFieldsAreValidated() {
        val current = Calendar.getInstance()
        val nextYear = (current.get(Calendar.YEAR) + 1) % 100
        val validExpiry = "12/${nextYear.toString().padStart(2, '0')}"

        assertTrue(InputValidators.isValidCardCvv("123"))
        assertFalse(InputValidators.isValidCardCvv("12"))
        assertFalse(InputValidators.isValidCardCvv("12A"))

        assertTrue(InputValidators.isValidCardExpiry(validExpiry))
        assertFalse(InputValidators.isValidCardExpiry("00/30"))
        assertFalse(InputValidators.isValidCardExpiry("13/30"))
        assertFalse(InputValidators.isValidCardExpiry("01/20"))

        assertTrue(InputValidators.isValidCardHolder("Daniel Lopez"))
        assertFalse(InputValidators.isValidCardHolder("D"))
        assertFalse(InputValidators.isValidCardHolder("Daniel123"))
    }

    @Test
    fun emailNamePasswordAndUrlRulesAreApplied() {
        assertTrue(InputValidators.isValidName("Daniel"))
        assertFalse(InputValidators.isValidName("D"))
        assertTrue(InputValidators.isValidEmail("daniel@example.com"))
        assertFalse(InputValidators.isValidEmail("daniel@"))
        assertTrue(InputValidators.isValidPassword("123456"))
        assertFalse(InputValidators.isValidPassword("12345"))
        assertTrue(InputValidators.isValidHttpUrl("https://example.com/image.png"))
        assertFalse(InputValidators.isValidHttpUrl("example.com/image.png"))
    }

    @Test
    fun monetaryValuesAllowAtMostTwoDecimals() {
        assertTrue(InputValidators.hasAtMostTwoDecimals(10.0))
        assertTrue(InputValidators.hasAtMostTwoDecimals(10.25))
        assertFalse(InputValidators.hasAtMostTwoDecimals(10.257))
    }
}
