package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.remote.NotificationsRemoteDataSource
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.catalog.dto.PaginationMetadataDto
import com.cocido.oficios.core.network.api.notifications.NotificationsApi
import com.cocido.oficios.core.network.api.notifications.dto.MarkNotificationReadResponseDto
import com.cocido.oficios.core.network.api.notifications.dto.NotificationDto
import com.cocido.oficios.core.network.api.notifications.dto.NotificationsPageDto
import com.cocido.oficios.core.network.api.notifications.dto.RegisterDeviceTokenRequestDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsRepositoryImplTest {

    @Test
    fun `mark as read decrements the unread cache`() = runTest {
        val api = FakeNotificationsApi()
        val repository = NotificationsRepositoryImpl(
            NotificationsRemoteDataSource(api),
            SessionCacheCoordinator(),
        )
        val page = repository.getNotifications(pageNumber = 1, pageSize = 50, soloNoLeidas = true)
        assertTrue(page is AppResult.Success)
        assertEquals(2, repository.unreadCount.value)

        assertTrue(repository.markAsRead(1L) is AppResult.Success)
        assertEquals(1, repository.unreadCount.value)
    }
}

private class FakeNotificationsApi : NotificationsApi {
    override suspend fun registerDeviceToken(request: RegisterDeviceTokenRequestDto) = error("unused")

    override suspend fun markAsRead(notificationId: Long) = MarkNotificationReadResponseDto(leido = true)

    override suspend fun getNotifications(
        pageSize: Int,
        pageNumber: Int,
        soloNoLeidas: Boolean?,
    ): NotificationsPageDto = PaginatedResponseDto(
        data = listOf(
            NotificationDto(id = 1L, title = "A", leido = false),
            NotificationDto(id = 2L, title = "B", leido = false),
        ),
        metadata = PaginationMetadataDto(pageNumber = pageNumber, totalPages = 1),
    )
}
