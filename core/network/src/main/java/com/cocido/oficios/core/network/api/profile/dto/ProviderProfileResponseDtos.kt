package com.cocido.oficios.core.network.api.profile.dto

import com.cocido.oficios.core.network.api.catalog.dto.BarrioDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import kotlinx.serialization.Serializable

@Serializable
data class TrabajadorOficioRubroFullDto(
    val id: Long,
    val rubro: RubroDto? = null,
    val principal: Boolean = false,
    val subRubros: List<TrabajadorOficioSubRubroFullDto> = emptyList(),
    val diasLaborales: List<DiaLaboralSimpleDto> = emptyList(),
)

@Serializable
data class TrabajadorOficioSubRubroFullDto(
    val id: Long,
    val subRubro: SubRubroDto? = null,
    val lugarDomicilio: Boolean = false,
    val lugarLocal: Boolean = false,
    val requiereTurnos: Boolean = false,
)

@Serializable
data class DiaLaboralSimpleDto(
    val id: Long? = null,
    val diaInicio: String,
    val diaFin: String,
    val horariosLaborales: List<HorarioLaboralSimpleDto> = emptyList(),
)

@Serializable
data class HorarioLaboralSimpleDto(
    val id: Long? = null,
    val horaInicio: String,
    val horaFin: String,
)

@Serializable
data class ProviderDomicilioDto(
    val id: Long? = null,
    val barrioId: Long? = null,
    val calle: String? = null,
    val nombre: String? = null,
    val barrio: BarrioDto? = null,
)
