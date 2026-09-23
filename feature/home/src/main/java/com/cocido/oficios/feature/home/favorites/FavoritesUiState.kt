package com.cocido.oficios.feature.home.favorites

import com.cocido.oficios.domain.model.FavoriteWorker

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<FavoriteWorker> = emptyList(),
    val errorMessage: String? = null,
    val hasMore: Boolean = false,
    val pageNumber: Int = 1,
)

sealed interface FavoritesEvent {
    data object Retry : FavoritesEvent
    data object LoadMore : FavoritesEvent
    data class HireClicked(val workerId: Long) : FavoritesEvent
    data class ProfileClicked(val workerId: Long) : FavoritesEvent
}

sealed interface FavoritesEffect {
    data class NavigateToWorker(val workerId: Long) : FavoritesEffect
    data class NavigateToHire(val workerId: Long) : FavoritesEffect
}
