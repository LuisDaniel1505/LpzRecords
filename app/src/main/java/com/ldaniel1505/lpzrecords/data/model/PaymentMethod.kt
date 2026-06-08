package com.ldaniel1505.lpzrecords.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class PaymentMethod(
    @SerialName("id_method")
    val idMethod: String = "",
    @SerialName("id_user")
    val userId: String = "",
    val type: String = "card",
    val last4: String? = null,
    val brand: String? = null,
    @SerialName("payment_token")
    val paymentToken: String? = null,
    @SerialName("default")
    val isDefault: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val id: String
        get() = idMethod

    val provider: String
        get() = brand ?: type.uppercase()

    val lastFourDigits: String
        get() = last4?.trim()?.takeIf { it.isNotBlank() } ?: "----"

    val cardHolder: String
        get() = "Cliente LPZ"

    val expiryDate: String
        get() = "--/--"

    val displayName: String
        get() = "$provider terminacion $lastFourDigits"

    val canValidateCvv: Boolean
        get() = paymentToken?.startsWith(CVV_TOKEN_PREFIX) == true

    fun matchesCvv(cvv: String): Boolean {
        return paymentToken == cvvToken(idMethod, cvv)
    }
}

@Serializable
data class CreatePaymentMethod(
    @SerialName("id_method")
    val idMethod: String,
    @SerialName("id_user")
    val userId: String,
    val type: String,
    val last4: String,
    val brand: String,
    @SerialName("payment_token")
    val paymentToken: String? = null,
    @SerialName("default")
    val isDefault: Boolean = false
)

private const val CVV_TOKEN_PREFIX = "cvv_sha256:"

fun cvvToken(paymentMethodId: String, cvv: String): String {
    val cleanCvv = cvv.trim()
    val source = "$paymentMethodId:$cleanCvv"
    val digest = MessageDigest
        .getInstance("SHA-256")
        .digest(source.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    return "$CVV_TOKEN_PREFIX$digest"
}
