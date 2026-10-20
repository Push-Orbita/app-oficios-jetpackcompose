package com.cocido.oficios.core.common.navigation

object PushDeepLinkMapper {

    fun toInternalRoute(deepLink: String?): String? {
        val normalized = deepLink
            ?.trim()
            ?.removePrefix("oficios://")
            ?.removePrefix("https://oficios.app/")
            ?.trimStart('/')
            .orEmpty()
        if (normalized.isBlank()) return null

        mapOnboardingDeepLink(normalized)?.let { return it }

        return when {
            normalized.startsWith("orders/rate/") -> {
                val bookingId = normalized.removePrefix("orders/rate/").substringBefore("?").toLongOrNull()
                bookingId?.let { "orders/rate/$it" }
            }
            normalized.startsWith("orders/") -> {
                val orderId = normalized.removePrefix("orders/").substringBefore("/").substringBefore("?").toLongOrNull()
                orderId?.let { "orders/$it" }
            }
            normalized == "orders/mine" -> "orders/mine"
            normalized == "notifications" -> "notifications"
            else -> null
        }
    }

    private fun mapOnboardingDeepLink(normalized: String): String? = when (normalized) {
        "onboarding/identidad/dni" -> "onboarding/provider/dni"
        "onboarding/identidad/selfie" -> "onboarding/provider/selfie"
        "onboarding/identidad", "onboarding" -> "onboarding/provider-checklist"
        else -> null
    }
}
