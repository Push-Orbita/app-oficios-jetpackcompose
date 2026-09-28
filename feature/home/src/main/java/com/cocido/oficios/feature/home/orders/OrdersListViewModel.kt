package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.OrderRepository
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
class OrdersListViewModel @Inject constructor(
    private val orderUseCases: OrderUseCases,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersListUiState>(OrdersListUiState.Loading)
    val uiState: StateFlow<OrdersListUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OrdersListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var skipNextResume = true

    init {
        load()
        viewModelScope.launch {
            orderRepository.ordersChanged.collect {
                load(silent = _uiState.value is OrdersListUiState.Success)
            }
        }
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        load(silent = _uiState.value is OrdersListUiState.Success)
    }

    fun onEvent(event: OrdersListEvent) {
        when (event) {
            OrdersListEvent.Retry -> load()
            OrdersListEvent.Refresh -> load(refreshing = true)
            is OrdersListEvent.FilterSelected -> _uiState.update { state ->
                if (state is OrdersListUiState.Success) state.copy(filter = event.filter) else state
            }
            is OrdersListEvent.OrderClicked -> emitEffect(OrdersListEffect.NavigateToOrderDetail(event.orderId))
            OrdersListEvent.SearchClicked -> emitEffect(OrdersListEffect.NavigateToSearch)
        }
    }

    private fun load(silent: Boolean = false, refreshing: Boolean = false) {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val previous = snapshot as? OrdersListUiState.Success
            when {
                previous != null && (silent || refreshing) ->
                    _uiState.update { previous.copy(isRefreshing = true) }
                snapshot is OrdersListUiState.Error && refreshing ->
                    _uiState.update { snapshot.copy(isRefreshing = true) }
                !silent -> _uiState.update { OrdersListUiState.Loading }
            }
            val userResult = authRepository.getCurrentUser()
            val role = (userResult as? AppResult.Success)?.data?.role
            val ordersResult = if (role == UserRole.PROVIDER) {
                orderUseCases.getWorkerOrders()
            } else {
                orderUseCases.getClientOrders()
            }
            when (ordersResult) {
                is AppResult.Success -> _uiState.update {
                    OrdersListUiState.Success(
                        orders = ordersResult.data,
                        filter = previous?.filter ?: OrdersListFilter.ACTIVE,
                    )
                }
                is AppResult.Error -> {
                    if (previous != null && (silent || refreshing)) {
                        _uiState.update { previous.copy(isRefreshing = false) }
                        emitEffect(OrdersListEffect.ShowRefreshError)
                    } else {
                        _uiState.update { OrdersListUiState.Error(ordersResult.error.userMessage()) }
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: OrdersListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
