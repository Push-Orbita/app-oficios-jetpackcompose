package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderUseCases: OrderUseCases,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L
    private var viewerRole: UserRole = UserRole.UNKNOWN

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
            OrderDetailEvent.MarkEnRoute -> withBookingId { runAction { orderUseCases.markEnRoute(it) } }
            OrderDetailEvent.MarkArrived -> withBookingId { runAction { orderUseCases.markArrived(it) } }
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
                is AppResult.Success -> _uiState.update { OrderDetailUiState.Success(result.data, viewerRole) }
                is AppResult.Error -> _uiState.update { OrderDetailUiState.Error(result.error.userMessage()) }
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
}
