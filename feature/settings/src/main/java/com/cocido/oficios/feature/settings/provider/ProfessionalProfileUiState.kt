package com.cocido.oficios.feature.settings.provider

data class ProfessionalProfileUiState(
    val isLoading: Boolean = true,
    val description: String = "",
    val experience: String = "",
    val paymentMethods: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val loadFailed: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface ProfessionalProfileEvent {
    data object Retry : ProfessionalProfileEvent
    data class DescriptionChanged(val value: String) : ProfessionalProfileEvent
    data class ExperienceChanged(val value: String) : ProfessionalProfileEvent
    data class PaymentToggled(val method: String, val checked: Boolean) : ProfessionalProfileEvent
    data object Save : ProfessionalProfileEvent
    data object Back : ProfessionalProfileEvent
}

sealed interface ProfessionalProfileEffect {
    data object NavigateBack : ProfessionalProfileEffect
    data class Saved(val message: String) : ProfessionalProfileEffect
}
