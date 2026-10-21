package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.domain.model.ProviderHomeContent
import java.time.ZonedDateTime

sealed interface ProviderHomeUiState {
    data object Loading : ProviderHomeUiState
    data class Success(
        val content: ProviderHomeContent,
        val now: ZonedDateTime,
        val isRefreshing: Boolean = false,
    ) : ProviderHomeUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : ProviderHomeUiState
}
