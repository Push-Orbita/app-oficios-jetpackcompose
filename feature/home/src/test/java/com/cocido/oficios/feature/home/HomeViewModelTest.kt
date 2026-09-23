package com.cocido.oficios.feature.home

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.usecase.GetClientHomeUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
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
        assertEquals(1, content.featuredWorkers.size)
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

    private fun createViewModel(repository: FakeHomeRepository) = HomeViewModel(
        getClientHomeUseCase = GetClientHomeUseCase(repository),
        trackRecentSearchUseCase = TrackRecentSearchUseCase(FakeRecentSearchRepository()),
        getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeNotificationsRepository()),
    )
}

private class FakeNotificationsRepository : NotificationsRepository {
    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = AppResult.Success(Unit)
    override suspend fun markAsRead(notificationId: Long) = AppResult.Success(Unit)
    override suspend fun getNotifications(pageNumber: Int, pageSize: Int, soloNoLeidas: Boolean) =
        AppResult.Success(NotificationPage(items = emptyList(), pageNumber = 1, totalPages = 1, hasMore = false))
}
