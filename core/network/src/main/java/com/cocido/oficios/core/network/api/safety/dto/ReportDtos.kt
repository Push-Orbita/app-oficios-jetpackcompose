package com.cocido.oficios.core.network.api.safety.dto

import kotlinx.serialization.Serializable

@Serializable
data class DenunciaMotivoItemDto(
    val code: String,
    val label: String,
)

@Serializable
data class DenunciaMotivosResponseDto(
    val data: List<DenunciaMotivoItemDto> = emptyList(),
)

@Serializable
data class CreateDenunciaRequestDto(
    val targetType: String,
    val targetId: Long,
    val reasonCode: String,
    val description: String,
    val bookingId: Long? = null,
)

@Serializable
data class DenunciaDto(
    val id: Long,
    val status: String? = null,
    val createdAt: String? = null,
    val targetType: String? = null,
    val targetId: Long? = null,
    val reasonCode: String? = null,
)
