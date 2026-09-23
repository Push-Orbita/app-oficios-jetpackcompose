package com.cocido.oficios.core.network.api.notifications.dto

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.catalog.dto.PaginationMetadataDto
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Long,
    val type: String? = null,
    val title: String? = null,
    val body: String? = null,
    val createdAt: String? = null,
    val readAt: String? = null,
    val leido: Boolean? = null,
    val entityType: String? = null,
    val entityId: Long? = null,
    val deepLink: String? = null,
)

typealias NotificationsPageDto = PaginatedResponseDto<NotificationDto>

@Serializable
data class MarkNotificationReadResponseDto(
    val leido: Boolean = false,
)

@Serializable
data class RegisterDeviceTokenRequestDto(
    val fcmToken: String,
    val platform: String = "android",
    val deviceId: String? = null,
    val deviceName: String? = null,
)
