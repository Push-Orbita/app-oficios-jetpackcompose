package com.cocido.oficios.domain.validation

import com.cocido.oficios.domain.model.DayAvailability
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.WeeklyAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduleValidatorTest {

    @Test
    fun emptyWeekRequiresAtLeastOneDay() {
        assertEquals(
            ScheduleValidator.EMPTY_WEEK,
            ScheduleValidator.validate(WeeklyAvailability()),
        )
    }

    @Test
    fun fromMustBeBeforeTo() {
        val week = WeeklyAvailability(
            days = listOf(DayAvailability("LUNES", listOf(TimeRange("18:00", "09:00")))),
        )
        assertEquals(ScheduleValidator.FROM_AFTER_TO, ScheduleValidator.validate(week))
    }

    @Test
    fun overlappingRangesOnSameDayAreRejected() {
        val week = WeeklyAvailability(
            days = listOf(
                DayAvailability(
                    "LUNES",
                    listOf(TimeRange("08:00", "12:00"), TimeRange("11:00", "14:00")),
                ),
            ),
        )
        assertEquals(ScheduleValidator.OVERLAP, ScheduleValidator.validate(week))
    }

    @Test
    fun defaultWeekIsValid() {
        assertNull(ScheduleValidator.validate(WeeklyAvailability.default()))
    }

    @Test
    fun adjacentRangesOnSameDayAreAllowed() {
        val week = WeeklyAvailability(
            days = listOf(
                DayAvailability(
                    "LUNES",
                    listOf(TimeRange("08:00", "12:00"), TimeRange("12:00", "14:00")),
                ),
            ),
        )
        assertNull(ScheduleValidator.validate(week))
    }
}
