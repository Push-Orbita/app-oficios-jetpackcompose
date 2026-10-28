package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.availability.AvailabilityApi
import com.cocido.oficios.core.network.api.availability.dto.CrearBloqueoAgendaDto
import com.cocido.oficios.core.network.api.availability.dto.ReemplazarDisponibilidadDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityRemoteDataSource @Inject constructor(
    private val api: AvailabilityApi,
) {
    suspend fun getWeekly(subRubroId: Long) = safeApiCall {
        api.getWeeklyAvailability(subRubroId)
    }

    suspend fun replaceWeekly(subRubroId: Long, request: ReemplazarDisponibilidadDto) = safeApiCall {
        api.replaceWeeklyAvailability(subRubroId, request)
    }

    suspend fun listBlocks(subRubroId: Long) = safeApiCall {
        api.listBlocks(subRubroId)
    }

    suspend fun createBlock(subRubroId: Long, request: CrearBloqueoAgendaDto) = safeApiCall {
        api.createBlock(subRubroId, request)
    }

    suspend fun deleteBlock(subRubroId: Long, blockId: Long) = safeApiCall {
        api.deleteBlock(subRubroId, blockId)
    }

    suspend fun getDayAvailability(workerId: Long, date: String, subRubroId: Long) = safeApiCall {
        api.getDayAvailability(workerId, date, subRubroId)
    }
}
