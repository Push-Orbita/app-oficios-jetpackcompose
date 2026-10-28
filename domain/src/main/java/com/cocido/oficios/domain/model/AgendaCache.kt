package com.cocido.oficios.domain.model

data class AgendaSlotsSnapshot(
    val subRubroId: Long,
    val fromIso: String?,
    val toIso: String?,
    val slots: List<AgendaSlot>,
)

data class AgendaSessionsSnapshot(
    val subRubroId: Long,
    val sessions: List<AgendaSessionConfig>,
)
