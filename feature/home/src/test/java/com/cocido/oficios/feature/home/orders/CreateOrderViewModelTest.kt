package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerSpecialty
import com.cocido.oficios.domain.model.WorkerSubRubroOption
import com.cocido.oficios.domain.repository.AgendaRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.repository.WorkerProfileRepository
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.repository.AvailabilityRepository
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.AvailabilityUseCases
import com.cocido.oficios.domain.usecase.CreateServiceRequestUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import java.time.LocalDate
import java.time.LocalTime
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
class CreateOrderViewModelTest {

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
    fun `continue without date stays on form`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(CreateOrderEvent.ContinueToSummary)
        advanceUntilIdle()

        val state = viewModel.uiState.value as CreateOrderUiState.Success
        assertEquals(CreateOrderStep.FORM, state.step)
        assertTrue(state.hasAttemptedContinue)
    }

    @Test
    fun `continue with date and hour opens summary`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        val date = LocalDate.now().plusDays(1)

        viewModel.onEvent(CreateOrderEvent.DateSelected(date))
        viewModel.onEvent(CreateOrderEvent.HourSelected(LocalTime.of(10, 0)))
        viewModel.onEvent(CreateOrderEvent.ContinueToSummary)
        advanceUntilIdle()

        val state = viewModel.uiState.value as CreateOrderUiState.Success
        assertEquals(CreateOrderStep.SUMMARY, state.step)
    }

    @Test
    fun `submit from form does not call the API`() = runTest(testDispatcher) {
        val orders = RecordingOrderRepository()
        val viewModel = viewModel(orders = orders)
        advanceUntilIdle()
        val date = LocalDate.now().plusDays(1)
        viewModel.onEvent(CreateOrderEvent.DateSelected(date))
        viewModel.onEvent(CreateOrderEvent.HourSelected(LocalTime.of(10, 0)))

        viewModel.onEvent(CreateOrderEvent.Submit)
        advanceUntilIdle()

        assertEquals(0, orders.createCalls.size)
        assertEquals(CreateOrderStep.SUMMARY, (viewModel.uiState.value as CreateOrderUiState.Success).step)
    }

    @Test
    fun `submit from summary sends request and emits RequestSent`() = runTest(testDispatcher) {
        val orders = RecordingOrderRepository()
        val viewModel = viewModel(orders = orders)
        advanceUntilIdle()
        val date = LocalDate.now().plusDays(1)
        viewModel.onEvent(CreateOrderEvent.DateSelected(date))
        viewModel.onEvent(CreateOrderEvent.HourSelected(LocalTime.of(10, 0)))
        viewModel.onEvent(CreateOrderEvent.ContinueToSummary)
        advanceUntilIdle()

        val effects = mutableListOf<CreateOrderEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(CreateOrderEvent.Submit)
        advanceUntilIdle()
        job.cancel()

        assertEquals(1, orders.createCalls.size)
        assertEquals(3L, orders.createCalls.single().domicilioId)
        assertTrue(effects.contains(CreateOrderEffect.RequestSent))
    }

    private fun viewModel(
        orders: RecordingOrderRepository = RecordingOrderRepository(),
    ): CreateOrderViewModel {
        val profile = WorkerProfile(
            id = 8L,
            displayName = "Emanuel Molina",
            avatarUrl = null,
            isVerified = true,
            specialties = listOf(
                WorkerSpecialty(
                    rubroName = "Construcción",
                    subRubroNames = listOf("Albañilería"),
                    servesAtHome = true,
                    servesAtLocal = false,
                    isPrimary = true,
                    subRubros = listOf(WorkerSubRubroOption(id = 21L, name = "Albañilería")),
                ),
            ),
        )
        return CreateOrderViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to 8L)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(FixedWorkerProfileRepository(profile)),
            clientProfileRepository = FixedClientProfileRepository(),
            createServiceRequestUseCase = CreateServiceRequestUseCase(orders),
            agendaUseCases = AgendaUseCases(EmptyAgendaRepository()),
            availabilityUseCases = AvailabilityUseCases(MissingDayAvailabilityRepository()),
        )
    }
}

private class FixedWorkerProfileRepository(
    private val profile: WorkerProfile,
) : WorkerProfileRepository {
    override suspend fun getWorkerProfile(workerId: Long) = AppResult.Success(profile)
    override suspend fun getWorkerReviews(workerId: Long, pageNumber: Int, pageSize: Int) =
        error("unused")
}

