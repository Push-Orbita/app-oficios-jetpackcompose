package com.cocido.oficios.core.network.api.search.dto

import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WorkerSearchItemDto(
    val id: Long,
    val estado: String,
    val documento: String? = null,
    val descripcion: String? = null,
    val experiencia: String? = null,
    val usuario: UsuarioDto? = null,
    val rubros: List<TrabajadorOficioRubroFullDto> = emptyList(),
    val reputacion: ReputacionResumenDto? = null,
)

@Serializable
data class ReputacionResumenDto(
    val promedio: Double? = null,
    val cantidadCalificaciones: Int? = null,

    @SerialName("puntajeGlobal")
    val puntajeGlobal: Double? = null,

    @SerialName("comentariosTotales")
    val comentariosTotales: Int? = null,
)
