package com.cocido.oficios.domain.model

import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaAppointmentTagTest {

    private val zone = DisplayDateTime.ARGENTINA
    private val clock = clockAt(2026, 10, 21, 3, 17)

    @Test
    fun `past in-progress appointment stays on the agenda and has no tag`() {
        val past = order(1, "2026-09-25T07:00:00", BookingStatus.IN_PROGRESS)
        val now = nowInArgentina(clock)
        assertEquals(listOf(1L), upcomingAgendaAppointments(listOf(past), now).map { it.id })
        assertEquals(AgendaAppointmentTag.NONE, past.agendaTag(now))
    }

    @Test
    fun `elapsed confirmed appointment is dropped from the agenda`() {
        val past = order(1, "2026-09-25T07:00:00", BookingStatus.CONFIRMED, end = "2026-09-25T08:00:00")
        val now = nowInArgentina(clock)
        assertTrue(upcomingAgendaAppointments(listOf(past), now).isEmpty())
        assertEquals(AgendaAppointmentTag.NONE, past.agendaTag(now))
    }

    @Test
    fun `today before slot is HOY`() {
        val item = order(2, "2026-10-21T10:00:00", BookingStatus.CONFIRMED, end = "2026-10-21T11:00:00")
        assertEquals(AgendaAppointmentTag.TODAY, item.agendaTag(nowInArgentina(clock)))
    }

    @Test
    fun `today during slot is AHORA even if confirmed`() {
        val item = order(3, "2026-10-21T03:00:00", BookingStatus.CONFIRMED, end = "2026-10-21T04:00:00")
        assertEquals(AgendaAppointmentTag.NOW, item.agendaTag(nowInArgentina(clock)))
    }

    @Test
    fun `today after slot is HOY`() {
        val item = order(4, "2026-10-21T01:00:00", BookingStatus.CONFIRMED, end = "2026-10-21T02:00:00")
        assertEquals(AgendaAppointmentTag.TODAY, item.agendaTag(nowInArgentina(clock)))
    }

    @Test
    fun `future appointment has no tag and stays in the list`() {
        val future = order(5, "2026-10-22T09:00:00", BookingStatus.CONFIRMED)
        val now = nowInArgentina(clock)
        assertEquals(listOf(5L), upcomingAgendaAppointments(listOf(future), now).map { it.id })
        assertEquals(AgendaAppointmentTag.NONE, future.agendaTag(now))
    }

    @Test
    fun `in progress on another day is never AHORA`() {
        val item = order(6, "2026-10-20T03:00:00", BookingStatus.IN_PROGRESS, end = "2026-10-20T04:00:00")
        assertEquals(AgendaAppointmentTag.NONE, item.agendaTag(nowInArgentina(clock)))
    }

    @Test
    fun `in progress today is AHORA even outside the slot`() {
        val item = order(7, "2026-10-21T10:00:00", BookingStatus.IN_PROGRESS, end = "2026-10-21T11:00:00")
        assertEquals(AgendaAppointmentTag.NOW, item.agendaTag(nowInArgentina(clock)))
    }

    @Test
    fun `without end uses session duration of 60 minutes`() {
        val during = order(8, "2026-10-21T03:00:00", BookingStatus.CONFIRMED)
        val after = order(9, "2026-10-21T02:00:00", BookingStatus.CONFIRMED)
        val now = nowInArgentina(clock)
        assertEquals(AgendaAppointmentTag.NOW, during.agendaTag(now))
        assertEquals(AgendaAppointmentTag.TODAY, after.agendaTag(now))
    }

    @Test
    fun `home agenda keeps open past bookings with today and future sorted ascending`() {
        val now = nowInArgentina(clock)
        val filtered = upcomingAgendaAppointments(
            orders = listOf(
                order(1, "2026-09-25T07:00:00", BookingStatus.IN_PROGRESS),
                order(2, "2026-10-22T09:00:00", BookingStatus.CONFIRMED),
                order(3, "2026-10-21T10:00:00", BookingStatus.CONFIRMED),
                order(4, "2026-10-05T12:00:00", BookingStatus.IN_PROGRESS),
                order(9, "2026-09-20T09:00:00", BookingStatus.CONFIRMED, end = "2026-09-20T10:00:00"),
            ),
            now = now,
        )
        assertEquals(listOf(1L, 4L, 3L, 2L), filtered.map { it.id })
        val groups = groupAgendaAppointments(filtered, now.toLocalDate())
        assertEquals(
            listOf("25 de septiembre", "5 de octubre", "21 de octubre", "22 de octubre"),
            groups.map { it.headerLabel },
        )
        assertEquals(LocalDate.of(2026, 9, 25), groups[0].date)
    }

    @Test
    fun `default selected date is today when it has bookings otherwise the nearest day`() {
        val today = LocalDate.of(2026, 10, 21)
        assertEquals(
            today,
            defaultAgendaSelectedDate(
                dates = setOf(LocalDate.of(2026, 10, 21), LocalDate.of(2026, 9, 25)),
                today = today,
            ),
        )
        assertEquals(
            LocalDate.of(2026, 10, 5),
            defaultAgendaSelectedDate(
                dates = setOf(LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 5)),
                today = today,
            ),
        )
        assertEquals(
            LocalDate.of(2026, 9, 25),
            defaultAgendaSelectedDate(
                dates = setOf(LocalDate.of(2026, 9, 25)),
                today = today,
            ),
        )
        assertEquals(
            LocalDate.of(2026, 10, 22),
            defaultAgendaSelectedDate(
                dates = setOf(LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 22)),
                today = today,
            ),
        )
        assertEquals(today, defaultAgendaSelectedDate(dates = emptySet(), today = today))
    }

    @Test
    fun `month navigation selects today in current month or nearest booking in that month`() {
        val today = LocalDate.of(2026, 10, 21)
        val dates = setOf(LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 29), LocalDate.of(2026, 10, 5))
        assertEquals(
            LocalDate.of(2026, 9, 29),
            defaultAgendaDateInMonth(dates, YearMonth.of(2026, 9), today, fallbackDay = 21),
        )
        assertEquals(today, defaultAgendaDateInMonth(dates, YearMonth.of(2026, 10), today, 21))
        assertEquals(
            LocalDate.of(2026, 11, 21),
            defaultAgendaDateInMonth(dates, YearMonth.of(2026, 11), today, 21),
        )
    }

    private fun clockAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Clock {
        val instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant()
        return Clock.fixed(instant, zone)
    }

    private fun order(
        id: Long,
        start: String,
        status: BookingStatus,
        end: String? = null,
    ) = ServiceOrderListItem(
        id = id,
        counterpartName = "Cliente",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.ACCEPTED,
        bookingStatus = status,
        scheduledLabel = null,
        addressLabel = "Av. 25 de Mayo 100",
        rubroLabel = "Plomería",
        scheduledSortKey = start,
        scheduledEndKey = end,
    )
}
