package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.NotificationPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val EmptyUnreadCount: StateFlow<Int?> = MutableStateFlow<Int?>(null).asStateFlow()

interface NotificationsRepository {
    val unreadCount: StateFlow<Int?> get() = EmptyUnreadCount

    suspend fun registerDeviceToken(fcmToken: String, deviceId: String?): AppResult<Unit>
    suspend fun markAsRead(notificationId: Long): AppResult<Unit>
    suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage>

    /** Último conteo de no leídas que llegó bien. Null si todavía no hubo uno. */
    fun cachedUnreadCount(): Int? = unreadCount.value
}
