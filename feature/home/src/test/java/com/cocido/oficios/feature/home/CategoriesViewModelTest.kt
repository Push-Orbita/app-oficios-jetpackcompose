package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.usecase.GetHomeCategoriesUseCase
import com.cocido.oficios.domain.usecase.TrackRecentCategoryUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.feature.home.navigation.HomeDestination
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
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CategoriesUiState.Success)
    }

    @Test
    fun `typing filters visible categories without leaving oficios`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<CategoriesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(CategoriesEvent.QueryChanged("hogar"))
        advanceUntilIdle()
        job.cancel()

        val success = viewModel.uiState.value as CategoriesUiState.Success
        assertEquals("hogar", success.query)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `search submitted opens worker results by name without rubro`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<CategoriesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(CategoriesEvent.SearchSubmitted("  Facundo Barberis "))
        viewModel.onEvent(CategoriesEvent.SearchSubmitted("   "))
        advanceUntilIdle()
        job.cancel()

        assertEquals(listOf(CategoriesEffect.NavigateToSearchResults("Facundo Barberis")), effects)
    }

    @Test
    fun `see all opens flat oficios list`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<CategoriesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(CategoriesEvent.SeeAll)
        advanceUntilIdle()
        job.cancel()

        assertTrue(
            effects.any { effect ->
                effect is CategoriesEffect.NavigateToRubros &&
                    effect.categoryId == HomeDestination.allRubrosCategoryId
            },
        )
    }

    @Test
    fun `invalid category id is ignored`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(
            CategoriesEvent.CategorySelected(HomeCategoryItem(id = -1L, name = "Ver todos")),
        )
        advanceUntilIdle()
    }

    private fun createViewModel() = CategoriesViewModel(
        getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
        trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
        getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(FakeUnreadNotificationsRepository()),
    )
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
