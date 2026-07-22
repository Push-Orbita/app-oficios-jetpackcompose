package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.domain.model.ProviderHomeContent

sealed interface ProviderHomeUiState {
    data object Loading : ProviderHomeUiState
    data class Success(val content: ProviderHomeContent) : ProviderHomeUiState
    data class Error(val message: String) : ProviderHomeUiState
}
