package com.cocido.oficios.feature.home.rubros

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
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
class RubrosViewModelTest {

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
        assertTrue(viewModel.uiState.value is RubrosUiState.Success)
    }

    @Test
    fun `search submitted emits navigate effect`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<RubrosEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(RubrosEvent.SearchSubmitted("gasista"))
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.any { it is RubrosEffect.NavigateToSearchResults })
    }

    private fun createViewModel(): RubrosViewModel {
        val catalogRepository = object : CatalogRepository {
            override suspend fun getCategories(): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>> =
                AppResult.Success(listOf(CatalogOption(1L, "Plomería")))
            override suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
        }
        return RubrosViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "categoryId" to 1L,
                    "categoryName" to "Construcción",
                ),
            ),
            getRubrosByCategoryUseCase = GetRubrosByCategoryUseCase(catalogRepository),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(object : NotificationsRepository {
                override fun cachedUnreadCount(): Int? = 0
                override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = AppResult.Success(Unit)
                override suspend fun markAsRead(notificationId: Long) = AppResult.Success(Unit)
                override suspend fun getNotifications(
                    pageNumber: Int,
                    pageSize: Int,
                    soloNoLeidas: Boolean,
                ): AppResult<NotificationPage> = AppResult.Success(
                    NotificationPage(emptyList(), 1, 1, false),
                )
            }),
        )
    }
}
