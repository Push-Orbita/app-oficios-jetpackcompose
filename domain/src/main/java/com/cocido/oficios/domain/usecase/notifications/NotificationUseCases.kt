package com.cocido.oficios.domain.usecase.notifications

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import javax.inject.Inject

class RegisterDeviceTokenUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) {
    suspend operator fun invoke(fcmToken: String, deviceId: String? = null): AppResult<Unit> =
        notificationsRepository.registerDeviceToken(fcmToken, deviceId)
}

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) {
    suspend operator fun invoke(notificationId: Long): AppResult<Unit> =
        notificationsRepository.markAsRead(notificationId)
}

class GetNotificationsUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) {
    suspend operator fun invoke(
        pageNumber: Int = 1,
        pageSize: Int = 20,
        soloNoLeidas: Boolean = false,
    ): AppResult<NotificationPage> =
        notificationsRepository.getNotifications(pageNumber, pageSize, soloNoLeidas)
}

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) {
    suspend operator fun invoke(): AppResult<Int> =
        when (val result = notificationsRepository.getNotifications(pageNumber = 1, pageSize = 50, soloNoLeidas = true)) {
            is AppResult.Success -> AppResult.Success(result.data.items.count { !it.isRead })
            is AppResult.Error -> result
        }
}
