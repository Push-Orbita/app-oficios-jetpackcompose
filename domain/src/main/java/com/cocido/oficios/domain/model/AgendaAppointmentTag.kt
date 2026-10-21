package com.cocido.oficios.domain.model

import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

/** Etiqueta de card en Agenda. Nunca se infiere solo por estado de booking. */
enum class AgendaAppointmentTag {
    NOW,
    TODAY,
    NONE,
}

/** Si el API no manda fin ni duración, la franja de sesión se asume de 60 minutos. */
const val DEFAULT_AGENDA_SESSION_MINUTES = 60

fun nowInArgentina(clock: Clock): ZonedDateTime = ZonedDateTime.now(clock.withZone(DisplayDateTime.ARGENTINA))

fun ServiceOrderListItem.agendaSlotStart(clockZone: ZoneId = DisplayDateTime.ARGENTINA): ZonedDateTime? =
    DisplayDateTime.toZonedDateTime(scheduledSortKey, clockZone)

fun ServiceOrderListItem.agendaSlotEndExclusive(clockZone: ZoneId = DisplayDateTime.ARGENTINA): ZonedDateTime? {
    val start = agendaSlotStart(clockZone) ?: return null
    val explicitEnd = DisplayDateTime.toZonedDateTime(scheduledEndKey, clockZone)
    if (explicitEnd != null && explicitEnd.isAfter(start)) return explicitEnd
    val minutes = scheduledDurationMinutes?.takeIf { it > 0 } ?: DEFAULT_AGENDA_SESSION_MINUTES
    return start.plusMinutes(minutes.toLong())
}

/**
 * AHORA: hoy y (ahora dentro de la franja, o LLEGUE/EN_CURSO). Nunca por estado solo.
 * HOY: fecha de hoy que no cumple AHORA.
 * Sin etiqueta: pasado o futuro.
 */
fun ServiceOrderListItem.agendaTag(now: ZonedDateTime): AgendaAppointmentTag {
    val zone = now.zone
    val start = agendaSlotStart(zone) ?: return AgendaAppointmentTag.NONE
    if (start.toLocalDate() != now.toLocalDate()) return AgendaAppointmentTag.NONE
    val inProgressToday = bookingStatus == BookingStatus.IN_PROGRESS
    val end = agendaSlotEndExclusive(zone)
    val inSlot = end != null && !now.isBefore(start) && now.isBefore(end)
    return if (inSlot || inProgressToday) AgendaAppointmentTag.NOW else AgendaAppointmentTag.TODAY
}

/** Agenda del Inicio: solo hoy en adelante, orden ascendente. Sin fecha no entra. */
fun upcomingAgendaAppointments(
    orders: List<ServiceOrderListItem>,
    now: ZonedDateTime,
): List<ServiceOrderListItem> {
    val today = now.toLocalDate()
    return orders
        .filter { item ->
            val date = item.scheduledDate() ?: return@filter false
            !date.isBefore(today)
        }
        .sortedWith(
            compareBy<ServiceOrderListItem> { it.scheduledSortKey.orEmpty() }
                .thenBy { it.id },
        )
}
