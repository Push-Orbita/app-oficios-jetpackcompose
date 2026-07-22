package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult

interface NotificationsRepository {
    suspend fun registerDeviceToken(fcmToken: String, deviceId: String?): AppResult<Unit>
    suspend fun markAsRead(notificationId: Long): AppResult<Unit>
}
