package com.cocido.oficios.core.network.api.agenda

import com.cocido.oficios.core.network.api.agenda.dto.CreateSesionRequestDto
import com.cocido.oficios.core.network.api.agenda.dto.CreateSlotTurnoRequestDto
import com.cocido.oficios.core.network.api.agenda.dto.SesionDto
import com.cocido.oficios.core.network.api.agenda.dto.SlotTurnoAgendaDto
import com.cocido.oficios.core.network.api.agenda.dto.SlotsAgrupadoPorTrabajadorDto
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AgendaApi {

    @GET("agenda/slot/search")
    suspend fun searchSlots(
        @Query("trabajadorOficioSubRubroId") subRubroId: Long,
        @Query("pageSize") pageSize: Int = 100,
    ): PaginatedResponseDto<SlotsAgrupadoPorTrabajadorDto>

    @POST("agenda/slot")
    suspend fun createSlot(@Body request: CreateSlotTurnoRequestDto): SlotTurnoAgendaDto

    @DELETE("agenda/slot/{id}")
    suspend fun deleteSlot(@Path("id") id: Long): JsonElement

    @GET("sesion/por-subrubro/{trabajadorOficioSubRubroId}")
    suspend fun getSesionesPorSubRubro(
        @Path("trabajadorOficioSubRubroId") subRubroId: Long,
    ): List<SesionDto>

    @POST("sesion")
    suspend fun createSesion(@Body request: CreateSesionRequestDto): SesionDto

    @DELETE("sesion/{id}")
    suspend fun deleteSesion(@Path("id") id: Long): JsonElement
}
