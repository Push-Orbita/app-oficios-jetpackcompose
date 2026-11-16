package com.cocido.oficios.feature.home.notifications

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.notifications.GetNotificationsUseCase
import com.cocido.oficios.domain.usecase.notifications.MarkManyNotificationsReadUseCase
import com.cocido.oficios.domain.usecase.notifications.MarkNotificationReadUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `mark all as read updates items and unread count optimistically`() = runTest(testDispatcher) {
        val repository = FakeNotificationsRepository(
            items = listOf(
                notification(1, isRead = false),
                notification(2, isRead = false),
                notification(3, isRead = true),
            ),
            unread = 2,
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(NotificationsEvent.MarkAllAsRead)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.all { it.isRead })
        assertFalse(viewModel.uiState.value.isMarkingAllRead)
        assertFalse(viewModel.uiState.value.hasUnread)
        assertEquals(0, repository.unreadCount.value)
        assertEquals(listOf(1L, 2L), repository.markedIds)
    }

    @Test
    fun `mark all as read reverts only failed ids and emits retry effect`() = runTest(testDispatcher) {
        val repository = FakeNotificationsRepository(
            items = listOf(
                notification(1, isRead = false),
                notification(2, isRead = false),
                notification(3, isRead = false),
            ),
            unread = 3,
            failIds = setOf(2L),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val effects = mutableListOf<NotificationsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(NotificationsEvent.MarkAllAsRead)
        advanceUntilIdle()
        job.cancel()

        val items = viewModel.uiState.value.items.associateBy { it.id }
        assertTrue(items.getValue(1).isRead)
        assertFalse(items.getValue(2).isRead)
        assertTrue(items.getValue(3).isRead)
        assertEquals(1, repository.unreadCount.value)
        assertTrue(
            effects.any {
                it is NotificationsEffect.ShowMarkAllError && it.failedIds.toSet() == setOf(2L)
            },
        )
    }

    @Test
    fun `retry mark failed only retries those ids`() = runTest(testDispatcher) {
        val repository = FakeNotificationsRepository(
            items = listOf(
                notification(1, isRead = false),
                notification(2, isRead = false),
            ),
            unread = 2,
            failIds = setOf(2L),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(NotificationsEvent.MarkAllAsRead)
        advanceUntilIdle()
        repository.failIds = emptySet()
        repository.markedIds.clear()

        viewModel.onEvent(NotificationsEvent.RetryMarkFailed(listOf(2L)))
        advanceUntilIdle()

        assertEquals(listOf(2L), repository.markedIds)
        assertTrue(viewModel.uiState.value.items.all { it.isRead })
        assertEquals(0, repository.unreadCount.value)
    }

    @Test
    fun `mark all ignores double submit while in flight`() = runTest(testDispatcher) {
        val repository = FakeNotificationsRepository(
            items = listOf(notification(1, isRead = false), notification(2, isRead = false)),
            unread = 2,
            markDelayMs = 50,
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(NotificationsEvent.MarkAllAsRead)
        viewModel.onEvent(NotificationsEvent.MarkAllAsRead)
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L), repository.markedIds)
    }

    @Test
    fun `mark all with unread filter removes items optimistically`() = runTest(testDispatcher) {
        val repository = FakeNotificationsRepository(
            items = listOf(notification(1, isRead = false), notification(2, isRead = false)),
            unread = 2,
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(NotificationsEvent.ToggleUnreadFilter)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.soloNoLeidas)

        viewModel.onEvent(NotificationsEvent.MarkAllAsRead)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertFalse(viewModel.uiState.value.showMarkAllAction)
    }

    private fun createViewModel(repository: FakeNotificationsRepository) = NotificationsViewModel(
        getNotificationsUseCase = GetNotificationsUseCase(repository),
        markNotificationReadUseCase = MarkNotificationReadUseCase(repository),
        markManyNotificationsReadUseCase = MarkManyNotificationsReadUseCase(repository),
        orderUseCases = OrderUseCases(UnusedOrderRepository()),
    )

    private fun notification(id: Long, isRead: Boolean) = NotificationItem(
        id = id,
        type = "info",
        title = "Aviso $id",
        body = "Texto",
        createdAtLabel = "hoy",
        isRead = isRead,
        entityType = null,
        entityId = null,
        deepLink = null,
    )
}

