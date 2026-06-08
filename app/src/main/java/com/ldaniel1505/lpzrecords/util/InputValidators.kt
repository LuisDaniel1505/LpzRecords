package com.ldaniel1505.lpzrecords.util

import java.math.BigDecimal
import java.net.URI
import java.util.Calendar

object InputValidators {
    const val NAME_MIN_LENGTH = 2
    const val NAME_MAX_LENGTH = 80
    const val EMAIL_MAX_LENGTH = 254
    const val PASSWORD_MIN_LENGTH = 6
    const val PHONE_LENGTH = 10
    const val POSTAL_CODE_LENGTH = 5
    const val CARD_NUMBER_LENGTH = 16
    const val CARD_CVV_LENGTH = 3
    const val CARD_HOLDER_MAX_LENGTH = 80
    const val STREET_MAX_LENGTH = 150
    const val CITY_MAX_LENGTH = 80
    const val STATE_MAX_LENGTH = 80
    const val PRODUCT_TITLE_MAX_LENGTH = 150
    const val PRODUCT_DESCRIPTION_MAX_LENGTH = 1000
    const val URL_MAX_LENGTH = 500
    const val STOCK_MAX = 99_999

    private val emailRegex = Regex(
        pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        option = RegexOption.IGNORE_CASE
    )

    fun digitsOnly(value: String, maxLength: Int): String =
        value.filter(Char::isDigit).take(maxLength)

    fun isValidName(value: String): Boolean =
        value.trim().length in NAME_MIN_LENGTH..NAME_MAX_LENGTH

    fun isValidEmail(value: String): Boolean {
        val clean = value.trim()
        return clean.length <= EMAIL_MAX_LENGTH && emailRegex.matches(clean)
    }

    fun isValidPassword(value: String): Boolean =
        value.length >= PASSWORD_MIN_LENGTH

    fun isValidOptionalPhone(value: String): Boolean {
        val clean = value.trim()
        return clean.isBlank() || (clean.length == PHONE_LENGTH && clean.all(Char::isDigit))
    }

    fun isValidPostalCode(value: String): Boolean {
        val clean = value.trim()
        return clean.length == POSTAL_CODE_LENGTH && clean.all(Char::isDigit)
    }

    fun isValidSupportedCard(value: String): Boolean {
        val digits = value.filter(Char::isDigit)
        return digits.length == CARD_NUMBER_LENGTH &&
                cardBrand(digits) != null &&
                passesLuhn(digits)
    }

    fun isValidCardCvv(value: String): Boolean {
        val clean = value.trim()
        return clean.length == CARD_CVV_LENGTH && clean.all(Char::isDigit)
    }

    fun isValidCardExpiry(value: String): Boolean {
        val clean = value.trim()
        val parts = clean.split("/")
        if (parts.size != 2) return false

        val month = parts[0].toIntOrNull() ?: return false
        val yearSuffix = parts[1].toIntOrNull() ?: return false
        if (parts[0].length != 2 || parts[1].length != 2 || month !in 1..12) return false

        val current = Calendar.getInstance()
        val currentYear = current.get(Calendar.YEAR) % 100
        val currentMonth = current.get(Calendar.MONTH) + 1

        return yearSuffix > currentYear || (yearSuffix == currentYear && month >= currentMonth)
    }

    fun isValidCardHolder(value: String): Boolean {
        val clean = value.trim()
        return clean.length in NAME_MIN_LENGTH..CARD_HOLDER_MAX_LENGTH &&
                clean.any(Char::isLetter) &&
                clean.all { it.isLetter() || it.isWhitespace() || it == '.' || it == '\'' || it == '-' }
    }

    fun cardBrand(value: String): String? {
        val digits = value.filter(Char::isDigit)
        return when {
            digits.startsWith("4") -> "VISA"
            isMastercard(digits) -> "MASTERCARD"
            else -> null
        }
    }

    fun isValidHttpUrl(value: String): Boolean {
        val clean = value.trim()
        if (clean.isBlank()) return true
        if (clean.length > URL_MAX_LENGTH) return false

        return runCatching {
            val uri = URI(clean)
            uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    fun hasAtMostTwoDecimals(value: Double): Boolean =
        value.isFinite() && BigDecimal.valueOf(value).stripTrailingZeros().scale() <= 2

    private fun isMastercard(digits: String): Boolean {
        if (digits.length < 4) return false
        val firstTwo = digits.take(2).toIntOrNull() ?: return false
        val firstFour = digits.take(4).toIntOrNull() ?: return false
        return firstTwo in 51..55 || firstFour in 2221..2720
    }

    private fun passesLuhn(digits: String): Boolean {
        var sum = 0
        val parity = digits.length % 2

        digits.forEachIndexed { index, char ->
            var value = char.digitToInt()
            if (index % 2 == parity) {
                value *= 2
                if (value > 9) value -= 9
            }
            sum += value
        }

        return sum % 10 == 0
    }
}
