package com.cocido.oficios.domain.usecase.notifications

import com.cocido.oficios.core.common.result.AppResult
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
