package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val content: ClientHomeContent,
        val isRefreshing: Boolean = false,
    ) : HomeUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : HomeUiState
}

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data class Success(
        val categories: List<HomeCategoryItem>,
        val isRefreshing: Boolean = false,
    ) : CategoriesUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : CategoriesUiState
}
