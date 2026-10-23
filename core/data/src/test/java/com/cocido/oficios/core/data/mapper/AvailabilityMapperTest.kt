package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.availability.dto.BloqueoAgendaDto
import com.cocido.oficios.core.network.api.availability.dto.DiaDisponibilidadDto
import com.cocido.oficios.core.network.api.availability.dto.DisponibilidadDiaDto
import com.cocido.oficios.core.network.api.availability.dto.DisponibilidadSemanalDto
import com.cocido.oficios.core.network.api.availability.dto.RangoHorarioDto
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.DayAvailability
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.WeeklyAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityMapperTest {

    @Test
    fun weeklyDtoMapsRangesAndSundayFlag() {
        val week = DisponibilidadSemanalDto(
            dias = listOf(
                DiaDisponibilidadDto(
                    dia = "LUNES",
                    rangos = listOf(
                        RangoHorarioDto("08:00:00", "12:00"),
                        RangoHorarioDto("14:00", "18:00"),
                    ),
                ),
                DiaDisponibilidadDto(
                    dia = "DOMINGO",
                    rangos = listOf(RangoHorarioDto("10:00", "13:00")),
                ),
            ),
        ).toDomain()
        assertEquals(listOf("LUNES", "DOMINGO"), week.activeDays)
        assertTrue(week.includeSunday)
        assertEquals("08:00", week.rangesOn("LUNES").first().start)
        assertEquals("12:00", week.rangesOn("LUNES").first().end)
    }

    @Test
    fun replaceDtoOmitsSundayWhenNotIncluded() {
        val dto = WeeklyAvailability(
            days = listOf(
                DayAvailability("LUNES", listOf(TimeRange("09:00", "18:00"))),
                DayAvailability("DOMINGO", listOf(TimeRange("10:00", "13:00"))),
            ),
            includeSunday = false,
        ).toReplaceDto()
        assertEquals(1, dto.dias.size)
        assertEquals("LUNES", dto.dias.single().dia)
        assertEquals("09:00", dto.dias.single().rangos.single().horaInicio)
    }

    @Test
    fun blockMapsFullDayAndRange() {
        val fullDay = BloqueoAgendaDto(id = 1, fecha = "2026-12-25").toDomain()
        assertTrue(fullDay.isFullDay)
        assertNull(fullDay.start)
        val range = BloqueoAgendaDto(
            id = 2,
            fecha = "2026-12-24",
            horaInicio = "14:00:00",
            horaFin = "18:00",
            motivo = "Feriado",
        ).toDomain()
        assertFalse(range.isFullDay)
        assertEquals("14:00", range.start)
        assertEquals("Feriado", range.reason)
    }

    @Test
    fun createBlockDtoDropsBlankHours() {
        val dto = CreateAgendaBlockParams(
            subRubroId = 9,
            date = "2026-12-24",
            start = "",
            end = " ",
            reason = "  ",
        ).toDto()
        assertEquals("2026-12-24", dto.fecha)
        assertNull(dto.horaInicio)
        assertNull(dto.horaFin)
        assertNull(dto.motivo)
    }

    @Test
    fun dayAvailabilityMapsFranjas() {
        val window = DisponibilidadDiaDto(
            fecha = "2026-10-26",
            dia = "LUNES",
            requiereTurnos = true,
            franjas = listOf(RangoHorarioDto("08:00", "12:00"), RangoHorarioDto("16:00", "20:00")),
        ).toDomain()
        assertEquals("2026-10-26", window.date)
        assertTrue(window.requiresTurns)
        assertEquals(2, window.ranges.size)
        assertEquals("16:00", window.ranges.last().start)
        assertEquals("20:00", window.ranges.last().end)
    }
}
