package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.orders.dto.BookingSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.ContrapropuestaDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.TrackingBookingDto
import com.cocido.oficios.domain.model.BookingTracking
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.CounterOffer
import com.cocido.oficios.domain.model.CounterOfferStatus
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.domain.model.GeoPoint
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal val ORDER_DISPLAY_ZONE: ZoneId = DisplayDateTime.ARGENTINA

private val DISPLAY_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("es-AR"))
private val TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("es-AR"))

fun bookingTypeLabel(tipo: String?): String? {
    val raw = tipo?.trim().orEmpty()
    if (raw.isEmpty()) return null
    return when (raw.uppercase(Locale.ROOT)) {
        "PRESUPUESTO" -> "Presupuesto"
        "TRABAJO" -> "Trabajo"
        "AMBOS" -> "Presupuesto y trabajo"
        else -> raw.lowercase(Locale.forLanguageTag("es-AR")).replaceFirstChar { it.titlecase(Locale.forLanguageTag("es-AR")) }
    }
}

fun String?.toDisplayDateLabel(zone: ZoneId = ORDER_DISPLAY_ZONE): String? {
    if (this.isNullOrBlank()) return null
    return toZonedDateTimeOrNull(zone)?.formatDisplayLabel() ?: this
}

/**
 * Rango del booking en hora de Argentina.
 * Con `Z` u offset se convierte el instante. Sin offset la hora ya es local y no se mueve.
 * Inicio y fin usan el mismo huso para no invertir el rango. Si el fin cae otro día, se conserva la fecha.
 */
internal fun formatScheduledRange(
    startIso: String?,
    endIso: String?,
    zone: ZoneId = ORDER_DISPLAY_ZONE,
): String? {
    val start = startIso.toZonedDateTimeOrNull(zone) ?: return startIso.toDisplayDateLabel(zone)
    val end = endIso.toZonedDateTimeOrNull(zone) ?: return start.formatDisplayLabel()
    val (from, to) = if (end.isBefore(start)) end to start else start to end
    val endLabel = if (from.toLocalDate() == to.toLocalDate()) {
        to.format(TIME_FORMATTER)
    } else {
        to.formatDisplayLabel()
    }
    return "${from.formatDisplayLabel()} - $endLabel"
}

private fun String?.toZonedDateTimeOrNull(zone: ZoneId): ZonedDateTime? =
    DisplayDateTime.toZonedDateTime(this, zone)

private fun ZonedDateTime.formatDisplayLabel(): String =
    format(DISPLAY_FORMATTER).replace(".", "").replaceFirstChar { it.uppercase() }

fun String.toRequestStatus(): ServiceRequestStatus = when (trim().uppercase(Locale.ROOT)) {
    "PENDIENTE" -> ServiceRequestStatus.PENDING
    "CONTRAPROPUESTA" -> ServiceRequestStatus.COUNTER_OFFER
    // La solicitud cerrada por un booking finalizado sigue siendo una solicitud aceptada:
    // el detalle lo resuelve el estado del booking.
    "ACEPTADA", "FINALIZADA", "FINALIZADO" -> ServiceRequestStatus.ACCEPTED
    "CANCELADA", "RECHAZADA" -> ServiceRequestStatus.CANCELLED
    "EXPIRADA" -> ServiceRequestStatus.EXPIRED
    else -> ServiceRequestStatus.PENDING
}

fun String.toBookingStatus(): BookingStatus = when (trim().uppercase(Locale.ROOT)) {
    "PENDIENTE" -> BookingStatus.PENDING
    "CONFIRMADA", "CONFIRMADO" -> BookingStatus.CONFIRMED
    "EN_CAMINO" -> BookingStatus.EN_ROUTE
    // LLEGUE es el estado de tracking post-llegada. Si llega como estado del booking,
    // el pedido sigue vivo y la UI es la de código de finalización (igual que EN_CURSO).
    "EN_CURSO", "LLEGUE" -> BookingStatus.IN_PROGRESS
    "FINALIZADA", "FINALIZADO" -> BookingStatus.FINISHED
    "CANCELADA", "CANCELADO" -> BookingStatus.CANCELLED
    "EXPIRADA", "EXPIRADO" -> BookingStatus.EXPIRED
    else -> BookingStatus.PENDING
}

/**
 * El booking es la fuente de verdad una vez que existe y avanzó: si está confirmado,
 * en camino, en curso o finalizado, la solicitud ya fue aceptada aunque su `estado`
 * venga desfasado (p. ej. PENDIENTE o un valor desconocido). Sin esto, un booking
 * FINALIZADA mostraba «Pedido Pendiente» con Aceptar/Rechazar.
 * Cancelación y expiración de la solicitud se respetan tal cual.
 */
internal fun resolveRequestStatus(estado: String, bookingStatus: BookingStatus?): ServiceRequestStatus {
    val request = estado.toRequestStatus()
    if (request == ServiceRequestStatus.CANCELLED || request == ServiceRequestStatus.EXPIRED) return request
    return when (bookingStatus) {
        BookingStatus.CONFIRMED,
        BookingStatus.EN_ROUTE,
        BookingStatus.IN_PROGRESS,
        BookingStatus.FINISHED,
        -> ServiceRequestStatus.ACCEPTED
        else -> request
    }
}

