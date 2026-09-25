package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toFavoritePage
import com.cocido.oficios.core.data.remote.FavoritesRemoteDataSource
import com.cocido.oficios.domain.repository.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val remoteDataSource: FavoritesRemoteDataSource,
) : FavoritesRepository {

    private val _favoritesChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val favoritesChanged: SharedFlow<Unit> = _favoritesChanged.asSharedFlow()

    override suspend fun getFavorites(pageNumber: Int, pageSize: Int) =
        remoteDataSource.getFavorites(pageNumber, pageSize).map { it.toFavoritePage() }

    override suspend fun getStatus(workerId: Long) =
        remoteDataSource.getStatus(workerId).map { it.toDomain() }

    override suspend fun add(workerId: Long, idempotencyKey: String): AppResult<Unit> =
        remoteDataSource.add(workerId, idempotencyKey).map { }.also(::notifyIfSuccess)

    override suspend fun remove(workerId: Long): AppResult<Unit> =
        remoteDataSource.remove(workerId).map { }.also(::notifyIfSuccess)

    private fun notifyIfSuccess(result: AppResult<Unit>) {
        if (result is AppResult.Success) _favoritesChanged.tryEmit(Unit)
    }
}