private class FixedClientProfileRepository : ClientProfileRepository {
    override suspend fun saveDomicilio(request: ClientDomicilioRequest) = unused()
    override suspend fun saveContacto(request: ClientContactoRequest) = unused()
    override suspend fun sendPhoneVerificationCode(contactId: Long) = unused()
    override suspend fun verifyPhoneCode(contactId: Long, code: String) = unused()
    override suspend fun getMyDomicilios() = AppResult.Success(
        listOf(
            ClientDomicilio(
                id = 3L,
                barrioId = 1L,
                barrioName = "San Juan",
                nombre = "Casa",
                calle = "Corrientes",
                numero = "1245",
                casaTorre = null,
                departamento = null,
                piso = null,
                favorito = true,
            ),
        ),
    )
    override suspend fun getMyContactos() = AppResult.Success(
        listOf(
            ClientContacto(
                id = 4L,
                nombre = "Nicolás",
                telefonoWhatsapp = "3704000000",
                email = null,
                favorito = true,
            ),
        ),
    )

    private fun unused(): Nothing = error("unused")
}

private class MissingDayAvailabilityRepository : AvailabilityRepository {
    override suspend fun getWeeklyAvailability(subRubroId: Long) =
        AppResult.Error(AppError.Unknown("unused"))
    override suspend fun replaceWeeklyAvailability(
        subRubroId: Long,
        week: WeeklyAvailability,
    ) = AppResult.Error(AppError.Unknown("unused"))
    override suspend fun listBlocks(subRubroId: Long) =
        AppResult.Error(AppError.Unknown("unused"))
    override suspend fun createBlock(params: CreateAgendaBlockParams) =
        AppResult.Error(AppError.Unknown("unused"))
    override suspend fun deleteBlock(subRubroId: Long, blockId: Long) =
        AppResult.Error(AppError.Unknown("unused"))
    override suspend fun getDayAvailability(workerId: Long, date: String, subRubroId: Long) =
        AppResult.Error(AppError.Unknown("unavailable"))
}

private class EmptyAgendaRepository : AgendaRepository {
    override suspend fun getAvailableSlots(subRubroId: Long, fromIso: String, toIso: String) =
        AppResult.Success(emptyList<AgendaSlot>())
    override suspend fun getSlotsForSubRubro(subRubroId: Long, fromIso: String?, toIso: String?) = unused()
    override suspend fun createSlot(params: CreateAgendaSlotParams) = unused()
    override suspend fun updateSlot(params: UpdateAgendaSlotParams) = unused()
    override suspend fun deleteSlot(slotId: Long) = unused()
    override suspend fun getSessionsForSubRubro(subRubroId: Long) = unused()
    override suspend fun saveSession(params: SaveAgendaSessionParams) = unused()
    override suspend fun updateSession(params: UpdateAgendaSessionParams) = unused()
    override suspend fun deleteSession(sessionId: Long) = unused()
    private fun unused(): Nothing = error("unused")
}

private class RecordingOrderRepository : OrderRepository {
    val createCalls = mutableListOf<CreateServiceRequestParams>()
    override val ordersChanged: SharedFlow<Unit> = MutableSharedFlow()
    override suspend fun createServiceRequest(params: CreateServiceRequestParams): AppResult<Long> {
        createCalls += params
        return AppResult.Success(99L)
    }
    override suspend fun getClientOrders() = unused()
    override suspend fun getWorkerOrders() = unused()
    override suspend fun getOrderDetail(orderId: Long) = unused()
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
    override suspend fun markEnRoute(
        bookingId: Long,
        location: com.cocido.oficios.domain.model.GeoPoint,
    ) = unused()
    override suspend fun markArrived(
        bookingId: Long,
        location: com.cocido.oficios.domain.model.GeoPoint,
    ) = unused()
    override suspend fun getCompletionCode(bookingId: Long) = unused()
    override suspend fun resendCompletionCode(bookingId: Long) = unused()
    override suspend fun finalizeBooking(bookingId: Long, completionCode: String) = unused()
    override suspend fun submitRating(
        bookingId: Long,
        quality: Int,
        punctuality: Int,
        comment: String?,
    ) = unused()

    private fun unused(): Nothing = error("unused")
}
