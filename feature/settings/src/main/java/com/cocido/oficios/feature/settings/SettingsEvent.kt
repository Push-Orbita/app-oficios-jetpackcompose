package com.cocido.oficios.feature.settings

import com.cocido.oficios.domain.model.ThemeMode

sealed interface SettingsEvent {
    data class ThemeModeChanged(val themeMode: ThemeMode) : SettingsEvent
    data object NavigateBack : SettingsEvent
}
