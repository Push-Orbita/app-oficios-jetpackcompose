package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.availability.dto.BloqueoAgendaDto
import com.cocido.oficios.core.network.api.availability.dto.CrearBloqueoAgendaDto
import com.cocido.oficios.core.network.api.availability.dto.DiaDisponibilidadDto
import com.cocido.oficios.core.network.api.availability.dto.DiaDisponibilidadInputDto
import com.cocido.oficios.core.network.api.availability.dto.DisponibilidadDiaDto
import com.cocido.oficios.core.network.api.availability.dto.DisponibilidadSemanalDto
import com.cocido.oficios.core.network.api.availability.dto.RangoHorarioDto
import com.cocido.oficios.core.network.api.availability.dto.ReemplazarDisponibilidadDto
import com.cocido.oficios.domain.model.AgendaBlock
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.DayAvailability
import com.cocido.oficios.domain.model.DayAvailabilityWindow
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.toReplaceDays

fun DisponibilidadSemanalDto.toDomain(): WeeklyAvailability {
    val days = dias.mapNotNull { it.toDomain() }
        .sortedBy { WeeklyAvailability.ALL_DAYS.indexOf(it.day) }
    return WeeklyAvailability(
        days = days,
        includeSunday = days.any { it.day == WeeklyAvailability.SUNDAY },
    )
}

fun WeeklyAvailability.toReplaceDto(): ReemplazarDisponibilidadDto =
    ReemplazarDisponibilidadDto(
        dias = toReplaceDays().map { day ->
            DiaDisponibilidadInputDto(
                dia = day.day,
                rangos = day.ranges.map { it.toDto() },
            )
        },
    )

fun BloqueoAgendaDto.toDomain(): AgendaBlock = AgendaBlock(
    id = id,
    date = fecha,
    start = horaInicio?.let(TimeRangeValidation::normalize)?.takeIf { it.isNotBlank() },
    end = horaFin?.let(TimeRangeValidation::normalize)?.takeIf { it.isNotBlank() },
    reason = motivo?.trim()?.takeIf { it.isNotBlank() },
)

fun CreateAgendaBlockParams.toDto(): CrearBloqueoAgendaDto = CrearBloqueoAgendaDto(
    fecha = date,
    horaInicio = start?.takeIf { it.isNotBlank() },
    horaFin = end?.takeIf { it.isNotBlank() },
    motivo = reason?.trim()?.takeIf { it.isNotBlank() },
)

fun DisponibilidadDiaDto.toDomain(): DayAvailabilityWindow = DayAvailabilityWindow(
    date = fecha,
    day = dia,
    requiresTurns = requiereTurnos,
    ranges = franjas.map { it.toDomain() },
)

private fun DiaDisponibilidadDto.toDomain(): DayAvailability? {
    val key = dia.uppercase()
    if (key !in WeeklyAvailability.ALL_DAYS) return null
    val ranges = rangos.map { it.toDomain() }.filter { TimeRangeValidation.validateRange(it) == null }
    if (ranges.isEmpty()) return null
    return DayAvailability(day = key, ranges = ranges)
}

private fun RangoHorarioDto.toDomain() = TimeRange(
    start = TimeRangeValidation.normalize(horaInicio),
    end = TimeRangeValidation.normalize(horaFin),
)

private fun TimeRange.toDto() = RangoHorarioDto(horaInicio = start, horaFin = end)
