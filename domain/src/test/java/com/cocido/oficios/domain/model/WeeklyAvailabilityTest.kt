package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyAvailabilityTest {

    @Test
    fun validRangePasses() {
        assertNull(TimeRangeValidation.validateRange(TimeRange("08:00", "12:00")))
        assertEquals(8 * 60, TimeRangeValidation.parseMinutes("08:00"))
        assertEquals(8 * 60, TimeRangeValidation.parseMinutes("8:00:00"))
    }

    @Test
    fun rejectsInvalidFormatAndOvernight() {
        assertEquals(
            "Use el formato HH:mm (por ejemplo 08:00).",
            TimeRangeValidation.validateRange(TimeRange("8", "12:00")),
        )
        assertEquals(
            "La hora de fin debe ser posterior al inicio.",
            TimeRangeValidation.validateRange(TimeRange("18:00", "09:00")),
        )
        assertEquals(
            "La hora de fin debe ser posterior al inicio.",
            TimeRangeValidation.validateRange(TimeRange("09:00", "09:00")),
        )
    }

    @Test
    fun rejectsOverlappingRangesOnSameDay() {
        val error = TimeRangeValidation.validateRanges(
            listOf(TimeRange("08:00", "12:00"), TimeRange("11:00", "14:00")),
        )
        assertEquals("Las franjas de un mismo día no pueden superponerse.", error)
        assertNull(
            TimeRangeValidation.validateRanges(
                listOf(TimeRange("08:00", "12:00"), TimeRange("12:00", "14:00")),
            ),
        )
    }

    @Test
    fun sundayIsOmittedUnlessIncluded() {
        val week = WeeklyAvailability.default().copy(
            days = WeeklyAvailability.default().days + DayAvailability(
                WeeklyAvailability.SUNDAY,
                listOf(TimeRange("10:00", "13:00")),
            ),
            includeSunday = false,
        )
        assertFalse(week.toReplaceDays().any { it.day == WeeklyAvailability.SUNDAY })
        assertTrue(week.copy(includeSunday = true).toReplaceDays().any { it.day == WeeklyAvailability.SUNDAY })
    }

    @Test
    fun defaultWeekHasWeekdaysAndNoSunday() {
        val week = WeeklyAvailability.default()
        assertEquals(WeeklyAvailability.DEFAULT_DAYS, week.activeDays)
        assertFalse(week.includeSunday)
        assertNull(TimeRangeValidation.validateWeek(week))
    }

    @Test
    fun hoursCoveredIncludesStartExcludesEnd() {
        val hours = TimeRangeValidation.hoursCovered(
            listOf(TimeRange("08:00", "12:00"), TimeRange("16:30", "20:00")),
        )
        assertEquals(setOf(8, 9, 10, 11, 17, 18, 19), hours)
        assertFalse(12 in hours)
        assertFalse(16 in hours)
    }

    @Test
    fun blockValidationRequiresBothHoursOrNeither() {
        assertNull(TimeRangeValidation.validateBlock("2026-12-24", null, null))
        assertEquals(
            "Indique inicio y fin, o deje ambas vacías para bloquear el día entero.",
            TimeRangeValidation.validateBlock("2026-12-24", "14:00", null),
        )
        assertEquals(
            "Indique la fecha en formato AAAA-MM-DD.",
            TimeRangeValidation.validateBlock("24/12", null, null),
        )
    }

    @Test
    fun mapsManagedScheduleToWeeklyAvailability() {
        val week = listOf(
            ManagedWorkSchedule(
                dayFrom = "LUNES",
                dayTo = "MARTES",
                ranges = listOf(ManagedTimeRange("09:00:00", "13:00")),
            ),
        ).toWeeklyAvailability()
        assertEquals(listOf("LUNES", "MARTES"), week.activeDays)
        assertEquals("09:00", week.rangesOn("LUNES").single().start)
        assertEquals("13:00", week.rangesOn("MARTES").single().end)
        assertFalse(week.includeSunday)
    }

    @Test
    fun changeRangeClearsEndWhenStartMovesPastIt() {
        val week = WeeklyAvailability.default().changeRange("LUNES", 0, from = "10:00")
        assertEquals("10:00", week.rangesOn("LUNES").single().start)
        assertEquals("18:00", week.rangesOn("LUNES").single().end)

        val cleared = week.changeRange("LUNES", 0, to = "12:00")
            .changeRange("LUNES", 0, from = "14:00")
        assertEquals("14:00", cleared.rangesOn("LUNES").single().start)
        assertEquals("", cleared.rangesOn("LUNES").single().end)
    }
}