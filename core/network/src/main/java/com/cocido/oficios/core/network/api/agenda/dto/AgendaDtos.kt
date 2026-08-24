package com.cocido.oficios.core.network.api.agenda.dto

import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import kotlinx.serialization.Serializable

@Serializable
data class TrabajadorOficioSubRubroAgendaDto(
    val id: Long,
    val subRubro: SubRubroDto? = null,
    val requiereTurnos: Boolean = false,
)

@Serializable
data class SlotTurnoAgendaDto(
    val id: Long,
    val trabajadorOficioSubRubro: TrabajadorOficioSubRubroAgendaDto? = null,
    val inicio: String,
    val fin: String,
    val estado: String,
    val autoAceptacion: Boolean = false,
)

@Serializable
data class SlotsAgrupadoPorTrabajadorDto(
    val slotsTurnos: List<SlotTurnoAgendaDto> = emptyList(),
)

@Serializable
data class CreateSlotTurnoRequestDto(
    val trabajadorOficioSubRubroId: Long,
    val inicio: String,
    val fin: String,
    val autoAceptacion: Boolean,
)

@Serializable
data class SesionDto(
    val id: Long,
    val duracion: Int,
    val medidaDuracion: String,
    val intervalo: String? = null,
    val medidaIntervalo: String? = null,
    val trabajadorOficioSubRubroId: Long,
)

@Serializable
data class CreateSesionRequestDto(
    val trabajadorOficioSubRubroId: Long,
    val duracion: Int,
    val medidaDuracion: String,
    val intervalo: String? = null,
    val medidaIntervalo: String? = null,
)
