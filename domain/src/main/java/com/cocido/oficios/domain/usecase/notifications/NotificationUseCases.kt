package com.cocido.oficios.domain.usecase.notifications

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.MarkAllNotificationsReadResult
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
    fun lastKnown(): Int? = notificationsRepository.cachedUnreadCount()

    suspend operator fun invoke(): AppResult<Int> =
        when (val result = notificationsRepository.getNotifications(pageNumber = 1, pageSize = 50, soloNoLeidas = true)) {
            // metadata.count trae el total real; contar items de la página topeaba el badge en 50.
            is AppResult.Success -> AppResult.Success(
                result.data.totalCount ?: result.data.items.count { !it.isRead },
            )
            is AppResult.Error -> result
        }
}

/**
 * El backend no expone un endpoint de "marcar todas" (solo `PATCH /notifications/{id}/read`),
 * así que juntamos todas las no leídas paginando `soloNoLeidas=true` y marcamos una por una.
 * Primero se recolectan los ids y después se marcan, para no correr las páginas mientras se marca.
 */
class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) {
    suspend operator fun invoke(): AppResult<MarkAllNotificationsReadResult> {
        val unreadIds = LinkedHashSet<Long>()
        var pageNumber = 1
        while (pageNumber <= MAX_PAGES) {
            when (
                val result = notificationsRepository.getNotifications(
                    pageNumber = pageNumber,
                    pageSize = PAGE_SIZE,
                    soloNoLeidas = true,
                )
            ) {
                is AppResult.Success -> {
                    result.data.items.filterNot { it.isRead }.mapTo(unreadIds) { it.id }
                    if (!result.data.hasMore || result.data.items.isEmpty()) break
                    pageNumber++
                }
                is AppResult.Error -> if (unreadIds.isEmpty()) return result else break
            }
        }
        val marked = mutableListOf<Long>()
        val failed = mutableListOf<Long>()
        unreadIds.forEach { id ->
            when (notificationsRepository.markAsRead(id)) {
                is AppResult.Success -> marked += id
                is AppResult.Error -> failed += id
            }
        }
        return AppResult.Success(MarkAllNotificationsReadResult(markedIds = marked, failedIds = failed))
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val MAX_PAGES = 20
    }
}
