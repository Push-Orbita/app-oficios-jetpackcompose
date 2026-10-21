package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.cache.CachedValue
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.mapper.NotificationMapper.toDomain
import com.cocido.oficios.core.data.remote.NotificationsRemoteDataSource
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationsRemoteDataSource,
    coordinator: SessionCacheCoordinator,
) : NotificationsRepository {

    private val unreadCache = CachedValue<Int>(coordinator)
    override val unreadCount: StateFlow<Int?> = unreadCache.state

    override fun cachedUnreadCount(): Int? = unreadCache.get()

    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) =
        remoteDataSource.registerDeviceToken(fcmToken, deviceId).map { }

    override suspend fun markAsRead(notificationId: Long): AppResult<Unit> {
        val previous = unreadCache.get()
        previous?.let { unreadCache.set((it - 1).coerceAtLeast(0)) }
        return when (val result = remoteDataSource.markAsRead(notificationId).map { }) {
            is AppResult.Success -> result
            is AppResult.Error -> {
                unreadCache.set(previous)
                result
            }
        }
    }

    override suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage> {
        val result = remoteDataSource.getNotifications(pageNumber, pageSize, soloNoLeidas).map { it.toDomain() }
        if (result is AppResult.Success && pageNumber == 1 && soloNoLeidas) {
            unreadCache.set(result.data.items.count { !it.isRead })
        }
        return result
    }
}
