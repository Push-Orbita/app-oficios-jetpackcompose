package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.BookingTracking
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersListViewModelTest {

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
    fun `refresh failure keeps orders and emits refresh error`() = runTest(testDispatcher) {
        val orders = listOf(order(7))
        val repository = FakeOrderRepository(AppResult.Success(orders))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        repository.clientOrders = AppResult.Error(AppError.Network("No internet connection"))
        val effects = mutableListOf<OrdersListEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(OrdersListEvent.Refresh)
        advanceUntilIdle()
        job.cancel()

        val state = viewModel.uiState.value as OrdersListUiState.Success
        assertEquals(orders, state.orders)
        assertFalse(state.isRefreshing)
        assertEquals(
            listOf(
                OrdersListEffect.ShowRefreshError(
                    "No pudimos conectarnos. Revisá tu conexión e intentá nuevamente.",
                ),
            ),
            effects,
        )
    }

    @Test
    fun `refresh keeps orders and tabs when auth me fails`() = runTest(testDispatcher) {
        val orders = listOf(order(7))
        val repository = FakeOrderRepository(AppResult.Success(orders))
        val auth = FakeAuthRepository()
        val viewModel = viewModel(repository, auth)
        advanceUntilIdle()
        viewModel.onEvent(OrdersListEvent.FilterSelected(OrdersListFilter.HISTORY))

        auth.currentUser = AppResult.Error(AppError.Network("No internet connection"))
        viewModel.onEvent(OrdersListEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value as OrdersListUiState.Success
        assertEquals(orders, state.orders)
        assertEquals(OrdersListFilter.HISTORY, state.filter)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `initial load failure stays on retry error without refresh effect`() = runTest(testDispatcher) {
        val repository = FakeOrderRepository(
            AppResult.Error(AppError.Network("No internet connection")),
        )
        val viewModel = viewModel(repository)
        val effects = mutableListOf<OrdersListEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        advanceUntilIdle()
        viewModel.onEvent(OrdersListEvent.Retry)
        advanceUntilIdle()
        viewModel.onEvent(OrdersListEvent.Refresh)
        advanceUntilIdle()
        job.cancel()

        val state = viewModel.uiState.value as OrdersListUiState.Error
        assertEquals(
            "No pudimos conectarnos. Revisá tu conexión e intentá nuevamente.",
            state.message,
        )
        assertFalse(state.isRefreshing)
        assertEquals(
            listOf(
                OrdersListEffect.ShowRefreshError(
                    "No pudimos conectarnos. Revisá tu conexión e intentá nuevamente.",
                ),
            ),
            effects,
        )
    }

    private fun viewModel(
        repository: FakeOrderRepository,
        authRepository: AuthRepository = FakeAuthRepository(),
    ) = OrdersListViewModel(
        orderUseCases = OrderUseCases(repository),
        authRepository = authRepository,
        orderRepository = repository,
    )

    private fun order(id: Long) = ServiceOrderListItem(
        id = id,
        counterpartName = "Ana",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.PENDING,
        bookingStatus = null,
        scheduledLabel = null,
        addressLabel = "Calle 1",
    )
}

private class FakeOrderRepository(
    var clientOrders: AppResult<List<ServiceOrderListItem>>,
) : OrderRepository {
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val ordersChanged: SharedFlow<Unit> = changes

    override suspend fun getClientOrders() = clientOrders
    override suspend fun getWorkerOrders() = clientOrders
    override suspend fun createServiceRequest(params: CreateServiceRequestParams): AppResult<Long> = unused()
    override suspend fun getOrderDetail(orderId: Long): AppResult<ServiceOrder> = unused()
    override suspend fun acceptOrder(orderId: Long): AppResult<Unit> = unused()
    override suspend fun rejectOrder(orderId: Long, reason: String): AppResult<Unit> = unused()
    override suspend fun sendCounterOffer(
        orderId: Long,
        newStartIso: String,
        newEndIso: String,
        newPrice: Double?,
    ): AppResult<Unit> = unused()
    override suspend fun acceptCounterOffer(counterOfferId: Long): AppResult<Unit> = unused()
    override suspend fun rejectCounterOffer(counterOfferId: Long): AppResult<Unit> = unused()
    override suspend fun cancelOrderBeforeAcceptance(orderId: Long): AppResult<Unit> = unused()
    override suspend fun cancelConfirmedOrder(orderId: Long, reason: String?): AppResult<Unit> = unused()
    override suspend fun getBookingTracking(bookingId: Long): AppResult<BookingTracking?> = unused()
    override suspend fun findOrderIdForBooking(bookingId: Long): AppResult<Long?> = unused()
    override suspend fun markEnRoute(bookingId: Long, location: GeoPoint): AppResult<Unit> = unused()
    override suspend fun markArrived(bookingId: Long, location: GeoPoint): AppResult<Unit> = unused()
    override suspend fun getCompletionCode(bookingId: Long): AppResult<String?> = unused()
    override suspend fun resendCompletionCode(bookingId: Long): AppResult<Unit> = unused()
    override suspend fun finalizeBooking(bookingId: Long, completionCode: String): AppResult<Unit> = unused()
    override suspend fun submitRating(
        bookingId: Long,
        quality: Int,
        punctuality: Int,
        comment: String?,
    ): AppResult<Unit> = unused()
}

private class FakeAuthRepository : AuthRepository {
    var currentUser: AppResult<User> = AppResult.Success(
        User(
            id = 1,
            email = "ana@oficios.test",
            firstName = "Ana",
            lastName = "Perez",
            role = UserRole.CLIENT,
            emailVerified = true,
        ),
    )

    override suspend fun getCurrentUser(): AppResult<User> = currentUser

    override suspend fun login(email: String, password: String): AppResult<UserSession> = unused()
    override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> = unused()
    override suspend fun registerProvider(request: RegisterProviderRequest): AppResult<UserSession> = unused()
    override suspend fun refreshToken(refreshToken: String): AppResult<AuthTokens> = unused()
    override suspend fun logout(): AppResult<Unit> = unused()
    override suspend fun requestPasswordReset(email: String): AppResult<Unit> = unused()
    override suspend fun verifyPasswordResetCode(code: String): AppResult<String> = unused()
    override suspend fun resetPassword(
        resetToken: String,
        password: String,
        confirmPassword: String,
    ): AppResult<Unit> = unused()
    override suspend fun sendVerificationEmail(): AppResult<String> = unused()
    override suspend fun verifyEmail(code: String): AppResult<Unit> = unused()
    override suspend fun getUserRoles(): AppResult<List<UserRoleDefinition>> = unused()
}

private fun unused(): Nothing = error("unused")
