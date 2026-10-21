package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.usecase.GetProviderHomeUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.feature.home.FakeHomeRepository
import java.time.Clock
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

    @Test
    fun `past unclosed bookings are dropped from home agenda`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            percent = 100,
            upcoming = listOf(
                agendaOrder(1, "2026-09-25T07:00:00", BookingStatus.IN_PROGRESS),
                agendaOrder(2, "2026-09-29T07:00:00", BookingStatus.IN_PROGRESS),
                agendaOrder(3, "2026-10-05T12:00:00", BookingStatus.IN_PROGRESS),
                agendaOrder(4, "2026-10-22T09:00:00", BookingStatus.CONFIRMED),
            ),
        )
        advanceUntilIdle()

        val content = successContent(viewModel)
        assertEquals(listOf(4L), content.upcomingOrders.map { it.id })
    }

    @Test
    fun `gallery tile navigates to portfolio not onboarding checklist`() = runTest(testDispatcher) {
        val viewModel = createViewModel(percent = 66)
        advanceUntilIdle()
        val effects = collectEffects(viewModel) {
            viewModel.onEvent(ProviderHomeEvent.GalleryTileClicked)
        }
        assertEquals(listOf(ProviderHomeEffect.NavigateToGallery), effects)
    }

    @Test
    fun `local tile navigates to commercial local editor`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            content = providerHome(66).copy(
                hasGalleryPhotos = true,
                hasCommercialLocal = false,
                hasCertificate = true,
            ),
        )
        advanceUntilIdle()
        val effects = collectEffects(viewModel) {
            viewModel.onEvent(ProviderHomeEvent.CommercialLocalTileClicked)
        }
        assertEquals(listOf(ProviderHomeEffect.NavigateToCommercialLocal), effects)
    }

    @Test
    fun `certificate tile navigates to add certificate`() = runTest(testDispatcher) {
        val viewModel = createViewModel(percent = 66)
        advanceUntilIdle()
        val effects = collectEffects(viewModel) {
            viewModel.onEvent(ProviderHomeEvent.CertificateTileClicked)
        }
        assertEquals(listOf(ProviderHomeEffect.NavigateToCertificate), effects)
    }

    @Test
    fun `completar perfil link opens first incomplete tile`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            content = providerHome(66).copy(
                hasGalleryPhotos = true,
                hasCommercialLocal = false,
                hasCertificate = true,
            ),
        )
        advanceUntilIdle()
        val effects = collectEffects(viewModel) {
            viewModel.onEvent(ProviderHomeEvent.CompleteProfileLinkClicked)
        }
        assertEquals(listOf(ProviderHomeEffect.NavigateToCommercialLocal), effects)
    }

    @Test
    fun `returning to home silently refetches profile completion`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository(
            providerHomeResult = AppResult.Success(
                providerHome(66).copy(
                    hasGalleryPhotos = true,
                    hasCommercialLocal = false,
                    hasCertificate = true,
                ),
            ),
        )
        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()
        assertEquals(66, successContent(viewModel).profileCompletionPercent)
        assertEquals(1, repository.providerHomeLoads)

        repository.providerHomeResult = AppResult.Success(
            providerHome(100).copy(
                hasGalleryPhotos = true,
                hasCommercialLocal = true,
                hasCertificate = true,
            ),
        )
        viewModel.onVisible()
        advanceUntilIdle()

        val content = successContent(viewModel)
        assertEquals(2, repository.providerHomeLoads)
        assertEquals(100, content.profileCompletionPercent)
        assertTrue(content.hasCommercialLocal)
        assertFalse(content.showProfileCompletion)
        assertFalse((viewModel.uiState.value as ProviderHomeUiState.Success).isRefreshing)
    }

    @Test
    fun `today and future stay sorted ascending`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            percent = 100,
            upcoming = listOf(
                agendaOrder(2, "2026-10-22T09:00:00", BookingStatus.CONFIRMED),
                agendaOrder(1, "2026-10-21T10:00:00", BookingStatus.CONFIRMED),
            ),
        )
        advanceUntilIdle()

        val content = successContent(viewModel)
        assertEquals(listOf(1L, 2L), content.upcomingOrders.map { it.id })
    }

    private fun successContent(viewModel: ProviderHomeViewModel): ProviderHomeContent {
        val state = viewModel.uiState.value
        assertTrue(state is ProviderHomeUiState.Success)
        return (state as ProviderHomeUiState.Success).content
    }

    private fun TestScope.collectEffects(
        viewModel: ProviderHomeViewModel,
        block: () -> Unit,
    ): List<ProviderHomeEffect> {
        val effects = mutableListOf<ProviderHomeEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        block()
        advanceUntilIdle()
        job.cancel()
        return effects
    }

    private fun createViewModel(
        percent: Int = 0,
        upcoming: List<ServiceOrderListItem> = emptyList(),
        clock: Clock = fixedClock(),
        content: ProviderHomeContent? = null,
        repository: FakeHomeRepository? = null,
    ) = ProviderHomeViewModel(
        getProviderHomeUseCase = GetProviderHomeUseCase(
            repository ?: FakeHomeRepository(
                providerHomeResult = AppResult.Success(
                    (content ?: providerHome(percent)).copy(upcomingOrders = upcoming),
                ),
            ),
        ),
        getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(
            FakeUnreadNotificationsRepository(),
        ),
        clock = clock,
    )

    private fun fixedClock(): Clock {
        val instant = ZonedDateTime.of(2026, 10, 21, 3, 17, 0, 0, DisplayDateTime.ARGENTINA).toInstant()
        return Clock.fixed(instant, DisplayDateTime.ARGENTINA)
    }

    private fun agendaOrder(id: Long, start: String, status: BookingStatus) = ServiceOrderListItem(
        id = id,
        counterpartName = "Cliente",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.ACCEPTED,
        bookingStatus = status,
        scheduledLabel = null,
        addressLabel = "Av. 25 de Mayo 100",
        rubroLabel = "Plomería",
        scheduledSortKey = start,
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
