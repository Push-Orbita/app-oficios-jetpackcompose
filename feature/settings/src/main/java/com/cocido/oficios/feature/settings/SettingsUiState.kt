package com.cocido.oficios.feature.settings

import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profile: AccountProfile? = null,
    val isLoggingOut: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
)
