package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.cache.AgendaStore
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.remote.AgendaRemoteDataSource
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.AgendaSessionsSnapshot
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotsSnapshot
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import com.cocido.oficios.domain.repository.AgendaRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

@Singleton
class AgendaRepositoryImpl @Inject constructor(
    private val remoteDataSource: AgendaRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val store: AgendaStore,
) : AgendaRepository {

    private val agendaChangedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val agendaChanged: SharedFlow<Unit> = agendaChangedEvents.asSharedFlow()
    override val slots: StateFlow<AgendaSlotsSnapshot?> = store.slots.state
    override val sessions: StateFlow<AgendaSessionsSnapshot?> = store.sessions.state

    override suspend fun getAvailableSlots(
        subRubroId: Long,
        fromIso: String,
        toIso: String,
    ): AppResult<List<AgendaSlot>> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getSlotsDisponibles(subRubroId, fromIso, toIso).map { slots ->
                slots.map { it.toDomain() }
            }
        }

    override suspend fun getAvailableSlotsForDate(
        subRubroId: Long,
        date: String,
    ): AppResult<List<AgendaSlot>> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getSlotsDisponibles(subRubroId, date, date, date).map { slots ->
                slots.map { it.toDomain() }
            }
        }

    override suspend fun getSlotsForSubRubro(
        subRubroId: Long,
        fromIso: String?,
        toIso: String?,
    ): AppResult<List<AgendaSlot>> =
        withContext(dispatcherProvider.io) {
            fetchSlots(subRubroId, fromIso, toIso)
        }

    override suspend fun updateSlot(params: UpdateAgendaSlotParams): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.updateSlot(
                id = params.slotId,
                subRubroId = params.subRubroId,
                inicio = params.startIso,
                fin = params.endIso,
                autoAceptacion = params.autoAcceptance,
            ).map { }.refreshCachedAgenda()
        }

    override suspend fun createSlot(params: CreateAgendaSlotParams): AppResult<Long> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.createSlot(
                subRubroId = params.subRubroId,
                inicio = params.startIso,
                fin = params.endIso,
                autoAceptacion = params.autoAcceptance,
            ).map { it.id }.refreshCachedAgenda()
        }

    override suspend fun deleteSlot(slotId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            val previous = store.slots.get()
            store.slots.update { snap -> snap.copy(slots = snap.slots.filterNot { it.id == slotId }) }
            when (val result = remoteDataSource.deleteSlot(slotId).map { }) {
                is AppResult.Success -> result.refreshCachedAgenda()
                is AppResult.Error -> {
                    store.slots.set(previous)
                    result
                }
            }
        }

    override suspend fun getSessionsForSubRubro(subRubroId: Long): AppResult<List<AgendaSessionConfig>> =
        withContext(dispatcherProvider.io) {
            fetchSessions(subRubroId)
        }

    override suspend fun saveSession(params: SaveAgendaSessionParams): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.createSesion(
                subRubroId = params.subRubroId,
                duracion = params.durationMinutes,
                medidaDuracion = params.durationUnit,
                intervalo = params.intervalMinutes?.toString(),
                medidaIntervalo = params.intervalMinutes?.let { params.intervalUnit ?: "minutos" },
            ).map { }.refreshCachedAgenda()
        }

    override suspend fun deleteSession(sessionId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            val previous = store.sessions.get()
            store.sessions.update { snap ->
                snap.copy(sessions = snap.sessions.filterNot { it.id == sessionId })
            }
            when (val result = remoteDataSource.deleteSesion(sessionId).map { }) {
                is AppResult.Success -> result.refreshCachedAgenda()
                is AppResult.Error -> {
                    store.sessions.set(previous)
                    result
                }
            }
        }

    private suspend fun fetchSlots(
        subRubroId: Long,
        fromIso: String?,
        toIso: String?,
    ): AppResult<List<AgendaSlot>> =
        remoteDataSource.searchSlots(subRubroId, fromIso, toIso).map { response ->
            response.data.flatMap { grupo -> grupo.slotsTurnos.map { it.toDomain() } }
        }.also { result ->
            if (result is AppResult.Success) {
                store.slots.set(
                    AgendaSlotsSnapshot(
                        subRubroId = subRubroId,
                        fromIso = fromIso,
                        toIso = toIso,
                        slots = result.data,
                    ),
                )
            }
        }

    private suspend fun fetchSessions(subRubroId: Long): AppResult<List<AgendaSessionConfig>> =
        remoteDataSource.getSesionesPorSubRubro(subRubroId).map { list -> list.map { it.toDomain() } }
            .also { result ->
                if (result is AppResult.Success) {
                    store.sessions.set(
                        AgendaSessionsSnapshot(subRubroId = subRubroId, sessions = result.data),
                    )
                }
            }

    private suspend fun <T> AppResult<T>.refreshCachedAgenda(): AppResult<T> {
        if (this is AppResult.Success) {
            store.slots.get()?.let { snap -> fetchSlots(snap.subRubroId, snap.fromIso, snap.toIso) }
            store.sessions.get()?.let { snap -> fetchSessions(snap.subRubroId) }
            agendaChangedEvents.tryEmit(Unit)
        }
        return this
    }
}
