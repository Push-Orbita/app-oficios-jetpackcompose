package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.notifications.NotificationsApi
import com.cocido.oficios.core.network.api.notifications.dto.RegisterDeviceTokenRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRemoteDataSource @Inject constructor(
    private val notificationsApi: NotificationsApi,
) {
    suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = safeApiCall {
        notificationsApi.registerDeviceToken(
            RegisterDeviceTokenRequestDto(
                fcmToken = fcmToken,
                platform = "android",
                deviceId = deviceId,
            ),
        )
    }

    suspend fun markAsRead(notificationId: Long) = safeApiCall {
        notificationsApi.markAsRead(notificationId)
    }
}
