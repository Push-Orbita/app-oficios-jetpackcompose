package com.cocido.oficios.domain.mapper

import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.resolvedSchedule
import com.cocido.oficios.domain.model.toFlatTimeSlots

fun RegisterTradeDraft.toProfessionalInfoRequest() = ProviderProfessionalInfoRequest(
    descripcion = otherSpecialization ?: rubroName,
    experiencia = experienceRange,
)

fun RegisterTradeDraft.toRubroFullRequest(): ProviderRubroFullRequest {
    val schedule = resolvedSchedule()
    return ProviderRubroFullRequest(
        rubroId = rubroId,
        subRubroIds = subRubroIds,
        servesAtClientHome = servesAtClientHome,
        servesAtLocal = servesAtLocal,
        workDays = schedule.activeDays.ifEmpty { DEFAULT_WORK_DAYS },
        requiresTurns = requiresTurns,
        turnDurationMinutes = turnDurationMinutes,
        turnIntervalMinutes = turnIntervalMinutes,
        plazoCancelacionHoras = plazoCancelacionHoras,
        timeSlots = schedule.toFlatTimeSlots(),
        weeklyAvailability = schedule,
        domicilioLocal = if (servesAtLocal && barrioId != null) {
            com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest(
                barrioId = barrioId,
                nombre = "Local",
            )
        } else {
            null
        },
        paymentMethods = paymentMethods,
    )
}

private val DEFAULT_WORK_DAYS = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")
