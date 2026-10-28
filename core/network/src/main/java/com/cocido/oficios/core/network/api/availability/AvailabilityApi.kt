package com.cocido.oficios.core.network.api.availability

import com.cocido.oficios.core.network.api.availability.dto.BloqueoAgendaDto
import com.cocido.oficios.core.network.api.availability.dto.CrearBloqueoAgendaDto
import com.cocido.oficios.core.network.api.availability.dto.DisponibilidadDiaDto
import com.cocido.oficios.core.network.api.availability.dto.DisponibilidadSemanalDto
import com.cocido.oficios.core.network.api.availability.dto.ReemplazarDisponibilidadDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AvailabilityApi {

    @GET("trabajador-oficio-sub-rubro/{id}/disponibilidad")
    suspend fun getWeeklyAvailability(
        @Path("id") subRubroId: Long,
    ): DisponibilidadSemanalDto

    @PUT("trabajador-oficio-sub-rubro/{id}/disponibilidad")
    suspend fun replaceWeeklyAvailability(
        @Path("id") subRubroId: Long,
        @Body request: ReemplazarDisponibilidadDto,
    ): DisponibilidadSemanalDto

    @GET("trabajador-oficio-sub-rubro/{id}/bloqueos")
    suspend fun listBlocks(
        @Path("id") subRubroId: Long,
    ): List<BloqueoAgendaDto>

    @POST("trabajador-oficio-sub-rubro/{id}/bloqueos")
    suspend fun createBlock(
        @Path("id") subRubroId: Long,
        @Body request: CrearBloqueoAgendaDto,
    ): BloqueoAgendaDto

    @DELETE("trabajador-oficio-sub-rubro/{id}/bloqueos/{bloqueoId}")
    suspend fun deleteBlock(
        @Path("id") subRubroId: Long,
        @Path("bloqueoId") blockId: Long,
    )

    @GET("trabajador-oficio/{id}/disponibilidad")
    suspend fun getDayAvailability(
        @Path("id") workerId: Long,
        @Query("date") date: String,
        @Query("trabajadorOficioSubRubroId") subRubroId: Long,
    ): DisponibilidadDiaDto
}
