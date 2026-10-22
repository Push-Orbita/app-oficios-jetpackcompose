package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.cache.AgendaStore
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.remote.AgendaRemoteDataSource
import com.cocido.oficios.core.network.api.agenda.AgendaApi
import com.cocido.oficios.core.network.api.agenda.dto.CreateSesionRequestDto
import com.cocido.oficios.core.network.api.agenda.dto.CreateSlotTurnoRequestDto
import com.cocido.oficios.core.network.api.agenda.dto.SesionDto
import com.cocido.oficios.core.network.api.agenda.dto.SlotTurnoAgendaDto
import com.cocido.oficios.core.network.api.agenda.dto.SlotsAgrupadoPorTrabajadorDto
import com.cocido.oficios.core.network.api.agenda.dto.UpdateSlotTurnoRequestDto
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaRepositoryCacheTest {

    @Test
    fun `get slots caches the month and delete updates it`() = runTest {
        val api = FakeAgendaApi()
        val coordinator = SessionCacheCoordinator()
        val store = AgendaStore(coordinator)
        val repository = repository(api, store)

        val loaded = repository.getSlotsForSubRubro(10L, "from", "to")
        assertTrue(loaded is AppResult.Success)
        assertEquals(listOf(1L, 2L), store.slots.get()?.slots?.map { it.id })

        val deleted = repository.deleteSlot(1L)
        assertTrue(deleted is AppResult.Success)
        assertEquals(listOf(2L), store.slots.get()?.slots?.map { it.id })
        assertEquals(1, api.deleteSlotCalls)
    }

    @Test
    fun `failed delete restores the previous slots`() = runTest {
        val api = FakeAgendaApi(failDelete = true)
        val coordinator = SessionCacheCoordinator()
        val store = AgendaStore(coordinator)
        val repository = repository(api, store)

        repository.getSlotsForSubRubro(10L, "from", "to")
        val failed = repository.deleteSlot(1L)
        assertTrue(failed is AppResult.Error)
        assertEquals(listOf(1L, 2L), store.slots.get()?.slots?.map { it.id })
    }

    @Test
    fun `logout clears agenda cache`() = runTest {
        val coordinator = SessionCacheCoordinator()
        val store = AgendaStore(coordinator)
        val repository = repository(FakeAgendaApi(), store)
        repository.getSlotsForSubRubro(10L, "from", "to")
        repository.getSessionsForSubRubro(10L)
        assertEquals(2, store.slots.get()?.slots?.size)
        assertEquals(1, store.sessions.get()?.sessions?.size)

        coordinator.clearAll()
        assertNull(store.slots.get())
        assertNull(store.sessions.get())
    }

    @Test
    fun `save session posts duration interval and units from OpenAPI`() = runTest {
        val api = FakeAgendaApi()
        val repository = repository(api, AgendaStore(SessionCacheCoordinator()))
        val result = repository.saveSession(
            SaveAgendaSessionParams(
                subRubroId = 10L,
                durationMinutes = 2,
                intervalMinutes = 15,
                durationUnit = "horas",
                intervalUnit = "minutos",
            ),
        )
        assertTrue(result is AppResult.Success)
        val body = api.lastCreateSesion
        assertEquals(10L, body?.trabajadorOficioSubRubroId)
        assertEquals(2, body?.duracion)
        assertEquals("horas", body?.medidaDuracion)
        assertEquals("15", body?.intervalo)
        assertEquals("minutos", body?.medidaIntervalo)
    }

    private fun repository(api: AgendaApi, store: AgendaStore) = AgendaRepositoryImpl(
        remoteDataSource = AgendaRemoteDataSource(api),
        dispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
            override val io: CoroutineDispatcher = Dispatchers.Unconfined
            override val default: CoroutineDispatcher = Dispatchers.Unconfined
        },
        store = store,
    )
}

private class FakeAgendaApi(
    private val failDelete: Boolean = false,
) : AgendaApi {
    var deleteSlotCalls = 0
    private var slots = listOf(slot(1L), slot(2L))
    private var sessions = listOf(
        SesionDto(
            id = 9L,
            duracion = 60,
            medidaDuracion = "minutos",
            trabajadorOficioSubRubroId = 10L,
        ),
    )

    override suspend fun getSlotsDisponibles(
        subRubroId: Long,
        fechaDesde: String,
        fechaHasta: String,
    ) = slots

    override suspend fun searchSlots(
        subRubroId: Long,
        fromIso: String?,
        toIso: String?,
        pageSize: Int,
    ) = PaginatedResponseDto(data = listOf(SlotsAgrupadoPorTrabajadorDto(slotsTurnos = slots)))

    override suspend fun createSlot(request: CreateSlotTurnoRequestDto) = slot(99L)

    override suspend fun updateSlot(id: Long, request: UpdateSlotTurnoRequestDto) = slot(id)

    override suspend fun deleteSlot(id: Long) {
        deleteSlotCalls += 1
        if (failDelete) error("delete failed")
        slots = slots.filterNot { it.id == id }
    }

    override suspend fun getSesionesPorSubRubro(subRubroId: Long) = sessions

    var lastCreateSesion: CreateSesionRequestDto? = null
    override suspend fun createSesion(request: CreateSesionRequestDto): SesionDto {
        lastCreateSesion = request
        return sessions.first()
    }

    override suspend fun deleteSesion(id: Long) {
        sessions = sessions.filterNot { it.id == id }
    }
}

private fun slot(id: Long) = SlotTurnoAgendaDto(
    id = id,
    inicio = "2026-10-25T10:00:00-03:00",
    fin = "2026-10-25T11:00:00-03:00",
    estado = "DISPONIBLE",
)
