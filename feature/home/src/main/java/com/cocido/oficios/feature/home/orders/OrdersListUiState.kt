package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.OrdersListSort
import com.cocido.oficios.domain.model.OrdersListSortPrefs
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.arrangedFor
import java.time.ZonedDateTime

sealed interface OrdersListUiState {
    data object Loading : OrdersListUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : OrdersListUiState
    data class Success(
        val orders: List<ServiceOrderListItem>,
        val filter: OrdersListFilter = OrdersListFilter.ACTIVE,
        val sortPrefs: OrdersListSortPrefs = OrdersListSortPrefs(),
        val viewerRole: UserRole = UserRole.UNKNOWN,
        val isRefreshing: Boolean = false,
    ) : OrdersListUiState {
        val sort: OrdersListSort get() = sortPrefs.forFilter(filter)

        fun visibleOrders(now: ZonedDateTime): List<ServiceOrderListItem> =
            orders.arrangedFor(filter, now, sort)
    }
}

sealed interface OrdersListEvent {
    data object Retry : OrdersListEvent
    data object Refresh : OrdersListEvent
    data class FilterSelected(val filter: OrdersListFilter) : OrdersListEvent
    data class SortSelected(val sort: OrdersListSort) : OrdersListEvent
    data class OrderClicked(val orderId: Long) : OrdersListEvent
    data object SearchClicked : OrdersListEvent
}

sealed interface OrdersListEffect {
    data class NavigateToOrderDetail(val orderId: Long) : OrdersListEffect
    data object NavigateToSearch : OrdersListEffect
    data class ShowRefreshError(val message: String) : OrdersListEffect
}
