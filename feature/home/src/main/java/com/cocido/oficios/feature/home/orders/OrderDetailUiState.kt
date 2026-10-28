package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.BookingTracking

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Error(val message: String, val isRefreshing: Boolean = false) : OrderDetailUiState
    data class Success(
        val order: ServiceOrder,
        val viewerRole: UserRole,
        val tracking: BookingTracking? = null,
        val isActionInProgress: Boolean = false,
        val isRefreshing: Boolean = false,
        val completionCodeError: String? = null,
        /** El cliente está EN_CURSO pero no pudimos traer ningún código (ni el del detalle ni el fresco). */
        val completionCodeUnavailable: Boolean = false,
    ) : OrderDetailUiState
}

sealed interface OrderDetailEvent {
    data object Retry : OrderDetailEvent
    data object Refresh : OrderDetailEvent
    data object Accept : OrderDetailEvent
    data class Reject(val reason: String) : OrderDetailEvent
    data object OpenCounterOffer : OrderDetailEvent
    data object AcceptCounterOffer : OrderDetailEvent
    data object RejectCounterOffer : OrderDetailEvent
    data object CancelBeforeAcceptance : OrderDetailEvent
    data object MarkEnRoute : OrderDetailEvent
    data object MarkArrived : OrderDetailEvent
    data class LocationPermissionGranted(val action: TrackingLocationAction) : OrderDetailEvent
    data class LocationPermissionDenied(val action: TrackingLocationAction) : OrderDetailEvent
    data object Finalize : OrderDetailEvent
    data class FinalizeWithCode(val code: String) : OrderDetailEvent
    data object ClearCompletionError : OrderDetailEvent
    data object GoToRate : OrderDetailEvent
    data object OpenWorkerProfile : OrderDetailEvent
    data class CancelConfirmed(val reason: String?) : OrderDetailEvent
}

enum class TrackingLocationAction { START_ROUTE, ARRIVED }

sealed interface OrderDetailEffect {
    data class ShowMessage(val message: String) : OrderDetailEffect
    data class ShowSuccess(val message: String) : OrderDetailEffect
    data class NavigateToCounterOffer(val orderId: Long) : OrderDetailEffect
    data class NavigateToRate(val bookingId: Long, val workerId: Long, val workerName: String) : OrderDetailEffect
    data class RequestPreciseLocation(val action: TrackingLocationAction) : OrderDetailEffect
    data class StartForegroundTracking(val bookingId: Long) : OrderDetailEffect
    data object StopForegroundTracking : OrderDetailEffect
    data class NavigateToWorker(val workerId: Long) : OrderDetailEffect
    data object CompletionConfirmed : OrderDetailEffect
}
