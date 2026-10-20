package com.cocido.oficios.feature.settings

import com.cocido.oficios.domain.model.AccountProfile

data class SettingsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profile: AccountProfile? = null,
    val isLoggingOut: Boolean = false,
)
