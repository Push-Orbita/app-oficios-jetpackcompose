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

class MarkManyNotificationsReadUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) {
    /** Devuelve los IDs que no se pudieron marcar como leídos. */
    suspend operator fun invoke(notificationIds: List<Long>): List<Long> =
        notificationsRepository.markManyAsRead(notificationIds)
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
    val unreadCount get() = notificationsRepository.unreadCount

    fun lastKnown(): Int? = notificationsRepository.cachedUnreadCount()

    suspend operator fun invoke(): AppResult<Int> =
        when (val result = notificationsRepository.getNotifications(pageNumber = 1, pageSize = 50, soloNoLeidas = true)) {
            is AppResult.Success -> AppResult.Success(result.data.items.count { !it.isRead })
            is AppResult.Error -> result
        }
}
