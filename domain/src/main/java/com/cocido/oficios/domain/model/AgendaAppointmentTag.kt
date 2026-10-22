package com.cocido.oficios.domain.model

import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
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

/**
 * Agenda del trabajador (home y agenda completa): pedidos con fecha que siguen
 * activos. Incluye EN_CAMINO / EN_CURSO aunque el día ya haya pasado — el trabajo
 * no se cerró. No incluye historial ni ítems sin fecha.
 */
fun upcomingAgendaAppointments(
    orders: List<ServiceOrderListItem>,
    now: ZonedDateTime,
): List<ServiceOrderListItem> {
    return orders
        .filter { item ->
            item.scheduledDate() != null && !item.isHistoryOrder(now)
        }
        .sortedWith(ordersListComparator(OrdersListSort.NEWEST_FIRST))
}

/** Día inicial del calendario de agenda: siempre hoy. Los pedidos no roban la selección. */
@Suppress("UNUSED_PARAMETER")
fun defaultAgendaSelectedDate(
    orders: List<ServiceOrderListItem>,
    today: LocalDate,
): LocalDate = defaultAgendaSelectedDate(today = today)

@Suppress("UNUSED_PARAMETER")
fun defaultAgendaSelectedDate(
    dates: Set<LocalDate>,
    today: LocalDate,
): LocalDate = defaultAgendaSelectedDate(today = today)

fun defaultAgendaSelectedDate(today: LocalDate): LocalDate = today

@Suppress("UNUSED_PARAMETER")
fun defaultAgendaDateInMonth(
    dates: Set<LocalDate>,
    month: YearMonth,
    today: LocalDate,
    fallbackDay: Int,
): LocalDate = defaultAgendaDateInMonth(month, today, fallbackDay)

fun defaultAgendaDateInMonth(
    month: YearMonth,
    today: LocalDate,
    fallbackDay: Int,
): LocalDate {
    if (YearMonth.from(today) == month) return today
    return month.atDay(fallbackDay.coerceIn(1, month.lengthOfMonth()))
}
