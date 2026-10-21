package com.cocido.oficios.feature.home

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.usecase.GetClientHomeUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.domain.usecase.search.GetRecentSearchesUseCase
import com.cocido.oficios.domain.usecase.search.ObserveRecentSearchesUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HomeViewModelTest {

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
    fun `load content emits success state`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository()
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Success)
        val content = (viewModel.uiState.value as HomeUiState.Success).content
        assertEquals(2, content.recentSearches.size)
        assertEquals("Nicolas Ayala", content.userDisplayName)
    }

    @Test
    fun `search submitted emits navigate effect with query`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val effects = mutableListOf<HomeEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(HomeEvent.SearchSubmitted("plomero"))
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.any { it is HomeEffect.NavigateToSearchResults && it.query == "plomero" })
    }

    @Test
    fun `search submitted does not persist recents before results`() = runTest(testDispatcher) {
        val recents = FakeRecentSearchRepository()
        val viewModel = HomeViewModel(
            getClientHomeUseCase = GetClientHomeUseCase(FakeHomeRepository()),
            trackRecentSearchUseCase = TrackRecentSearchUseCase(recents),
            getRecentSearchesUseCase = GetRecentSearchesUseCase(recents),
            observeRecentSearchesUseCase = ObserveRecentSearchesUseCase(recents),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeNotificationsRepository()),
        )
        advanceUntilIdle()
        viewModel.onEvent(HomeEvent.SearchSubmitted("plomería"))
        advanceUntilIdle()
        assertTrue(recents.tracked.isEmpty())
    }

    @Test
    fun `recents flow updates home after returning`() = runTest(testDispatcher) {
        val recents = FakeRecentSearchRepository(liveUpdates = true)
        val viewModel = HomeViewModel(
            getClientHomeUseCase = GetClientHomeUseCase(FakeHomeRepository()),
            trackRecentSearchUseCase = TrackRecentSearchUseCase(recents),
            getRecentSearchesUseCase = GetRecentSearchesUseCase(recents),
            observeRecentSearchesUseCase = ObserveRecentSearchesUseCase(recents),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeNotificationsRepository()),
        )
        advanceUntilIdle()
        recents.emitRecents(
            listOf(
                com.cocido.oficios.domain.model.RecentSearchEntry(
                    type = RecentSearchType.RUBRO,
                    id = 11,
                    label = "Plomería",
                    rubroId = 11,
                ),
            ),
        )
        advanceUntilIdle()
        val content = (viewModel.uiState.value as HomeUiState.Success).content
        assertEquals(listOf("Plomería"), content.recentSearches.map { it.label })
    }

    @Test
    fun `refresh failure keeps content and stops refreshing`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val content = (viewModel.uiState.value as HomeUiState.Success).content

        repository.homeResult = AppResult.Error(
            com.cocido.oficios.core.common.result.AppError.Network("No internet connection"),
        )
        viewModel.onEvent(HomeEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(content, state.content)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `retry reloads after error`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository(
            homeResult = com.cocido.oficios.core.common.result.AppResult.Error(
                com.cocido.oficios.core.common.result.AppError.Unknown(),
            ),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    @Test
    fun `unread badge keeps the last known count when refresh fails`() = runTest(testDispatcher) {
        val notifications = FakeNotificationsRepository()
        notifications.unreadCount = 3
        val first = createViewModel(FakeHomeRepository(), notifications)
        advanceUntilIdle()
        assertEquals(3, first.unreadNotificationCount.value)

        notifications.fail = true
        first.refreshUnreadCount()
        advanceUntilIdle()
        assertEquals(3, first.unreadNotificationCount.value)

        val recreated = createViewModel(FakeHomeRepository(), notifications)
        advanceUntilIdle()
        assertEquals(3, recreated.unreadNotificationCount.value)
    }

    private fun createViewModel(
        repository: FakeHomeRepository,
        notifications: FakeNotificationsRepository = FakeNotificationsRepository(),
    ) = HomeViewModel(
        getClientHomeUseCase = GetClientHomeUseCase(repository),
        trackRecentSearchUseCase = TrackRecentSearchUseCase(FakeRecentSearchRepository()),
        getRecentSearchesUseCase = GetRecentSearchesUseCase(FakeRecentSearchRepository()),
        observeRecentSearchesUseCase = ObserveRecentSearchesUseCase(FakeRecentSearchRepository()),
        getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(notifications),
    )
}

private class FakeNotificationsRepository : NotificationsRepository {
    var unreadCount: Int = 0
    var fail: Boolean = false
    private var cachedUnread: Int? = null

    override fun cachedUnreadCount(): Int? = cachedUnread

    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = AppResult.Success(Unit)
    override suspend fun markAsRead(notificationId: Long) = AppResult.Success(Unit)
    override suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage> {
        if (fail) return AppResult.Error(AppError.Network("No internet connection"))
        val page = NotificationPage(
            items = List(unreadCount) { index ->
                NotificationItem(
                    id = index.toLong(),
                    type = "info",
                    title = "Aviso",
                    body = "Texto",
                    createdAtLabel = "hoy",
                    isRead = false,
                    entityType = null,
                    entityId = null,
                    deepLink = null,
                )
            },
            pageNumber = 1,
            totalPages = 1,
            hasMore = false,
        )
        if (soloNoLeidas) cachedUnread = page.items.count { !it.isRead }
        return AppResult.Success(page)
    }
}
