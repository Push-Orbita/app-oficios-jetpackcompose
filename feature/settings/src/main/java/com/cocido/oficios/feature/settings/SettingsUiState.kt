package com.cocido.oficios.feature.settings

import com.cocido.oficios.domain.model.ThemeMode

data class SettingsUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
)
