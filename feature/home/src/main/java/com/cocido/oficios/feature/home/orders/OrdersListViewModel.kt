package com.cocido.oficios.feature.home.orders

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
class OrdersListViewModel @Inject constructor(
    private val orderUseCases: OrderUseCases,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersListUiState>(OrdersListUiState.Loading)
    val uiState: StateFlow<OrdersListUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OrdersListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: OrdersListEvent) {
        when (event) {
            OrdersListEvent.Retry -> load()
            is OrdersListEvent.OrderClicked -> emitEffect(OrdersListEffect.NavigateToOrderDetail(event.orderId))
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { OrdersListUiState.Loading }
            val userResult = authRepository.getCurrentUser()
            val role = (userResult as? AppResult.Success)?.data?.role

            val ordersResult = if (role == UserRole.PROVIDER) {
                orderUseCases.getWorkerOrders()
            } else {
                orderUseCases.getClientOrders()
            }

            when (ordersResult) {
                is AppResult.Success -> _uiState.update { OrdersListUiState.Success(ordersResult.data) }
                is AppResult.Error -> _uiState.update { OrdersListUiState.Error(ordersResult.error.userMessage()) }
            }
        }
    }

    private fun emitEffect(effect: OrdersListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
