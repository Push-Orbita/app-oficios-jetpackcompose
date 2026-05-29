package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.HomeContent

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val content: HomeContent) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
