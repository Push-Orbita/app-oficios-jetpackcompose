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
import com.cocido.oficios.domain.model.GeoPoint
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("es-AR"))

fun String?.toDisplayDateLabel(): String? {
    if (this.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(this).format(DISPLAY_FORMATTER).replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        this
    }
}

fun String.toRequestStatus(): ServiceRequestStatus = when (uppercase()) {
    "PENDIENTE" -> ServiceRequestStatus.PENDING
    "CONTRAPROPUESTA" -> ServiceRequestStatus.COUNTER_OFFER
    "ACEPTADA" -> ServiceRequestStatus.ACCEPTED
    "CANCELADA" -> ServiceRequestStatus.CANCELLED
    "EXPIRADA" -> ServiceRequestStatus.EXPIRED
    else -> ServiceRequestStatus.PENDING
}

fun String.toBookingStatus(): BookingStatus = when (uppercase()) {
    "PENDIENTE" -> BookingStatus.PENDING
    "CONFIRMADA" -> BookingStatus.CONFIRMED
    "EN_CAMINO" -> BookingStatus.EN_ROUTE
    "EN_CURSO" -> BookingStatus.IN_PROGRESS
    "FINALIZADA" -> BookingStatus.FINISHED
    "CANCELADA" -> BookingStatus.CANCELLED
    else -> BookingStatus.PENDING
}

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
    val start = inicioConfirmado.toDisplayDateLabel()
    val end = finConfirmado?.let { OffsetDateTime.parse(it).format(DateTimeFormatter.ofPattern("HH:mm")) }
    return if (start != null && end != null) "$start - $end" else start
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

    return ServiceOrder(
        id = id,
        clientName = clientName,
        workerName = workerName,
        workerId = trabajadorOficio?.id ?: 0L,
        rubroLabel = rubro?.nombre.orEmpty(),
        subRubroLabel = subRubro?.nombre.orEmpty(),
        status = estado.toRequestStatus(),
        description = descripcion.orEmpty(),
        addressLabel = domicilioLabel(domicilio?.calle, domicilio?.numero, domicilio?.barrio?.nombre),
        addressLatitude = domicilio?.latitud,
        addressLongitude = domicilio?.longitud,
        contactName = contacto?.nombre.orEmpty(),
        contactPhone = contacto?.telefonoWhatsapp.orEmpty(),
        rejectionReason = motivoRechazo,
        bookingId = booking?.id,
        scheduledLabel = booking?.scheduledLabel(),
        bookingStatus = booking?.estado?.toBookingStatus(),
        bookingType = booking?.tipo,
        canCancelBooking = booking?.puedeCancelar ?: false,
        pendingCounterOffer = pendingCounterOffer,
        completionCode = booking?.codigoFinalizacion,
    )
}

fun TrackingBookingDto.toDomain(): BookingTracking? {
    val latitude = lat.toDoubleOrNull() ?: return null
    val longitude = lng.toDoubleOrNull() ?: return null
    val point = GeoPoint(latitude, longitude)
    if (!point.isValid) return null
    return BookingTracking(
        bookingId = bookingId,
        workerId = trabajadorOficioId,
        status = estado,
        location = point,
        updatedAtIso = createdAt,
    )
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
    return ServiceOrderListItem(
        id = id,
        counterpartName = counterpart,
        subRubroLabel = subRubro?.nombre.orEmpty(),
        status = estado.toRequestStatus(),
        bookingStatus = booking?.estado?.toBookingStatus(),
        scheduledLabel = booking?.scheduledLabel(),
        addressLabel = domicilioLabel(domicilio?.calle, domicilio?.numero, domicilio?.barrio?.nombre),
    )
}
