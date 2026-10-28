package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.model.FavoriteStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val EmptyFavoritePage: StateFlow<FavoritePage?> = MutableStateFlow<FavoritePage?>(null).asStateFlow()

interface FavoritesRepository {
    val favoritesChanged: SharedFlow<Unit>
    val favorites: StateFlow<FavoritePage?> get() = EmptyFavoritePage

    suspend fun getFavorites(pageNumber: Int, pageSize: Int): AppResult<FavoritePage>
    suspend fun getStatus(workerId: Long): AppResult<FavoriteStatus>
    suspend fun add(workerId: Long, idempotencyKey: String): AppResult<Unit>
    suspend fun remove(workerId: Long): AppResult<Unit>
}
