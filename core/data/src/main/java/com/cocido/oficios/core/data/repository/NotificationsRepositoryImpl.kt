package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.cache.CachedValue
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.mapper.NotificationMapper.toDomain
import com.cocido.oficios.core.data.remote.NotificationsRemoteDataSource
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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

    override suspend fun markManyAsRead(notificationIds: List<Long>): List<Long> {
        val uniqueIds = notificationIds.distinct()
        if (uniqueIds.isEmpty()) return emptyList()

        val previous = unreadCache.get()
        previous?.let { unreadCache.set((it - uniqueIds.size).coerceAtLeast(0)) }

        val failed = ConcurrentLinkedQueue<Long>()
        coroutineScope {
            val semaphore = Semaphore(MARK_ALL_PARALLELISM)
            uniqueIds.map { id ->
                async {
                    semaphore.withPermit {
                        when (remoteDataSource.markAsRead(id)) {
                            is AppResult.Success -> Unit
                            is AppResult.Error -> failed.add(id)
                        }
                    }
                }
            }.awaitAll()
        }

        val failedIds = failed.toList()
        previous?.let {
            val successCount = uniqueIds.size - failedIds.size
            unreadCache.set((it - successCount).coerceAtLeast(0))
        }
        return failedIds
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

    private companion object {
        const val MARK_ALL_PARALLELISM = 4
    }
}
