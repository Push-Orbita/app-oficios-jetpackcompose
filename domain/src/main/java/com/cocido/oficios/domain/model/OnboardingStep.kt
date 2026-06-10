package com.cocido.oficios.domain.model

enum class OnboardingStepStatus {
    PENDING,
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
    val accountStatus: AccountStatus,
)

data class ProviderProfileStatus(
    val accountStatus: AccountStatus,
    val isConfigurationComplete: Boolean,
    val isProfileComplete: Boolean,
    val isOperational: Boolean,
    val missingSteps: List<String>,
    val rejectionReason: String? = null,
)