private fun BookingStatus?.isCancellable(): Boolean =
    this != BookingStatus.FINISHED && this != BookingStatus.CANCELLED && this != BookingStatus.EXPIRED

fun String.toCounterOfferStatus(): CounterOfferStatus = when (uppercase()) {
    "PENDIENTE" -> CounterOfferStatus.PENDING
    "ACEPTADA" -> CounterOfferStatus.ACCEPTED
    "RECHAZADA" -> CounterOfferStatus.REJECTED
    else -> CounterOfferStatus.PENDING
}

private fun domicilioLabel(
    calle: String?,
    numero: String?,
    barrioNombre: String?,
): String {
    val streetLine = listOfNotNull(calle, numero).joinToString(" ").ifBlank { null }
    return listOfNotNull(streetLine, barrioNombre).joinToString(", ").ifBlank { "Domicilio sin especificar" }
}

private fun ContrapropuestaDto.toDomain(): CounterOffer = CounterOffer(
    id = id,
    proposedLabel = fechaPropuestaInicio.toDisplayDateLabel().orEmpty(),
    proposedPrice = nuevoPrecio,
    status = estado.toCounterOfferStatus(),
)

private fun BookingSimpleDto.scheduledLabel(): String? {
    if (id == 0L && inicioConfirmado.isNullOrBlank()) return null
    return formatScheduledRange(inicioConfirmado, finConfirmado)
}

fun SolicitudServicioDto.toDomain(): ServiceOrder {
    val clientPersona = cliente?.usuario?.persona
    val clientName = listOfNotNull(clientPersona?.nombres, clientPersona?.apellido)
        .joinToString(" ").ifBlank { "Cliente" }

    val workerPersona = trabajadorOficio?.usuario?.persona
    val workerName = listOfNotNull(workerPersona?.nombres, workerPersona?.apellido)
        .joinToString(" ").ifBlank { "Trabajador" }

    val pendingCounterOffer = contrapropuestas
        .firstOrNull { it.estado.equals("PENDIENTE", ignoreCase = true) }
        ?.toDomain()
    val acceptedPrice = contrapropuestas
        .firstOrNull { it.estado.equals("ACEPTADA", ignoreCase = true) }
        ?.nuevoPrecio
    val bookingStatus = booking?.estado?.takeIf { it.isNotBlank() }?.toBookingStatus()
    val requestStatus = resolveRequestStatus(estado, bookingStatus)

    return ServiceOrder(
        id = id,
        clientName = clientName,
        workerName = workerName,
        workerId = trabajadorOficio?.id ?: 0L,
        rubroLabel = rubro?.nombre.orEmpty(),
        subRubroLabel = subRubro?.nombre.orEmpty(),
        status = requestStatus,
        description = descripcion.orEmpty(),
        addressLabel = domicilioLabel(domicilio?.calle, domicilio?.numero, domicilio?.barrio?.nombre),
        addressLatitude = validCoordinate(domicilio?.latitud, domicilio?.longitud)?.first,
        addressLongitude = validCoordinate(domicilio?.latitud, domicilio?.longitud)?.second,
        contactName = contacto?.nombre.orEmpty(),
        contactPhone = contacto?.telefonoWhatsapp.orEmpty(),
        workerPhone = resolveWorkerPhone(
            workerWhatsapp = trabajadorOficio?.telefonoWhatsapp,
            userWhatsapp = trabajadorOficio?.usuario?.telefonoWhatsapp,
            personaPhone = trabajadorOficio?.usuario?.persona?.telefono,
        ),
        rejectionReason = motivoRechazo,
        bookingId = booking?.id?.takeIf { it != 0L },
        scheduledLabel = booking?.scheduledLabel(),
        bookingStatus = bookingStatus,
        bookingType = bookingTypeLabel(booking?.tipo),
        // Un booking terminal no se cancela aunque el backend mande puedeCancelar=true.
        canCancelBooking = (booking?.puedeCancelar ?: false) && bookingStatus.isCancellable(),
        cancellationDeadlineLabel = booking?.fechaLimiteCancelacion.toDisplayDateLabel(),
        pendingCounterOffer = pendingCounterOffer,
        acceptedPrice = acceptedPrice,
        completionCode = booking?.codigoFinalizacion,
        workerAvatarUrl = trabajadorOficio?.usuario?.fotoPerfil.toPhotoUrl(),
        clientAvatarUrl = cliente?.usuario?.fotoPerfil.toPhotoUrl(),
        cancelledBy = booking?.canceladoPor,
        cancellationReason = booking?.motivoCancelacion ?: motivoRechazo,
    )
}

private val trackingJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

