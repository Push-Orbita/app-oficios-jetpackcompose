package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.social.FavoritesApi
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRemoteDataSource @Inject constructor(
    private val favoritesApi: FavoritesApi,
) {
    suspend fun getFavorites(pageNumber: Int, pageSize: Int) = safeApiCall {
        favoritesApi.getFavorites(pageNumber = pageNumber, pageSize = pageSize)
    }

    suspend fun getStatus(workerId: Long) = safeApiCall {
        favoritesApi.getStatus(workerId)
    }

    suspend fun add(workerId: Long, idempotencyKey: String) = safeApiCall {
        favoritesApi.add(workerId, idempotencyKey)
    }

    suspend fun remove(workerId: Long) = safeApiCall {
        favoritesApi.remove(workerId)
    }
}
