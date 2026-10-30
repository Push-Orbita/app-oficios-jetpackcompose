package com.cocido.oficios.domain.model

enum class OnboardingStepStatus {
    PENDING,
    IN_REVIEW,
    COMPLETE,
    BLOCKED,
    REJECTED,
}

data class OnboardingStep(
    val id: String,
    val title: String,
    val description: String,
    val status: OnboardingStepStatus,
    val isRequired: Boolean = true,
)

data class ClientProfileStatus(
    val isComplete: Boolean,
    val missingSteps: List<String>,
    val hasContact: Boolean,
    val hasAddress: Boolean,
    val emailVerified: Boolean,
    val phoneVerified: Boolean = false,
    val accountStatus: AccountStatus,
)

data class ProviderProfileStatus(
    val accountStatus: AccountStatus,
    val isConfigurationComplete: Boolean,
    val isProfileComplete: Boolean,
    val isOperational: Boolean,
    val isIdentityVerified: Boolean = false,
    val missingSteps: List<String>,
    val rejectionReason: String? = null,
    /** `requisitos.requisitosOnboardingCompletos` de `estado-perfil`. */
    val isOnboardingRequirementsComplete: Boolean = false,
) {
    /**
     * Requisitos obligatorios del onboarding cumplidos según la API: `perfilCompleto`, o
     * `requisitosOnboardingCompletos` sin `faltantes`. Galería, local y certificados son
     * opcionales y no cuentan acá.
     */
    val hasCompletedRequiredProfile: Boolean
        get() = isProfileComplete || (isOnboardingRequirementsComplete && missingSteps.isEmpty())
}
