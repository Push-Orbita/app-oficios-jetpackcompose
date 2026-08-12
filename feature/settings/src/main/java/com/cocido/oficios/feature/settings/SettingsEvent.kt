package com.cocido.oficios.feature.settings

sealed interface SettingsEvent {
    data object Logout : SettingsEvent
    data object NavigateBack : SettingsEvent
}
