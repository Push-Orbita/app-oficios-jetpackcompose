package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.remote.FavoritesRemoteDataSource
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.catalog.dto.PaginationMetadataDto
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
        val repository = FavoritesRepositoryImpl(FavoritesRemoteDataSource(api), SessionCacheCoordinator())
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

    @Test
    fun `toggle favorite emits the updated first page`() = runTest {
        val api = FakeFavoritesApi().apply { fail = false }
        api.items += FavoritoItemDto(id = 1, trabajadorOficioId = 5L, displayName = "Ana")
        val repository = FavoritesRepositoryImpl(FavoritesRemoteDataSource(api), SessionCacheCoordinator())
        assertTrue(repository.getFavorites(1, 20) is AppResult.Success)
        assertEquals(listOf(5L), repository.favorites.value?.items?.map { it.workerId })

        assertTrue(repository.remove(5L) is AppResult.Success)
        assertEquals(emptyList<Long>(), repository.favorites.value?.items?.map { it.workerId }.orEmpty())

        assertTrue(repository.add(8L, "key") is AppResult.Success)
        assertEquals(listOf(8L), repository.favorites.value?.items?.map { it.workerId })
    }
}

private class FakeFavoritesApi : FavoritesApi {
    var fail: Boolean = true
    val items = mutableListOf<FavoritoItemDto>()

    override suspend fun getFavorites(pageNumber: Int, pageSize: Int): FavoritosPageDto =
        PaginatedResponseDto(
            data = items.toList(),
            metadata = PaginationMetadataDto(pageNumber = pageNumber, totalPages = 1),
        )

    override suspend fun getStatus(trabajadorOficioId: Long): FavoritoStatusDto = error("unused")

    override suspend fun add(trabajadorOficioId: Long, idempotencyKey: String): FavoritoItemDto {
        if (fail) error("network")
        val item = FavoritoItemDto(id = trabajadorOficioId, trabajadorOficioId = trabajadorOficioId, displayName = "Ana")
        items.removeAll { it.trabajadorOficioId == trabajadorOficioId }
        items.add(0, item)
        return item
    }

    override suspend fun remove(trabajadorOficioId: Long) {
        if (fail) error("network")
        items.removeAll { it.trabajadorOficioId == trabajadorOficioId }
    }
}
