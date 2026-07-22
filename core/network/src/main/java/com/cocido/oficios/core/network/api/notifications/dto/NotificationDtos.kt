package com.cocido.oficios.core.network.api.notifications.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequestDto(
    val fcmToken: String,
    val platform: String = "android",
    val deviceId: String? = null,
    val deviceName: String? = null,
)
