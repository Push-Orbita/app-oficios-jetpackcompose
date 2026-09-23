package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toFavoritePage
import com.cocido.oficios.core.data.remote.FavoritesRemoteDataSource
import com.cocido.oficios.domain.repository.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val remoteDataSource: FavoritesRemoteDataSource,
) : FavoritesRepository {

    override suspend fun getFavorites(pageNumber: Int, pageSize: Int) =
        remoteDataSource.getFavorites(pageNumber, pageSize).map { it.toFavoritePage() }

    override suspend fun getStatus(workerId: Long) =
        remoteDataSource.getStatus(workerId).map { it.toDomain() }

    override suspend fun add(workerId: Long, idempotencyKey: String) =
        remoteDataSource.add(workerId, idempotencyKey).map { }

    override suspend fun remove(workerId: Long) =
        remoteDataSource.remove(workerId).map { }
}
