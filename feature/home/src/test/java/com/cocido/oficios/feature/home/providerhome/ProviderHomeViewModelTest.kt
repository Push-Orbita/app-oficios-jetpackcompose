package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.usecase.GetProviderHomeUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.feature.home.FakeHomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ProviderHomeViewModelTest {

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
    fun `incomplete profile keeps Completa Tu Perfil visible`() = runTest(testDispatcher) {
        val viewModel = createViewModel(percent = 80)
        advanceUntilIdle()

        val content = successContent(viewModel)
        assertEquals(80, content.profileCompletionPercent)
        assertTrue(content.showProfileCompletion)
    }

    @Test
    fun `one third complete still shows Completa Tu Perfil`() = runTest(testDispatcher) {
        val viewModel = createViewModel(percent = 33)
        advanceUntilIdle()

        val content = successContent(viewModel)
        assertEquals(33, content.profileCompletionPercent)
        assertTrue(content.showProfileCompletion)
    }

    @Test
    fun `complete profile hides Completa Tu Perfil`() = runTest(testDispatcher) {
        val viewModel = createViewModel(percent = 100)
        advanceUntilIdle()

        val content = successContent(viewModel)
        assertEquals(100, content.profileCompletionPercent)
        assertFalse(content.showProfileCompletion)
    }

    private fun successContent(viewModel: ProviderHomeViewModel): ProviderHomeContent {
        val state = viewModel.uiState.value
        assertTrue(state is ProviderHomeUiState.Success)
        return (state as ProviderHomeUiState.Success).content
    }

    private fun createViewModel(percent: Int) = ProviderHomeViewModel(
        getProviderHomeUseCase = GetProviderHomeUseCase(
            FakeHomeRepository(
                providerHomeResult = AppResult.Success(providerHome(percent)),
            ),
        ),
        getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(
            FakeUnreadNotificationsRepository(),
        ),
    )

    private fun providerHome(percent: Int) = ProviderHomeContent(
        userDisplayName = "Emanuel Molina",
        isVerified = true,
        primarySpecialtyLabel = "Albañil, Electricista",
        profileCompletionPercent = percent,
        missingProfileItems = emptyList(),
        hasGalleryPhotos = percent >= 33,
        hasCommercialLocal = percent >= 66,
        hasCertificate = percent >= 100,
    )
}

private class FakeUnreadNotificationsRepository : NotificationsRepository {
    override fun cachedUnreadCount(): Int? = 0

    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = AppResult.Success(Unit)

    override suspend fun markAsRead(notificationId: Long) = AppResult.Success(Unit)

    override suspend fun getNotifications(
        pageNumber: Int,
        pageSize: Int,
        soloNoLeidas: Boolean,
    ): AppResult<NotificationPage> {
        return AppResult.Success(
            NotificationPage(
                items = emptyList<NotificationItem>(),
                pageNumber = 1,
                totalPages = 1,
                hasMore = false,
            ),
        )
    }
}
