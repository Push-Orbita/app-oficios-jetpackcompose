package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.arrangedFor

sealed interface OrdersListUiState {
    data object Loading : OrdersListUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : OrdersListUiState
    data class Success(
        val orders: List<ServiceOrderListItem>,
        val filter: OrdersListFilter = OrdersListFilter.ACTIVE,
        val isRefreshing: Boolean = false,
    ) : OrdersListUiState {
        val visibleOrders: List<ServiceOrderListItem> get() = orders.arrangedFor(filter)
    }
}

sealed interface OrdersListEvent {
    data object Retry : OrdersListEvent
    data object Refresh : OrdersListEvent
    data class FilterSelected(val filter: OrdersListFilter) : OrdersListEvent
    data class OrderClicked(val orderId: Long) : OrdersListEvent
    data object SearchClicked : OrdersListEvent
}

sealed interface OrdersListEffect {
    data class NavigateToOrderDetail(val orderId: Long) : OrdersListEffect
    data object NavigateToSearch : OrdersListEffect
}