private class FakeNotificationsRepository(
    private val items: List<NotificationItem>,
    unread: Int,
    var failIds: Set<Long> = emptySet(),
    private val markDelayMs: Long = 0,
) : NotificationsRepository {
    val markedIds = mutableListOf<Long>()
    private val unreadState = MutableStateFlow<Int?>(unread)
    override val unreadCount: StateFlow<Int?> = unreadState

    override fun cachedUnreadCount(): Int? = unreadState.value

    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = AppResult.Success(Unit)

    override suspend fun markAsRead(notificationId: Long): AppResult<Unit> {
        if (markDelayMs > 0) delay(markDelayMs)
        markedIds += notificationId
        if (notificationId in failIds) return AppResult.Error(AppError.Network("fail"))
        unreadState.value = (unreadState.value ?: 0).minus(1).coerceAtLeast(0)
        return AppResult.Success(Unit)
    }

    override suspend fun markManyAsRead(notificationIds: List<Long>): List<Long> {
        val previous = unreadState.value
        previous?.let { unreadState.value = (it - notificationIds.size).coerceAtLeast(0) }
        val failed = mutableListOf<Long>()
        for (id in notificationIds) {
            if (markDelayMs > 0) delay(markDelayMs)
            markedIds += id
            if (id in failIds) failed += id
        }
        previous?.let {
            val successCount = notificationIds.size - failed.size
            unreadState.value = (it - successCount).coerceAtLeast(0)
        }
        return failed
    }

    override suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage> {
        val filtered = if (soloNoLeidas) items.filter { !it.isRead } else items
        return AppResult.Success(
            NotificationPage(
                items = filtered,
                pageNumber = 1,
                totalPages = 1,
                hasMore = false,
            ),
        )
    }
}

private class UnusedOrderRepository : OrderRepository {
    override val ordersChanged: SharedFlow<Unit> = MutableSharedFlow()
    private fun unused(): Nothing = error("unused")
    override suspend fun getWorkerOrders() = unused()
    override suspend fun getClientOrders() = unused()
    override suspend fun createServiceRequest(params: com.cocido.oficios.domain.model.CreateServiceRequestParams) = unused()
    override suspend fun getOrderDetail(orderId: Long) = unused()
    override suspend fun acceptOrder(orderId: Long) = unused()
    override suspend fun rejectOrder(orderId: Long, reason: String) = unused()
    override suspend fun sendCounterOffer(
        orderId: Long,
        newStartIso: String,
        newEndIso: String,
        newPrice: Double?,
    ) = unused()
    override suspend fun acceptCounterOffer(counterOfferId: Long) = unused()
    override suspend fun rejectCounterOffer(counterOfferId: Long) = unused()
    override suspend fun cancelOrderBeforeAcceptance(orderId: Long) = unused()
    override suspend fun cancelConfirmedOrder(orderId: Long, reason: String?) = unused()
    override suspend fun getBookingTracking(bookingId: Long) = unused()
    override suspend fun findOrderIdForBooking(bookingId: Long) = unused()
    override suspend fun markEnRoute(bookingId: Long, location: com.cocido.oficios.domain.model.GeoPoint) = unused()
    override suspend fun markArrived(bookingId: Long, location: com.cocido.oficios.domain.model.GeoPoint) = unused()
    override suspend fun getCompletionCode(bookingId: Long) = unused()
    override suspend fun resendCompletionCode(bookingId: Long) = unused()
    override suspend fun finalizeBooking(bookingId: Long, completionCode: String) = unused()
    override suspend fun submitRating(
        bookingId: Long,
        quality: Int,
        punctuality: Int,
        comment: String?,
    ) = unused()
}
