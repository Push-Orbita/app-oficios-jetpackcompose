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
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay
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

    @Test
    fun `mark many as read decrements unread once and returns failed ids`() = runTest {
        val api = FakeNotificationsApi(failIds = setOf(2L, 4L))
        val repository = NotificationsRepositoryImpl(
            NotificationsRemoteDataSource(api),
            SessionCacheCoordinator(),
        )
        repository.getNotifications(pageNumber = 1, pageSize = 50, soloNoLeidas = true)
        // seed unread higher than page for regression of partial revert
        assertEquals(2, repository.unreadCount.value)

        val failed = repository.markManyAsRead(listOf(1L, 2L, 3L, 4L, 5L))
        assertEquals(listOf(2L, 4L), failed.sorted())
        // previous=2, 3 éxitos → contador en 0 (no sobrecuenta por los fallidos)
        assertEquals(0, repository.unreadCount.value)
        assertEquals(5, api.markCalls.get())
    }

    @Test
    fun `mark many as read caps parallel calls`() = runTest {
        val api = FakeNotificationsApi(markDelayMs = 30)
        val repository = NotificationsRepositoryImpl(
            NotificationsRemoteDataSource(api),
            SessionCacheCoordinator(),
        )
        repository.getNotifications(pageNumber = 1, pageSize = 50, soloNoLeidas = true)

        val failed = repository.markManyAsRead(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L))
        assertTrue(failed.isEmpty())
        assertTrue(api.maxInFlight.get() <= 4)
        assertEquals(0, repository.unreadCount.value)
    }
}

private class FakeNotificationsApi(
    private val failIds: Set<Long> = emptySet(),
    private val markDelayMs: Long = 0,
) : NotificationsApi {
    val markCalls = AtomicInteger(0)
    val maxInFlight = AtomicInteger(0)
    private val inFlight = AtomicInteger(0)

    override suspend fun registerDeviceToken(request: RegisterDeviceTokenRequestDto) = error("unused")

    override suspend fun markAsRead(notificationId: Long): MarkNotificationReadResponseDto {
        markCalls.incrementAndGet()
        val current = inFlight.incrementAndGet()
        maxInFlight.updateAndGet { maxOf(it, current) }
        try {
            if (markDelayMs > 0) delay(markDelayMs)
            if (notificationId in failIds) throw IOException("fail $notificationId")
            return MarkNotificationReadResponseDto(leido = true)
        } finally {
            inFlight.decrementAndGet()
        }
    }

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
