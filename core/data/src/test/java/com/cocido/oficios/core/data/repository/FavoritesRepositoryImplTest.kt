package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.remote.FavoritesRemoteDataSource
import com.cocido.oficios.core.network.api.social.FavoritesApi
import com.cocido.oficios.core.network.api.social.dto.FavoritoItemDto
import com.cocido.oficios.core.network.api.social.dto.FavoritoStatusDto
import com.cocido.oficios.core.network.api.social.dto.FavoritosPageDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    @Test
    fun `emits change only after a successful add or remove`() = runTest {
        val api = FakeFavoritesApi()
        val repository = FavoritesRepositoryImpl(FavoritesRemoteDataSource(api))
        val events = mutableListOf<Unit>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.favoritesChanged.collect { events.add(Unit) }
        }

        val failed = repository.add(5L, "key")
        assertTrue(failed is AppResult.Error)
        assertEquals(0, events.size)

        api.fail = false
        val added = repository.add(5L, "key")
        assertTrue(added is AppResult.Success)
        val removed = repository.remove(5L)
        assertTrue(removed is AppResult.Success)
        assertEquals(2, events.size)

        job.cancel()
    }
}

private class FakeFavoritesApi : FavoritesApi {
    var fail: Boolean = true

    override suspend fun getFavorites(pageNumber: Int, pageSize: Int): FavoritosPageDto = error("unused")

    override suspend fun getStatus(trabajadorOficioId: Long): FavoritoStatusDto = error("unused")

    override suspend fun add(trabajadorOficioId: Long, idempotencyKey: String): FavoritoItemDto {
        if (fail) error("network")
        return FavoritoItemDto(id = 1, trabajadorOficioId = trabajadorOficioId, displayName = "Ana")
    }

    override suspend fun remove(trabajadorOficioId: Long) {
        if (fail) error("network")
    }
}
