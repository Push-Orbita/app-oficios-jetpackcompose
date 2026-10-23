package com.cocido.oficios.core.network.api.availability.dto

import kotlinx.serialization.Serializable

@Serializable
data class RangoHorarioDto(
    val horaInicio: String,
    val horaFin: String,
)

@Serializable
data class DiaDisponibilidadDto(
    val dia: String,
    val rangos: List<RangoHorarioDto> = emptyList(),
)

@Serializable
data class DisponibilidadSemanalDto(
    val dias: List<DiaDisponibilidadDto> = emptyList(),
)

@Serializable
data class DiaDisponibilidadInputDto(
    val dia: String? = null,
    val diaInicio: String? = null,
    val diaFin: String? = null,
    val rangos: List<RangoHorarioDto> = emptyList(),
)

@Serializable
data class ReemplazarDisponibilidadDto(
    val dias: List<DiaDisponibilidadInputDto>,
)

@Serializable
data class BloqueoAgendaDto(
    val id: Long,
    val fecha: String,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val motivo: String? = null,
)

@Serializable
data class CrearBloqueoAgendaDto(
    val fecha: String,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val motivo: String? = null,
)

@Serializable
data class DisponibilidadDiaDto(
    val fecha: String,
    val dia: String,
    val requiereTurnos: Boolean = false,
    val franjas: List<RangoHorarioDto> = emptyList(),
)