fun TrackingBookingDto.toDomain(): BookingTracking? {
    val latitude = lat.toCoordinateOrNull()
    val longitude = lng.toCoordinateOrNull()
    val point = if (latitude != null && longitude != null) {
        GeoPoint(latitude, longitude).takeIf { it.isValid }
    } else {
        null
    }
    // EN_CAMINO sin coordenadas válidas sigue sin tracking, para no pintar un pin falso.
    // LLEGUE a veces vuelve solo con el estado: alcanza para abrir el código de finalización.
    if (point == null && !estado.trim().equals("LLEGUE", ignoreCase = true)) return null
    return BookingTracking(
        bookingId = bookingId,
        workerId = trabajadorOficioId,
        status = estado,
        location = point,
        updatedAtIso = createdAt,
    )
}

/**
 * GET /tracking-booking puede venir como objeto, como `{ data: ... }` o como lista.
 * lat/lng llegan como string (y a veces como número).
 */
fun JsonElement.toBookingTracking(): BookingTracking? {
    val payload = unwrapTrackingElement(this) ?: return null
    return trackingJson.decodeFromJsonElement(TrackingBookingDto.serializer(), payload).toDomain()
}

/** Teléfono del trabajador. El contacto de la solicitud es del cliente y no se usa acá. */
fun resolveWorkerPhone(
    workerWhatsapp: String?,
    userWhatsapp: String?,
    personaPhone: String?,
): String? = listOf(workerWhatsapp, userWhatsapp, personaPhone)
    .firstOrNull { !it.isNullOrBlank() }
    ?.trim()

private fun String.toCoordinateOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

private fun unwrapTrackingElement(element: JsonElement): JsonObject? = when (element) {
    is JsonNull -> null
    is JsonArray -> element.mapNotNull { unwrapTrackingElement(it) }.lastOrNull()
    is JsonObject -> {
        val hasCoordinates = element["lat"] != null || element["lng"] != null ||
            element["latitude"] != null || element["longitude"] != null ||
            element["latitud"] != null || element["longitud"] != null
        if (!hasCoordinates && element["data"] != null) {
            unwrapTrackingElement(element["data"]!!)
        } else {
            element.withCoordinateAliases()
        }
    }
    else -> null
}

private fun validCoordinate(latitude: Double?, longitude: Double?): Pair<Double, Double>? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return GeoPoint(lat, lng).takeIf { it.isValid }?.let { it.latitude to it.longitude }
}

private fun JsonObject.withCoordinateAliases(): JsonObject {
    val lat = this["lat"] ?: this["latitude"] ?: this["latitud"] ?: return this
    val lng = this["lng"] ?: this["longitude"] ?: this["longitud"] ?: return this
    if (this["lat"] != null && this["lng"] != null) return this
    return JsonObject(this + ("lat" to lat) + ("lng" to lng))
}

/**
 * `SolicitudServicioSimpleDto` puede no traer `trabajadorOficio` (BACK-ORDERS-003).
 * Si falta, el listado del cliente usa el subrubro como contraparte.
 */
fun SolicitudServicioSimpleDto.toListItem(isWorkerViewing: Boolean): ServiceOrderListItem {
    val clientPersona = cliente?.usuario?.persona
    val clientName = listOfNotNull(clientPersona?.nombres, clientPersona?.apellido).joinToString(" ")
    val workerPersona = trabajadorOficio?.usuario?.persona
    val workerName = listOfNotNull(workerPersona?.nombres, workerPersona?.apellido).joinToString(" ")
    val counterpart = if (isWorkerViewing) {
        clientName.ifBlank { subRubro?.nombre.orEmpty() }
    } else {
        workerName.ifBlank { subRubro?.nombre.orEmpty() }
    }
    val bookingStatus = booking?.estado?.takeIf { it.isNotBlank() }?.toBookingStatus()
    val requestStatus = resolveRequestStatus(estado, bookingStatus)
    return ServiceOrderListItem(
        id = id,
        counterpartName = counterpart,
        subRubroLabel = subRubro?.nombre.orEmpty(),
        status = requestStatus,
        bookingStatus = bookingStatus,
        scheduledLabel = booking?.scheduledLabel(),
        addressLabel = if (isWorkerViewing && requestStatus != ServiceRequestStatus.ACCEPTED) {
            ""
        } else {
            domicilioLabel(domicilio?.calle, domicilio?.numero, domicilio?.barrio?.nombre)
        },
        rubroLabel = rubro?.nombre.orEmpty(),
        avatarUrl = (if (isWorkerViewing) cliente?.usuario?.fotoPerfil else trabajadorOficio?.usuario?.fotoPerfil).toPhotoUrl(),
        isVerified = if (isWorkerViewing) {
            cliente?.estado.equals("VALIDADO", ignoreCase = true) ||
                cliente?.usuario?.emailVerificado == true
        } else {
            trabajadorOficio?.estado.equals("VALIDADO", ignoreCase = true) ||
                trabajadorOficio?.usuario?.emailVerificado == true
        },
        workerId = trabajadorOficio?.id ?: 0L,
        bookingId = booking?.id?.takeIf { it != 0L },
        // Listado OpenAPI: no hay fecha de solicitud/creación. Booking confirmado, si no el slot pedido.
        scheduledSortKey = booking?.inicioConfirmado ?: booking?.slotTurno?.inicio,
        scheduledEndKey = booking?.finConfirmado ?: booking?.slotTurno?.fin,
        scheduledDurationMinutes = booking?.slotTurno?.duracion,
    )
}
