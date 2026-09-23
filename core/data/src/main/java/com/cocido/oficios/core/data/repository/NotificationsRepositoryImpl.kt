package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.NotificationMapper.toDomain
import com.cocido.oficios.core.data.remote.NotificationsRemoteDataSource
import com.cocido.oficios.domain.repository.NotificationsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationsRemoteDataSource,
) : NotificationsRepository {

    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) =
        remoteDataSource.registerDeviceToken(fcmToken, deviceId).map { }

    override suspend fun markAsRead(notificationId: Long) =
        remoteDataSource.markAsRead(notificationId).map { }

    override suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ) = remoteDataSource.getNotifications(pageNumber, pageSize, soloNoLeidas).map { it.toDomain() }
}
