package com.cocido.oficios.domain.model

import java.time.LocalDateTime

enum class AgendaSlotStatus { DISPONIBLE, RESERVADO, BLOQUEADO }

data class AgendaSlot(
    val id: Long,
    val subRubroId: Long,
    val subRubroLabel: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val status: AgendaSlotStatus,
    val autoAcceptance: Boolean,
)

data class AgendaSessionConfig(
    val id: Long,
    val subRubroId: Long,
    val durationMinutes: Int,
    val intervalMinutes: Int?,
)

/** Sub-rubro ofrecido por el trabajador logueado, con su trabajadorOficioSubRubroId real. */
data class ProviderSubRubroOption(
    val id: Long,
    val subRubroLabel: String,
    val rubroLabel: String,
    val requiresTurns: Boolean,
)

data class CreateAgendaSlotParams(
    val subRubroId: Long,
    val startIso: String,
    val endIso: String,
    val autoAcceptance: Boolean,
)

data class SaveAgendaSessionParams(
    val subRubroId: Long,
    val durationMinutes: Int,
    val intervalMinutes: Int?,
)
