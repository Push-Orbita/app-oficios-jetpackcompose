package com.cocido.oficios.core.network.api.orders

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.orders.dto.BookingDto
import com.cocido.oficios.core.network.api.orders.dto.CancelarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateCalificacionRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateContrapropuestaRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateSolicitudServicioRequestDto
import com.cocido.oficios.core.network.api.orders.dto.FinalizeBookingRequestDto
import com.cocido.oficios.core.network.api.orders.dto.MarcarTrackingRequestDto
import com.cocido.oficios.core.network.api.orders.dto.RechazarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSearchPageDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.UpdateSolicitudEstadoRequestDto
import com.cocido.oficios.core.network.api.orders.dto.TrackingBookingDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrdersApi {

    @POST("solicitud-servicio")
    suspend fun createSolicitud(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateSolicitudServicioRequestDto,
    ): SolicitudServicioDto

    @GET("solicitud-servicio/mis-solicitudes-cliente")
    suspend fun getMisSolicitudesCliente(): PaginatedResponseDto<SolicitudServicioSimpleDto>

    @GET("solicitud-servicio/search")
    suspend fun getSolicitudesTrabajador(): SolicitudServicioSearchPageDto

    @GET("solicitud-servicio/{id}")
    suspend fun getSolicitud(@Path("id") id: Long): SolicitudServicioDto

    @PATCH("solicitud-servicio/{id}/aceptar")
    suspend fun aceptarSolicitud(@Path("id") id: Long): SolicitudServicioDto

    @PATCH("solicitud-servicio/{id}/rechazar")
    suspend fun rechazarSolicitud(
        @Path("id") id: Long,
        @Body request: RechazarSolicitudRequestDto,
    ): SolicitudServicioDto

    @PATCH("solicitud-servicio/{id}/contrapropuesta")
    suspend fun enviarContrapropuesta(
        @Path("id") id: Long,
        @Body request: CreateContrapropuestaRequestDto,
    ): JsonElement

    @PATCH("solicitud-servicio/{id}/cancelar")
    suspend fun cancelarSolicitudConfirmada(
        @Path("id") id: Long,
        @Body request: CancelarSolicitudRequestDto,
    ): SolicitudServicioDto

    @PATCH("solicitud-servicio/{id}")
    suspend fun cancelarSolicitudPendiente(
        @Path("id") id: Long,
        @Body request: UpdateSolicitudEstadoRequestDto,
    ): SolicitudServicioDto

    @PATCH("contrapropuesta/{id}/aceptar")
    suspend fun aceptarContrapropuesta(@Path("id") id: Long): JsonElement

    @PATCH("contrapropuesta/{id}/rechazar")
    suspend fun rechazarContrapropuesta(@Path("id") id: Long): JsonElement

    @GET("booking/{id}")
    suspend fun getBooking(@Path("id") id: Long): BookingDto

    @PATCH("booking/{id}/finalizar")
    suspend fun finalizarBooking(
        @Path("id") id: Long,
        @Body request: FinalizeBookingRequestDto = FinalizeBookingRequestDto(),
    ): BookingDto

    @POST("tracking-booking/booking/{bookingId}/marcar")
    suspend fun marcarTracking(
        @Path("bookingId") bookingId: Long,
        @Body request: MarcarTrackingRequestDto,
    ): TrackingBookingDto

    @GET("tracking-booking/booking/{bookingId}")
    suspend fun getTracking(
        @Path("bookingId") bookingId: Long,
    ): TrackingBookingDto?

    @POST("calificaciones")
    suspend fun crearCalificacion(
        @Body request: CreateCalificacionRequestDto,
    ): JsonElement
}
