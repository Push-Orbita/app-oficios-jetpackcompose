package com.cocido.oficios.core.network.api.orders.dto

import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import com.cocido.oficios.core.network.api.profile.dto.ContactoSinPersonaDto
import com.cocido.oficios.core.network.api.profile.dto.DomicilioSinPersonaDto
import com.cocido.oficios.core.network.serialization.FlexibleNumberStringSerializer
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Solicitud de servicio — POST /solicitud-servicio y acciones sobre una existente.
// Campos y estados confirmados contra apioficios.pushsoftware.com.ar/api-json
// (tags "Solicitud de servicio", "Contrapropuesta", "Booking (cita)",
// "Tracking (en camino / llegada)", "Calificaciones").
// ---------------------------------------------------------------------------

@Serializable
data class CreateSolicitudServicioRequestDto(
    val trabajadorOficioSubRubroId: Long,
    val domicilioId: Long,
    val contactoId: Long,
    val slotTurnoId: Long? = null,
    val fechaPropuestaInicio: String? = null,
    val fechaPropuestaFin: String? = null,
    val descripcion: String? = null,
)

@Serializable
data class RechazarSolicitudRequestDto(
    val motivoRechazo: String,
)

@Serializable
data class CancelarSolicitudRequestDto(
    val motivoCancelacion: String,
)

@Serializable
data class UpdateSolicitudEstadoRequestDto(
    val estado: String,
)

@Serializable
data class CreateContrapropuestaRequestDto(
    val fechaPropuestaInicio: String,
    val fechaPropuestaFin: String,
    val nuevoPrecio: Double? = null,
)

@Serializable
data class MarcarTrackingRequestDto(
    val estado: String,
    val lat: String,
    val lng: String,
)

@Serializable
data class TrackingBookingDto(
    val id: Long = 0L,
    val bookingId: Long = 0L,
    val trabajadorOficioId: Long = 0L,
    val estado: String = "",
    @Serializable(with = FlexibleNumberStringSerializer::class)
    val lat: String = "",
    @Serializable(with = FlexibleNumberStringSerializer::class)
    val lng: String = "",
    val createdAt: String = "",
)

@Serializable
data class CreateCalificacionRequestDto(
    val bookingId: Long,
    val calidad: Int,
    val puntualidad: Int,
    val comentario: String? = null,
)

@Serializable
data class TrabajadorOficioSimpleDto(
    val id: Long = 0L,
    val usuario: UsuarioDto? = null,
    val estado: String? = null,
    val documento: String? = null,
    val telefonoWhatsapp: String? = null,
)

@Serializable
data class ClienteSimpleDto(
    val id: Long = 0L,
    val usuario: UsuarioDto? = null,
    val estado: String? = null,
    val documento: String? = null,
)

@Serializable
data class SlotTurnoEnBookingDto(
    val id: Long,
    val inicio: String? = null,
    val fin: String? = null,
    val duracion: Int? = null,
)

@Serializable
data class BookingSimpleDto(
    val id: Long = 0L,
    val inicioConfirmado: String? = null,
    val finConfirmado: String? = null,
    val tipo: String? = null,
    val estado: String = "",
    val domicilioTexto: String? = null,
    val contactoTexto: String? = null,
    val slotTurno: SlotTurnoEnBookingDto? = null,
    val fechaLimiteCancelacion: String? = null,
    val puedeCancelar: Boolean = false,
    val codigoFinalizacion: String? = null,
    val canceladoPor: String? = null,
    val motivoCancelacion: String? = null,
)

@Serializable
data class ContrapropuestaDto(
    val id: Long,
    val fechaPropuestaInicio: String,
    val fechaPropuestaFin: String,
    val nuevoPrecio: Double? = null,
    val estado: String,
)

@Serializable
data class SolicitudServicioDto(
    val id: Long,
    val cliente: ClienteSimpleDto? = null,
    val trabajadorOficio: TrabajadorOficioSimpleDto? = null,
    val rubro: RubroDto? = null,
    val subRubro: SubRubroDto? = null,
    val domicilio: DomicilioSinPersonaDto? = null,
    val contacto: ContactoSinPersonaDto? = null,
    val estado: String,
    val descripcion: String? = null,
    val motivoRechazo: String? = null,
    val booking: BookingSimpleDto? = null,
    val contrapropuestas: List<ContrapropuestaDto> = emptyList(),
)

@Serializable
data class SolicitudServicioSimpleDto(
    val id: Long,
    val cliente: ClienteSimpleDto? = null,
    val trabajadorOficio: TrabajadorOficioSimpleDto? = null,
    val rubro: RubroDto? = null,
    val subRubro: SubRubroDto? = null,
    val domicilio: DomicilioSinPersonaDto? = null,
    val contacto: ContactoSinPersonaDto? = null,
    val estado: String = "",
    val descripcion: String? = null,
    val booking: BookingSimpleDto? = null,
)

@Serializable
data class SolicitudServicioSearchResponseDto(
    val trabajador: TrabajadorOficioSimpleDto? = null,
    val solicitudes: List<SolicitudServicioSimpleDto> = emptyList(),
)

/**
 * Runtime de `GET /solicitud-servicio/search` (23 sep 2026): el cuerpo es
 * `{ data: { trabajador, solicitudes }, metadata }`, no el DTO suelto del OpenAPI.
 * Si el backend aplana la respuesta, también se acepta el shape de raíz.
 */
@Serializable
data class SolicitudServicioSearchPageDto(
    val data: SolicitudServicioSearchResponseDto? = null,
    val trabajador: TrabajadorOficioSimpleDto? = null,
    val solicitudes: List<SolicitudServicioSimpleDto> = emptyList(),
) {
    fun resolved(): SolicitudServicioSearchResponseDto = SolicitudServicioSearchResponseDto(
        trabajador = data?.trabajador ?: trabajador,
        solicitudes = if (data != null) data.solicitudes else solicitudes,
    )
}

@Serializable
data class ResendCompletionCodeResponseDto(
    val resent: Boolean = false,
    val retryAfterSeconds: Double? = null,
)

@Serializable
data class FinalizeBookingRequestDto(
    val code: String? = null,
)

@Serializable
data class BookingDto(
    val id: Long,
    val solicitudServicio: SolicitudServicioDto? = null,
    val inicioConfirmado: String? = null,
    val finConfirmado: String? = null,
    val tipo: String? = null,
    val estado: String,
    val puedeCancelar: Boolean = false,
    /** Solo viene para el cliente mientras el booking está EN_CURSO. */
    val codigoFinalizacion: String? = null,
)
