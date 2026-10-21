package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import java.time.Clock
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
class BookingsAgendaViewModelTest {

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
    fun `loads worker bookings including past in-progress and skips elapsed confirmed`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            orders = listOf(
                agendaOrder(1, "2026-09-25T07:00:00", BookingStatus.IN_PROGRESS),
                agendaOrder(2, "2026-10-22T09:00:00", BookingStatus.CONFIRMED),
                agendaOrder(3, "2026-09-20T09:00:00", BookingStatus.CONFIRMED),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BookingsAgendaUiState.Success)
        assertEquals(listOf(1L, 2L), (state as BookingsAgendaUiState.Success).orders.map { it.id })
    }

    @Test
    fun `open slots navigates to slot configuration`() = runTest(testDispatcher) {
        val viewModel = createViewModel(orders = emptyList())
        advanceUntilIdle()
        val effects = mutableListOf<BookingsAgendaEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(BookingsAgendaEvent.OpenSlots)
        advanceUntilIdle()
        job.cancel()
        assertEquals(listOf(BookingsAgendaEffect.NavigateToSlots), effects)
    }

    @Test
    fun `order click navigates to detail`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            orders = listOf(agendaOrder(8, "2026-10-22T09:00:00", BookingStatus.CONFIRMED)),
        )
        advanceUntilIdle()
        val effects = mutableListOf<BookingsAgendaEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(BookingsAgendaEvent.OrderClicked(8))
        advanceUntilIdle()
        job.cancel()
        assertEquals(listOf(BookingsAgendaEffect.NavigateToOrderDetail(8)), effects)
    }

    @Test
    fun `load error shows retry state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            result = AppResult.Error(AppError.Network("No internet connection")),
        )
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is BookingsAgendaUiState.Error)
    }

    private fun createViewModel(
        orders: List<ServiceOrderListItem> = emptyList(),
        result: AppResult<List<ServiceOrderListItem>> = AppResult.Success(orders),
    ) = BookingsAgendaViewModel(
        orderUseCases = OrderUseCases(FakeWorkerOrdersRepository(result)),
        clock = Clock.fixed(
            ZonedDateTime.of(2026, 10, 21, 3, 17, 0, 0, DisplayDateTime.ARGENTINA).toInstant(),
            DisplayDateTime.ARGENTINA,
        ),
    )

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
}

private class FakeWorkerOrdersRepository(
    private val workerOrdersResult: AppResult<List<ServiceOrderListItem>>,
) : OrderRepository {
    override val ordersChanged: SharedFlow<Unit> = MutableSharedFlow()
    override suspend fun getWorkerOrders() = workerOrdersResult
    override suspend fun getClientOrders() = unused()
    override suspend fun createServiceRequest(params: CreateServiceRequestParams) = unused()
    override suspend fun getOrderDetail(orderId: Long): AppResult<ServiceOrder> = unused()
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
    override suspend fun markEnRoute(bookingId: Long, location: GeoPoint) = unused()
    override suspend fun markArrived(bookingId: Long, location: GeoPoint) = unused()
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

private fun unused(): Nothing = error("unused")
