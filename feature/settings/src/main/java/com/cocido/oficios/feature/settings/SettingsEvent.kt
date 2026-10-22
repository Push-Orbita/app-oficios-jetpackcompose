package com.cocido.oficios.feature.settings

import com.cocido.oficios.domain.model.ThemeMode

sealed interface SettingsEvent {
    data object Retry : SettingsEvent
    data object OpenPersonalData : SettingsEvent
    data object OpenClientData : SettingsEvent
    data object OpenProfessionalData : SettingsEvent
    data object OpenChangePassword : SettingsEvent
    data object OpenDeleteAccount : SettingsEvent
    data object OpenTerms : SettingsEvent
    data object OpenHelp : SettingsEvent
    data object Logout : SettingsEvent
    data object NavigateBack : SettingsEvent
    data class SetThemeMode(val mode: ThemeMode) : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateToPersonalData : SettingsEffect
    data object NavigateToClientData : SettingsEffect
    data object NavigateToProfessionalData : SettingsEffect
    data object NavigateToChangePassword : SettingsEffect
    data object NavigateToDeleteAccount : SettingsEffect
    data object NavigateToTerms : SettingsEffect
    data object NavigateToHelp : SettingsEffect
}
