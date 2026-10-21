package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.cache.CachedValue
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toFavoritePage
import com.cocido.oficios.core.data.remote.FavoritesRemoteDataSource
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.repository.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val remoteDataSource: FavoritesRemoteDataSource,
    coordinator: SessionCacheCoordinator,
) : FavoritesRepository {

    private val _favoritesChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val favoritesChanged: SharedFlow<Unit> = _favoritesChanged.asSharedFlow()

    private val favoritesCache = CachedValue<FavoritePage>(coordinator)
    override val favorites: StateFlow<FavoritePage?> = favoritesCache.state

    override suspend fun getFavorites(pageNumber: Int, pageSize: Int) =
        remoteDataSource.getFavorites(pageNumber, pageSize).map { it.toFavoritePage() }
            .also { result ->
                if (result is AppResult.Success && pageNumber == 1) {
                    favoritesCache.set(result.data)
                }
            }

    override suspend fun getStatus(workerId: Long) =
        remoteDataSource.getStatus(workerId).map { it.toDomain() }

    override suspend fun add(workerId: Long, idempotencyKey: String): AppResult<Unit> =
        when (val result = remoteDataSource.add(workerId, idempotencyKey)) {
            is AppResult.Success -> {
                val worker = result.data.toDomain()
                favoritesCache.update { page ->
                    page.copy(items = listOf(worker) + page.items.filterNot { it.workerId == workerId })
                }
                refreshFirstPageIfCached()
                notifyChanged()
                AppResult.Success(Unit)
            }
            is AppResult.Error -> result
        }

    override suspend fun remove(workerId: Long): AppResult<Unit> {
        val previous = favoritesCache.get()
        previous?.let { page ->
            favoritesCache.set(page.copy(items = page.items.filterNot { it.workerId == workerId }))
        }
        return when (val result = remoteDataSource.remove(workerId).map { }) {
            is AppResult.Success -> {
                refreshFirstPageIfCached()
                notifyChanged()
                result
            }
            is AppResult.Error -> {
                favoritesCache.set(previous)
                result
            }
        }
    }

    private suspend fun refreshFirstPageIfCached() {
        if (favoritesCache.get() == null) return
        getFavorites(pageNumber = 1, pageSize = 20)
    }

    private fun notifyChanged() {
        _favoritesChanged.tryEmit(Unit)
    }
}
