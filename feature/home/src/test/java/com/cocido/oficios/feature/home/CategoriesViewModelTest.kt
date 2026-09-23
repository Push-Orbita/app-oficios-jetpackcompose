package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.usecase.GetHomeCategoriesUseCase
import com.cocido.oficios.domain.usecase.TrackRecentCategoryUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

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
    fun `load emits success state`() = runTest(testDispatcher) {
        val viewModel = CategoriesViewModel(
            getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
            trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeUnreadNotificationsRepository()),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CategoriesUiState.Success)
    }

    @Test
    fun `search submitted emits navigate effect`() = runTest(testDispatcher) {
        val viewModel = CategoriesViewModel(
            getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
            trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeUnreadNotificationsRepository()),
        )
        advanceUntilIdle()

        val effects = mutableListOf<CategoriesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(CategoriesEvent.SearchSubmitted("electricista"))
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.any { it is CategoriesEffect.NavigateToSearchResults })
    }

    @Test
    fun `invalid category id is ignored`() = runTest(testDispatcher) {
        val viewModel = CategoriesViewModel(
            getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
            trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeUnreadNotificationsRepository()),
        )
        advanceUntilIdle()

        viewModel.onEvent(
            CategoriesEvent.CategorySelected(HomeCategoryItem(id = -1L, name = "Ver todos")),
        )
        advanceUntilIdle()
    }
}

private class FakeUnreadNotificationsRepository : com.cocido.oficios.domain.repository.NotificationsRepository {
    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) =
        com.cocido.oficios.core.common.result.AppResult.Success(Unit)
    override suspend fun markAsRead(notificationId: Long) =
        com.cocido.oficios.core.common.result.AppResult.Success(Unit)
    override suspend fun getNotifications(pageNumber: Int, pageSize: Int, soloNoLeidas: Boolean) =
        com.cocido.oficios.core.common.result.AppResult.Success(
            com.cocido.oficios.domain.model.NotificationPage(
                items = emptyList(),
                pageNumber = 1,
                totalPages = 1,
                hasMore = false,
            ),
        )
}
