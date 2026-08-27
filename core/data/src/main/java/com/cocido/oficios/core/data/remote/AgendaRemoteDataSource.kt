package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.agenda.AgendaApi
import com.cocido.oficios.core.network.api.agenda.dto.CreateSesionRequestDto
import com.cocido.oficios.core.network.api.agenda.dto.CreateSlotTurnoRequestDto
import com.cocido.oficios.core.network.api.agenda.dto.UpdateSlotTurnoRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgendaRemoteDataSource @Inject constructor(
    private val agendaApi: AgendaApi,
) {
    suspend fun searchSlots(subRubroId: Long, fromIso: String?, toIso: String?) = safeApiCall {
        agendaApi.searchSlots(subRubroId = subRubroId, fromIso = fromIso, toIso = toIso)
    }

    suspend fun updateSlot(
        id: Long,
        subRubroId: Long,
        inicio: String,
        fin: String,
        autoAceptacion: Boolean,
    ) = safeApiCall {
        agendaApi.updateSlot(
            id = id,
            request = UpdateSlotTurnoRequestDto(
                trabajadorOficioSubRubroId = subRubroId,
                inicio = inicio,
                fin = fin,
                autoAceptacion = autoAceptacion,
            ),
        )
    }

    suspend fun createSlot(subRubroId: Long, inicio: String, fin: String, autoAceptacion: Boolean) = safeApiCall {
        agendaApi.createSlot(
            CreateSlotTurnoRequestDto(
                trabajadorOficioSubRubroId = subRubroId,
                inicio = inicio,
                fin = fin,
                autoAceptacion = autoAceptacion,
            ),
        )
    }

    suspend fun deleteSlot(id: Long) = safeApiCall {
        agendaApi.deleteSlot(id)
    }

    suspend fun getSesionesPorSubRubro(subRubroId: Long) = safeApiCall {
        agendaApi.getSesionesPorSubRubro(subRubroId)
    }

    suspend fun createSesion(
        subRubroId: Long,
        duracion: Int,
        medidaDuracion: String,
        intervalo: String?,
        medidaIntervalo: String?,
    ) = safeApiCall {
        agendaApi.createSesion(
            CreateSesionRequestDto(
                trabajadorOficioSubRubroId = subRubroId,
                duracion = duracion,
                medidaDuracion = medidaDuracion,
                intervalo = intervalo,
                medidaIntervalo = medidaIntervalo,
            ),
        )
    }

    suspend fun deleteSesion(id: Long) = safeApiCall {
        agendaApi.deleteSesion(id)
    }
}
