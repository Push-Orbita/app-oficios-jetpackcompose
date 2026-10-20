package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.NotificationMapper.toDomain
import com.cocido.oficios.core.data.remote.NotificationsRemoteDataSource
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationsRemoteDataSource,
) : NotificationsRepository {

    @Volatile
    private var cachedUnread: Int? = null

    override fun cachedUnreadCount(): Int? = cachedUnread

    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) =
        remoteDataSource.registerDeviceToken(fcmToken, deviceId).map { }

    override suspend fun markAsRead(notificationId: Long): AppResult<Unit> {
        val result = remoteDataSource.markAsRead(notificationId).map { }
        if (result is AppResult.Success) {
            cachedUnread = cachedUnread?.let { (it - 1).coerceAtLeast(0) }
        }
        return result
    }

    override suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage> {
        val result = remoteDataSource.getNotifications(pageNumber, pageSize, soloNoLeidas).map { it.toDomain() }
        if (soloNoLeidas && result is AppResult.Success) {
            // metadata.count es el total real de no leídas; la página solo trae pageSize items.
            cachedUnread = result.data.totalCount ?: result.data.items.count { !it.isRead }
        }
        return result
    }
}
