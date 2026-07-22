package com.cocido.oficios.feature.onboarding.navigation

object OnboardingDeepLinkMapper {

    fun toInternalRoute(deepLink: String?): String? = when (deepLink?.trim()?.removePrefix("/")) {
        "onboarding/identidad/dni" -> OnboardingDestination.PROVIDER_DNI
        "onboarding/identidad/selfie" -> OnboardingDestination.PROVIDER_SELFIE
        "onboarding/identidad" -> OnboardingDestination.PROVIDER_CHECKLIST
        "onboarding" -> OnboardingDestination.PROVIDER_CHECKLIST
        else -> null
    }
}
