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
