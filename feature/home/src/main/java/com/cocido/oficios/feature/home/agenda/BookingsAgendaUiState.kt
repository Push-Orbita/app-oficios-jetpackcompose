package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.domain.model.ServiceOrderListItem
import java.time.ZonedDateTime

sealed interface BookingsAgendaUiState {
    data object Loading : BookingsAgendaUiState

    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : BookingsAgendaUiState

    data class Success(
        val orders: List<ServiceOrderListItem>,
        val now: ZonedDateTime,
        val isRefreshing: Boolean = false,
    ) : BookingsAgendaUiState
}

sealed interface BookingsAgendaEvent {
    data object Retry : BookingsAgendaEvent
    data object Refresh : BookingsAgendaEvent
    data object OpenSlots : BookingsAgendaEvent
    data class OrderClicked(val orderId: Long) : BookingsAgendaEvent
}

sealed interface BookingsAgendaEffect {
    data object NavigateToSlots : BookingsAgendaEffect
    data class NavigateToOrderDetail(val orderId: Long) : BookingsAgendaEffect
    data class ShowMessage(val message: String) : BookingsAgendaEffect
}
