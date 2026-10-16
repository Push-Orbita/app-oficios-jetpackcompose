package com.cocido.oficios.core.network.api.account.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequestDto(
    val email: String,
    val contrasena: String,
    val confirmarContrasena: String,
)

@Serializable
data class WorkGalleryDto(
    val id: Long,
    val urlArchivo: String? = null,
)

@Serializable
data class CreateAccountDeletionRequestDto(
    val currentPassword: String,
    val reasonCode: String,
    val confirmation: String,
)

@Serializable
data class AccountDeletionRequestDto(
    val requestId: Long,
    val status: String,
    val requestedAt: String? = null,
    val scheduledFor: String? = null,
    val canCancelUntil: String? = null,
)
