package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.model.FavoriteStatus

interface FavoritesRepository {
    suspend fun getFavorites(pageNumber: Int, pageSize: Int): AppResult<FavoritePage>
    suspend fun getStatus(workerId: Long): AppResult<FavoriteStatus>
    suspend fun add(workerId: Long, idempotencyKey: String): AppResult<Unit>
    suspend fun remove(workerId: Long): AppResult<Unit>
}
