package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.profile.dto.DiaLaboralSimpleDto
import com.cocido.oficios.domain.model.DayAvailability
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.toFlatTimeSlots

fun TrabajadorOficioPerfilMeDto.toRegisterTradeDraft(): RegisterTradeDraft? {
    val principalRubro = rubros.firstOrNull { it.principal } ?: rubros.firstOrNull() ?: return null
    val rubro = principalRubro.rubro ?: return null
    val category = rubro.categoria ?: return null
    val subRubroIds = principalRubro.subRubros.mapNotNull { it.subRubro?.id }
    if (subRubroIds.isEmpty()) return null

    val subRubros = principalRubro.subRubros
    val domicilio = domicilios.firstOrNull()
    val weeklySchedule = principalRubro.diasLaborales.toWeeklySchedule()
    val workDays = weeklySchedule?.activeDays ?: principalRubro.diasLaborales.expandWorkDays()
    val timeSlots = weeklySchedule?.toFlatTimeSlots() ?: principalRubro.diasLaborales.extractTimeSlots()

    return RegisterTradeDraft(
        categoryId = category.id,
        categoryName = category.nombre,
        rubroId = rubro.id,
        rubroName = rubro.nombre,
        subRubroIds = subRubroIds,
        otherSpecialization = descripcion,
        experienceRange = experiencia.orEmpty(),
        neighborhoodQuery = domicilio?.barrio?.nombre ?: domicilio?.calle.orEmpty(),
        barrioId = domicilio?.barrioId ?: domicilio?.barrio?.id,
        workDays = workDays,
        servesAtClientHome = subRubros.any { it.lugarDomicilio },
        servesAtLocal = subRubros.any { it.lugarLocal },
        requiresTurns = subRubros.any { it.requiereTurnos },
        timeSlots = timeSlots,
        weeklySchedule = weeklySchedule,
        paymentMethods = metodosPago.ifEmpty { emptyList() },
    )
}

private val WEEK_DAYS = listOf(
    "LUNES",
    "MARTES",
    "MIERCOLES",
    "JUEVES",
    "VIERNES",
    "SABADO",
    "DOMINGO",
)

private fun List<DiaLaboralSimpleDto>.expandWorkDays(): List<String> {
    if (isEmpty()) return emptyList()
    val selected = linkedSetOf<String>()
    for (dia in this) {
        val start = WEEK_DAYS.indexOf(dia.diaInicio.uppercase())
        val end = WEEK_DAYS.indexOf(dia.diaFin.uppercase())
        when {
            start >= 0 && end >= 0 && start <= end -> {
                for (index in start..end) {
                    selected.add(WEEK_DAYS[index])
                }
            }
            start >= 0 -> selected.add(WEEK_DAYS[start])
            else -> selected.add(dia.diaInicio.uppercase())
        }
    }
    return selected.toList()
}

private fun List<DiaLaboralSimpleDto>.extractTimeSlots(): List<RegisterTradeTimeSlotDraft> {
    val slots = flatMap { dia ->
        dia.horariosLaborales.map { horario ->
            RegisterTradeTimeSlotDraft(
                from = horario.horaInicio,
                to = horario.horaFin,
            )
        }
    }.distinctBy { "${it.from}-${it.to}" }
    return slots.ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) }
}

private fun List<DiaLaboralSimpleDto>.toWeeklySchedule(): WeeklyAvailability? {
    if (isEmpty()) return null
    val byDay = linkedMapOf<String, MutableList<TimeRange>>()
    for (dia in this) {
        val start = WEEK_DAYS.indexOf(dia.diaInicio.uppercase())
        val end = WEEK_DAYS.indexOf(dia.diaFin.uppercase())
        val days = when {
            start >= 0 && end >= 0 && start <= end -> WEEK_DAYS.subList(start, end + 1)
            start >= 0 -> listOf(WEEK_DAYS[start])
            else -> listOf(dia.diaInicio.uppercase())
        }
        val ranges = dia.horariosLaborales.map { horario ->
            TimeRange(
                TimeRangeValidation.normalize(horario.horaInicio),
                TimeRangeValidation.normalize(horario.horaFin),
            )
        }.ifEmpty { listOf(TimeRange("09:00", "18:00")) }
        for (day in days) {
            byDay.getOrPut(day) { mutableListOf() }.addAll(ranges)
        }
    }
    val days = WEEK_DAYS.mapNotNull { day ->
        val ranges = byDay[day]?.distinct() ?: return@mapNotNull null
        if (ranges.isEmpty()) null else DayAvailability(day, ranges)
    }
    if (days.isEmpty()) return null
    return WeeklyAvailability(
        days = days,
        includeSunday = days.any { it.day == WeeklyAvailability.SUNDAY },
    )
}
