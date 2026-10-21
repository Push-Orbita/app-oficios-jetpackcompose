package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailCancelPollTest {

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
    fun `cancel confirmed without reason does not call the API`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = null, currentCode = null).apply {
            detailStatus = com.cocido.oficios.domain.model.BookingStatus.CONFIRMED
        }
        val viewModel = OrderDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 7L)),
            orderUseCases = OrderUseCases(orders),
            authRepository = RoleAuthRepository(UserRole.PROVIDER),
            getCurrentLocationUseCase = GetCurrentLocationUseCase(NoLocationRepository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(NoWorkerProfileRepository),
        )
        advanceUntilIdle()
        val effects = mutableListOf<OrderDetailEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(OrderDetailEvent.CancelConfirmed(null))
        viewModel.onEvent(OrderDetailEvent.CancelConfirmed("  "))
        advanceUntilIdle()
        job.cancel()

        assertEquals(0, orders.cancelConfirmedCalls)
        assertTrue(effects.any { it is OrderDetailEffect.ShowMessage })
    }

    @Test
    fun `cancel confirmed sends the motivo`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = null, currentCode = null).apply {
            detailStatus = com.cocido.oficios.domain.model.BookingStatus.CONFIRMED
        }
        val viewModel = OrderDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 7L)),
            orderUseCases = OrderUseCases(orders),
            authRepository = RoleAuthRepository(UserRole.PROVIDER),
            getCurrentLocationUseCase = GetCurrentLocationUseCase(NoLocationRepository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(NoWorkerProfileRepository),
        )
        advanceUntilIdle()

        viewModel.onEvent(OrderDetailEvent.CancelConfirmed("Cambio de agenda"))
        advanceUntilIdle()

        assertEquals(1, orders.cancelConfirmedCalls)
        assertEquals("Cambio de agenda", orders.lastCancelReason)
    }

    @Test
    fun `pending order polls detail every 12 seconds`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = null, currentCode = null).apply {
            detailStatus = com.cocido.oficios.domain.model.BookingStatus.PENDING
            requestStatus = ServiceRequestStatus.PENDING
        }
        val viewModel = OrderDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("orderId" to 7L)),
            orderUseCases = OrderUseCases(orders),
            authRepository = RoleAuthRepository(UserRole.CLIENT),
            getCurrentLocationUseCase = GetCurrentLocationUseCase(NoLocationRepository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(NoWorkerProfileRepository),
        )
        advanceUntilIdle()
        val afterLoad = orders.detailCalls
        backgroundScope.launch { viewModel.keepOrderStatusFresh() }
        advanceTimeBy(OrderDetailViewModel.STATUS_REFRESH_MILLIS)
        runCurrent()
        assertTrue(orders.detailCalls > afterLoad)
    }
}
