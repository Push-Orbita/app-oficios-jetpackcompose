package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.BookingTracking
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.PagedWorkerReviews
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.LocationRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.repository.WorkerProfileRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailCompletionCodeTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fetchMillis = OrderDetailViewModel.COMPLETION_CODE_FETCH_MILLIS
    private val regenerateMillis = OrderDetailViewModel.COMPLETION_CODE_REGENERATE_MILLIS

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `client in progress regenerates on start and shows the fresh code`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = "111111", currentCode = "222222")
        val viewModel = viewModel(orders)
        advanceUntilIdle()

        startVisible(viewModel)

        assertEquals(1, orders.resendCalls)
        assertEquals(1, orders.getCodeCalls)
        assertEquals("222222", successState(viewModel).order.completionCode)
    }

    @Test
    fun `while visible it re-fetches every 30 s and regenerates every 4 min`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = "111111", currentCode = "222222")
        val viewModel = viewModel(orders)
        advanceUntilIdle()
        startVisible(viewModel)

        orders.currentCode = "333333"
        advanceTimeBy(fetchMillis)
        runCurrent()
        assertEquals("333333", successState(viewModel).order.completionCode)
        assertEquals(1, orders.resendCalls)
        assertEquals(2, orders.getCodeCalls)

        // Hasta completar el intervalo de regeneración solo hay GETs.
        advanceTimeBy(regenerateMillis - fetchMillis)
        runCurrent()
        assertEquals(2, orders.resendCalls)
        assertEquals((regenerateMillis / fetchMillis).toInt() + 1, orders.getCodeCalls)
    }

    @Test
    fun `stopping the screen stops refreshing and resuming regenerates again`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = "111111", currentCode = "222222")
        val viewModel = viewModel(orders)
        advanceUntilIdle()

        val visible = startVisible(viewModel)
        visible.cancel() // ON_STOP: repeatOnLifecycle cancela el bloque
        advanceTimeBy(10 * fetchMillis)
        runCurrent()
        assertEquals(1, orders.resendCalls)
        assertEquals(1, orders.getCodeCalls)

        orders.currentCode = "444444"
        startVisible(viewModel) // ON_START de nuevo
        assertEquals(2, orders.resendCalls)
        assertEquals("444444", successState(viewModel).order.completionCode)
    }

    @Test
    fun `rate limited resend is ignored and the current code is fetched without errors`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = null, currentCode = "555555").apply {
            resendResult = AppResult.Error(
                AppError.Network("Esperá un momento", code = 429, backendCode = "COMPLETION_CODE_RATE_LIMITED"),
            )
        }
        val viewModel = viewModel(orders)
        advanceUntilIdle()
        val effects = mutableListOf<OrderDetailEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }

        startVisible(viewModel)

        val state = successState(viewModel)
        assertEquals("555555", state.order.completionCode)
        assertFalse(state.completionCodeUnavailable)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `fetch failure keeps the shown code and only flags when there is none`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = "111111", currentCode = "222222").apply {
            codeError = AppError.Network("No internet connection")
        }
        val viewModel = viewModel(orders)
        advanceUntilIdle()
        startVisible(viewModel)
        assertEquals("111111", successState(viewModel).order.completionCode)
        assertFalse(successState(viewModel).completionCodeUnavailable)

        val noCodeOrders = CodeOrderRepository(detailCode = null, currentCode = null).apply {
            codeError = AppError.Network("No internet connection")
        }
        val noCodeViewModel = viewModel(noCodeOrders)
        advanceUntilIdle()
        startVisible(noCodeViewModel)
        assertTrue(successState(noCodeViewModel).completionCodeUnavailable)
    }

    @Test
    fun `worker never triggers code regeneration`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = null, currentCode = "222222")
        val viewModel = viewModel(orders, role = UserRole.PROVIDER)
        advanceUntilIdle()
        startVisible(viewModel)
        advanceTimeBy(regenerateMillis)
        runCurrent()
        assertEquals(0, orders.resendCalls)
        assertEquals(0, orders.getCodeCalls)
    }

    @Test
    fun `when the backend stops exposing the code the detail is reloaded silently`() = runTest(testDispatcher) {
        val orders = CodeOrderRepository(detailCode = "111111", currentCode = "222222")
        val viewModel = viewModel(orders)
        advanceUntilIdle()
        startVisible(viewModel)

        orders.currentCode = null
        orders.detailStatus = BookingStatus.FINISHED
        advanceTimeBy(fetchMillis)
        runCurrent()
        advanceUntilIdle()

        assertEquals(BookingStatus.FINISHED, successState(viewModel).order.bookingStatus)
        val callsAfterFinish = orders.getCodeCalls
        advanceTimeBy(3 * fetchMillis)
        runCurrent()
        assertEquals(callsAfterFinish, orders.getCodeCalls)
    }

    private fun TestScope.startVisible(viewModel: OrderDetailViewModel) =
        backgroundScope.launch { viewModel.keepCompletionCodeFresh() }.also { runCurrent() }

    private fun successState(viewModel: OrderDetailViewModel) =
        viewModel.uiState.value as OrderDetailUiState.Success

    private fun viewModel(orders: CodeOrderRepository, role: UserRole = UserRole.CLIENT) = OrderDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("orderId" to 7L)),
        orderUseCases = OrderUseCases(orders),
        authRepository = RoleAuthRepository(role),
        getCurrentLocationUseCase = GetCurrentLocationUseCase(NoLocationRepository),
        getWorkerReviewsUseCase = GetWorkerReviewsUseCase(NoWorkerProfileRepository),
    )
}

