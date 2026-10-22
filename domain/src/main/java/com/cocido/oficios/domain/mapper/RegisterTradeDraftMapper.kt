package com.cocido.oficios.domain.mapper

import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft

fun RegisterTradeDraft.toProfessionalInfoRequest() = ProviderProfessionalInfoRequest(
    descripcion = otherSpecialization ?: rubroName,
    experiencia = experienceRange,
)

fun RegisterTradeDraft.toRubroFullRequest() = ProviderRubroFullRequest(
    rubroId = rubroId,
    subRubroIds = subRubroIds,
    servesAtClientHome = servesAtClientHome,
    servesAtLocal = servesAtLocal,
    workDays = workDays.ifEmpty { DEFAULT_WORK_DAYS },
    requiresTurns = requiresTurns,
    timeSlots = timeSlots.ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) },
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

private val DEFAULT_WORK_DAYS = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")
