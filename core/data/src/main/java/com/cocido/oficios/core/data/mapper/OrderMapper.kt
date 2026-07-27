package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.orders.dto.BookingSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.ContrapropuestaDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.CounterOffer
import com.cocido.oficios.domain.model.CounterOfferStatus
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
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
        contactName = contacto?.nombre.orEmpty(),
        contactPhone = contacto?.telefonoWhatsapp.orEmpty(),
        rejectionReason = motivoRechazo,
        bookingId = booking?.id,
        scheduledLabel = booking?.scheduledLabel(),
        bookingStatus = booking?.estado?.toBookingStatus(),
        bookingType = booking?.tipo,
        canCancelBooking = booking?.puedeCancelar ?: false,
        pendingCounterOffer = pendingCounterOffer,
    )
}

/**
 * `SolicitudServicioSimpleDto` (usado tanto en "mis-solicitudes-cliente" como en el
 * "search" del trabajador) solo trae el `cliente` — nunca el `trabajadorOficio`. Para el
 * cliente viendo su propia lista, ese `cliente` es él mismo, así que no sirve como
 * "contraparte"; se usa el subrubro como texto de la tarjeta. Para el trabajador viendo
 * solicitudes entrantes, `cliente` sí es la contraparte real. Gap de backend: no hay forma
 * de mostrar el nombre del trabajador en la lista del cliente sin pedir cada detalle.
 */
fun SolicitudServicioSimpleDto.toListItem(isWorkerViewing: Boolean): ServiceOrderListItem {
    val clientPersona = cliente?.usuario?.persona
    val clientName = listOfNotNull(clientPersona?.nombres, clientPersona?.apellido).joinToString(" ")
    val counterpart = if (isWorkerViewing) clientName.ifBlank { subRubro?.nombre.orEmpty() } else subRubro?.nombre.orEmpty()
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
