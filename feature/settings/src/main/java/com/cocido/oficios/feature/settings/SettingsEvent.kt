package com.cocido.oficios.feature.settings

sealed interface SettingsEvent {
    data object Retry : SettingsEvent
    data object OpenPersonalData : SettingsEvent
    data object OpenClientData : SettingsEvent
    data object OpenProfessionalData : SettingsEvent
    data object OpenServices : SettingsEvent
    data object OpenPortfolio : SettingsEvent
    data object OpenChangePassword : SettingsEvent
    data object OpenDeleteAccount : SettingsEvent
    data object OpenTerms : SettingsEvent
    data object Logout : SettingsEvent
    data object NavigateBack : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateToPersonalData : SettingsEffect
    data object NavigateToClientData : SettingsEffect
    data object NavigateToProfessionalData : SettingsEffect
    data object NavigateToServices : SettingsEffect
    data object NavigateToPortfolio : SettingsEffect
    data object NavigateToChangePassword : SettingsEffect
    data object NavigateToDeleteAccount : SettingsEffect
    data object NavigateToTerms : SettingsEffect
}
