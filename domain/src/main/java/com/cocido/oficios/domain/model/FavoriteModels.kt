package com.cocido.oficios.domain.model

data class FavoriteWorker(
    val favoriteId: Long,
    val workerId: Long,
    val displayName: String,
    val specialtyLabel: String,
    val rating: Float?,
    val avatarUrl: String?,
    val isVerified: Boolean,
)

data class FavoritePage(
    val items: List<FavoriteWorker>,
    val pageNumber: Int,
    val hasMore: Boolean,
)

data class FavoriteStatus(
    val isFavorite: Boolean,
    val favoriteId: Long? = null,
)
