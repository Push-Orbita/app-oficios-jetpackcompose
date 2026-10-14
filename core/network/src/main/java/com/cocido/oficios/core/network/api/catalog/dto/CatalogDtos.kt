package com.cocido.oficios.core.network.api.catalog.dto

import com.cocido.oficios.core.network.serialization.FlexibleLocalidadNameSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseDto<T>(
    val data: List<T> = emptyList(),
    val metadata: PaginationMetadataDto? = null,
)

@Serializable
data class PaginationMetadataDto(
    val count: Int? = null,
    val pageSize: Int? = null,
    val pageNumber: Int? = null,
    val totalPages: Int? = null,
)

@Serializable
data class CategoriaDto(
    val id: Long = 0L,
    val nombre: String = "",
    val iconUrl: String? = null,
)

@Serializable
data class RubroDto(
    val id: Long = 0L,
    val nombre: String = "",
    val categoria: CategoriaDto? = null,
)

@Serializable
data class SubRubroDto(
    val id: Long = 0L,
    val nombre: String = "",
    val rubro: RubroDto? = null,
)

@Serializable
data class BarrioDto(
    val id: Long,
    val nombre: String,
    @Serializable(with = FlexibleLocalidadNameSerializer::class)
    val localidad: String? = null,
)
