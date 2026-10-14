package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.NotificationPage

interface NotificationsRepository {
    suspend fun registerDeviceToken(fcmToken: String, deviceId: String?): AppResult<Unit>
    suspend fun markAsRead(notificationId: Long): AppResult<Unit>
    suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage>

    /** Último conteo de no leídas que llegó bien. Null si todavía no hubo uno. */
    fun cachedUnreadCount(): Int? = null
}