private class CodeOrderRepository(
    private val detailCode: String?,
    var currentCode: String?,
) : OrderRepository {
    var detailStatus: BookingStatus = BookingStatus.IN_PROGRESS
    var resendResult: AppResult<Unit> = AppResult.Success(Unit)
    var codeError: AppError? = null
    var resendCalls = 0
    var getCodeCalls = 0

    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val ordersChanged: SharedFlow<Unit> = changes

    override suspend fun getOrderDetail(orderId: Long): AppResult<ServiceOrder> = AppResult.Success(
        ServiceOrder(
            id = orderId,
            clientName = "Ana",
            workerName = "Juan",
            workerId = 0L,
            rubroLabel = "Hogar",
            subRubroLabel = "Plomería",
            status = ServiceRequestStatus.ACCEPTED,
            description = "Pérdida en la cocina",
            addressLabel = "Calle 1",
            contactName = "Ana",
            contactPhone = "3704000000",
            bookingId = 42L,
            bookingStatus = detailStatus,
            completionCode = detailCode.takeIf { detailStatus == BookingStatus.IN_PROGRESS },
        ),
    )

    override suspend fun getCompletionCode(bookingId: Long): AppResult<String?> {
        getCodeCalls++
        codeError?.let { return AppResult.Error(it) }
        return AppResult.Success(currentCode)
    }

    override suspend fun resendCompletionCode(bookingId: Long): AppResult<Unit> {
        resendCalls++
        return resendResult
    }

    override suspend fun getBookingTracking(bookingId: Long): AppResult<BookingTracking?> = AppResult.Success(null)

    override suspend fun createServiceRequest(params: CreateServiceRequestParams): AppResult<Long> = unused()
    override suspend fun getClientOrders(): AppResult<List<ServiceOrderListItem>> = unused()
    override suspend fun getWorkerOrders(): AppResult<List<ServiceOrderListItem>> = unused()
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
    override suspend fun findOrderIdForBooking(bookingId: Long): AppResult<Long?> = unused()
    override suspend fun markEnRoute(bookingId: Long, location: GeoPoint): AppResult<Unit> = unused()
    override suspend fun markArrived(bookingId: Long, location: GeoPoint): AppResult<Unit> = unused()
    override suspend fun finalizeBooking(bookingId: Long, completionCode: String): AppResult<Unit> = unused()
    override suspend fun submitRating(
        bookingId: Long,
        quality: Int,
        punctuality: Int,
        comment: String?,
    ): AppResult<Unit> = unused()
}

private class RoleAuthRepository(private val role: UserRole) : AuthRepository {
    override suspend fun getCurrentUser(): AppResult<User> = AppResult.Success(
        User(
            id = 1,
            email = "ana@oficios.test",
            firstName = "Ana",
            lastName = "Perez",
            role = role,
            emailVerified = true,
        ),
    )

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

private object NoLocationRepository : LocationRepository {
    override suspend fun getCurrentLocation(requirePrecise: Boolean): AppResult<GeoPoint> = unused()
    override fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>> = emptyFlow()
}

private object NoWorkerProfileRepository : WorkerProfileRepository {
    override suspend fun getWorkerProfile(workerId: Long): AppResult<WorkerProfile> = unused()
    override suspend fun getWorkerReviews(workerId: Long, pageNumber: Int, pageSize: Int): AppResult<PagedWorkerReviews> =
        unused()
}

private fun unused(): Nothing = error("unused")
