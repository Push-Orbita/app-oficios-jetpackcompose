package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.model.BookingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderUseCases: OrderUseCases,
    private val authRepository: AuthRepository,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L
    private var viewerRole: UserRole = UserRole.UNKNOWN
    private var trackingRefreshJob: Job? = null

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OrderDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: OrderDetailEvent) {
        when (event) {
            OrderDetailEvent.Retry -> load()
            OrderDetailEvent.Accept -> runAction { orderUseCases.acceptOrder(orderId) }
            is OrderDetailEvent.Reject -> runAction { orderUseCases.rejectOrder(orderId, event.reason) }
            OrderDetailEvent.OpenCounterOffer -> emitEffect(OrderDetailEffect.NavigateToCounterOffer(orderId))
            OrderDetailEvent.AcceptCounterOffer -> {
                val counterOfferId = (uiState.value as? OrderDetailUiState.Success)?.order?.pendingCounterOffer?.id
                if (counterOfferId != null) runAction { orderUseCases.acceptCounterOffer(counterOfferId) }
            }
            OrderDetailEvent.RejectCounterOffer -> {
                val counterOfferId = (uiState.value as? OrderDetailUiState.Success)?.order?.pendingCounterOffer?.id
                if (counterOfferId != null) runAction { orderUseCases.rejectCounterOffer(counterOfferId) }
            }
            OrderDetailEvent.CancelBeforeAcceptance -> runAction { orderUseCases.cancelOrderBeforeAcceptance(orderId) }
            OrderDetailEvent.MarkEnRoute -> emitEffect(
                OrderDetailEffect.RequestPreciseLocation(TrackingLocationAction.START_ROUTE),
            )
            OrderDetailEvent.MarkArrived -> emitEffect(
                OrderDetailEffect.RequestPreciseLocation(TrackingLocationAction.ARRIVED),
            )
            is OrderDetailEvent.LocationPermissionGranted -> performLocationAction(event.action)
            is OrderDetailEvent.LocationPermissionDenied -> emitEffect(
                OrderDetailEffect.ShowMessage(
                    "Para compartir el recorrido necesitamos ubicación precisa. Podés habilitarla desde los ajustes del teléfono.",
                ),
            )
            OrderDetailEvent.Finalize -> withBookingId { runAction { orderUseCases.finalizeBooking(it) } }
            OrderDetailEvent.GoToRate -> {
                val state = uiState.value as? OrderDetailUiState.Success ?: return
                val bookingId = state.order.bookingId ?: return
                emitEffect(
                    OrderDetailEffect.NavigateToRate(
                        bookingId = bookingId,
                        workerId = state.order.workerId,
                        workerName = state.order.workerName,
                    ),
                )
            }
        }
    }

    private inline fun withBookingId(block: (Long) -> Unit) {
        val bookingId = (uiState.value as? OrderDetailUiState.Success)?.order?.bookingId ?: return
        block(bookingId)
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { OrderDetailUiState.Loading }
            val userResult = authRepository.getCurrentUser()
            viewerRole = (userResult as? AppResult.Success)?.data?.role ?: UserRole.UNKNOWN

            when (val result = orderUseCases.getOrderDetail(orderId)) {
                is AppResult.Success -> {
                    val order = result.data
                    val tracking = order.bookingId?.takeIf {
                        order.bookingStatus == BookingStatus.EN_ROUTE || order.bookingStatus == BookingStatus.IN_PROGRESS
                    }?.let { bookingId ->
                        (orderUseCases.getBookingTracking(bookingId) as? AppResult.Success)?.data
                    }
                    _uiState.update { OrderDetailUiState.Success(order, viewerRole, tracking) }
                    startTrackingRefreshIfNeeded(order.bookingId, order.bookingStatus)
                }
                is AppResult.Error -> _uiState.update { OrderDetailUiState.Error(result.error.userMessage()) }
            }
        }
    }

    private fun performLocationAction(action: TrackingLocationAction) {
        val current = uiState.value as? OrderDetailUiState.Success ?: return
        val bookingId = current.order.bookingId ?: return
        viewModelScope.launch {
            _uiState.update { current.copy(isActionInProgress = true) }
            when (val location = getCurrentLocationUseCase(requirePrecise = true)) {
                is AppResult.Error -> {
                    _uiState.update { current.copy(isActionInProgress = false) }
                    emitEffect(OrderDetailEffect.ShowMessage(location.error.userMessage()))
                }
                is AppResult.Success -> {
                    val result = when (action) {
                        TrackingLocationAction.START_ROUTE -> orderUseCases.markEnRoute(bookingId, location.data)
                        TrackingLocationAction.ARRIVED -> orderUseCases.markArrived(bookingId, location.data)
                    }
                    when (result) {
                        is AppResult.Success -> {
                            when (action) {
                                TrackingLocationAction.START_ROUTE -> emitEffect(
                                    OrderDetailEffect.StartForegroundTracking(bookingId),
                                )
                                TrackingLocationAction.ARRIVED -> emitEffect(OrderDetailEffect.StopForegroundTracking)
                            }
                            load()
                        }
                        is AppResult.Error -> {
                            _uiState.update { current.copy(isActionInProgress = false) }
                            emitEffect(OrderDetailEffect.ShowMessage(result.error.userMessage()))
                        }
                    }
                }
            }
        }
    }

    private fun startTrackingRefreshIfNeeded(bookingId: Long?, status: BookingStatus?) {
        trackingRefreshJob?.cancel()
        if (bookingId == null || status != BookingStatus.EN_ROUTE || viewerRole != UserRole.CLIENT) return
        trackingRefreshJob = viewModelScope.launch {
            while (true) {
                delay(TRACKING_REFRESH_MILLIS)
                when (val result = orderUseCases.getBookingTracking(bookingId)) {
                    is AppResult.Success -> _uiState.update { state ->
                        (state as? OrderDetailUiState.Success)?.copy(tracking = result.data) ?: state
                    }
                    is AppResult.Error -> Unit
                }
            }
        }
    }

    private fun runAction(block: suspend () -> AppResult<Unit>) {
        val current = uiState.value
        if (current !is OrderDetailUiState.Success) return

        viewModelScope.launch {
            _uiState.update { current.copy(isActionInProgress = true) }
            when (val result = block()) {
                is AppResult.Success -> load()
                is AppResult.Error -> {
                    _uiState.update { current.copy(isActionInProgress = false) }
                    emitEffect(OrderDetailEffect.ShowMessage(result.error.userMessage()))
                }
            }
        }
    }

    private fun emitEffect(effect: OrderDetailEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    override fun onCleared() {
        trackingRefreshJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TRACKING_REFRESH_MILLIS = 15_000L
    }
}
