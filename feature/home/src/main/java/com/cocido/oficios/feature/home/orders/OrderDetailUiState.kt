package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.UserRole

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
    data class Success(
        val order: ServiceOrder,
        val viewerRole: UserRole,
        val isActionInProgress: Boolean = false,
    ) : OrderDetailUiState
}

sealed interface OrderDetailEvent {
    data object Retry : OrderDetailEvent
    data object Accept : OrderDetailEvent
    data class Reject(val reason: String) : OrderDetailEvent
    data object OpenCounterOffer : OrderDetailEvent
    data object AcceptCounterOffer : OrderDetailEvent
    data object RejectCounterOffer : OrderDetailEvent
    data object CancelBeforeAcceptance : OrderDetailEvent
    data object MarkEnRoute : OrderDetailEvent
    data object MarkArrived : OrderDetailEvent
    data object Finalize : OrderDetailEvent
    data object GoToRate : OrderDetailEvent
}

sealed interface OrderDetailEffect {
    data class ShowMessage(val message: String) : OrderDetailEffect
    data class NavigateToCounterOffer(val orderId: Long) : OrderDetailEffect
    data class NavigateToRate(val bookingId: Long, val workerId: Long, val workerName: String) : OrderDetailEffect
}
