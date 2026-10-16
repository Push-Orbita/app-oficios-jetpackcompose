package com.cocido.oficios.core.network.api.social.dto

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class FavoritoItemDto(
    val id: Long,
    val trabajadorOficioId: Long,
    val displayName: String,
    val profilePhotoUrl: String? = null,
    val specialtyLabel: String? = null,
    val rating: Double? = null,
    val verified: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
data class FavoritoStatusDto(
    val isFavorite: Boolean,
    val id: Long? = null,
)

typealias FavoritosPageDto = PaginatedResponseDto<FavoritoItemDto>
