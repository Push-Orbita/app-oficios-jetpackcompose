package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.ServiceOrderListItem

sealed interface OrdersListUiState {
    data object Loading : OrdersListUiState
    data class Error(val message: String) : OrdersListUiState
    data class Success(val orders: List<ServiceOrderListItem>) : OrdersListUiState
}

sealed interface OrdersListEvent {
    data object Retry : OrdersListEvent
    data class OrderClicked(val orderId: Long) : OrdersListEvent
}

sealed interface OrdersListEffect {
    data class NavigateToOrderDetail(val orderId: Long) : OrdersListEffect
}
