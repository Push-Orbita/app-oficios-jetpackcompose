package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.remote.AgendaRemoteDataSource
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import com.cocido.oficios.domain.repository.AgendaRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext

class AgendaRepositoryImpl @Inject constructor(
    private val remoteDataSource: AgendaRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : AgendaRepository {

    override suspend fun getSlotsForSubRubro(
        subRubroId: Long,
        fromIso: String?,
        toIso: String?,
    ): AppResult<List<AgendaSlot>> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.searchSlots(subRubroId, fromIso, toIso).map { response ->
                response.data.flatMap { grupo -> grupo.slotsTurnos.map { it.toDomain() } }
            }
        }

    override suspend fun updateSlot(params: UpdateAgendaSlotParams): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.updateSlot(
                id = params.slotId,
                subRubroId = params.subRubroId,
                inicio = params.startIso,
                fin = params.endIso,
                autoAceptacion = params.autoAcceptance,
            ).map { }
        }

    override suspend fun createSlot(params: CreateAgendaSlotParams): AppResult<Long> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.createSlot(
                subRubroId = params.subRubroId,
                inicio = params.startIso,
                fin = params.endIso,
                autoAceptacion = params.autoAcceptance,
            ).map { it.id }
        }

    override suspend fun deleteSlot(slotId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.deleteSlot(slotId).map { }
        }

    override suspend fun getSessionsForSubRubro(subRubroId: Long): AppResult<List<AgendaSessionConfig>> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getSesionesPorSubRubro(subRubroId).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun saveSession(params: SaveAgendaSessionParams): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.createSesion(
                subRubroId = params.subRubroId,
                duracion = params.durationMinutes,
                medidaDuracion = "minutos",
                intervalo = params.intervalMinutes?.toString(),
                medidaIntervalo = params.intervalMinutes?.let { "minutos" },
            ).map { }
        }

    override suspend fun deleteSession(sessionId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.deleteSesion(sessionId).map { }
        }
}
