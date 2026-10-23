package com.cocido.oficios.feature.settings.configuration

import com.cocido.oficios.domain.model.ThemeMode

data class ConfigurationUiState(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
)

sealed interface ConfigurationEvent {
    data class SetThemeMode(val mode: ThemeMode) : ConfigurationEvent
    data object NavigateBack : ConfigurationEvent
}
