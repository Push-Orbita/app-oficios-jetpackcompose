package com.cocido.oficios.domain.model

/** Disponibilidad de un día concreto para el cliente (GET /trabajador-oficio/{id}/disponibilidad). */
data class DayAvailabilityWindow(
    val date: String,
    val day: String,
    val requiresTurns: Boolean,
    val ranges: List<TimeRange>,
)
