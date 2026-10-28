package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.OrdersListSortPrefs
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
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
    private var loadJob: Job? = null
    private var loadGeneration = 0
    private var lastSuccess: OrdersListUiState.Success? = null
    private var loadedUserId: Long? = null

    init {
        viewModelScope.launch {
            orderRepository.clientOrders.collect { list ->
                applyCachedOrders(list, workerView = false)
            }
        }
        viewModelScope.launch {
            orderRepository.workerOrders.collect { list ->
                applyCachedOrders(list, workerView = true)
            }
        }
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
            is OrdersListEvent.SortSelected -> _uiState.update { state ->
                if (state is OrdersListUiState.Success) {
                    state.copy(sortPrefs = state.sortPrefs.with(state.filter, event.sort))
                } else {
                    state
                }
            }
            is OrdersListEvent.OrderClicked -> emitEffect(OrdersListEffect.NavigateToOrderDetail(event.orderId))
            OrdersListEvent.SearchClicked -> emitEffect(OrdersListEffect.NavigateToSearch)
        }
    }

    private fun load(silent: Boolean = false, refreshing: Boolean = false) {
        val generation = ++loadGeneration
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val snapshot = _uiState.value
            val previous = (snapshot as? OrdersListUiState.Success) ?: lastSuccess?.takeIf { silent || refreshing }
            try {
                when {
                    previous != null && (silent || refreshing) ->
                        _uiState.value = previous.copy(isRefreshing = true)
                    snapshot is OrdersListUiState.Error && refreshing ->
                        _uiState.value = snapshot.copy(isRefreshing = true)
                    !silent -> _uiState.value = OrdersListUiState.Loading
                }
                if (generation != loadGeneration) return@launch
                val userResult = authRepository.getCurrentUser()
                if (generation != loadGeneration) return@launch
                val user = (userResult as? AppResult.Success)?.data
                val sessionChanged = user != null && user.id != loadedUserId
                if (sessionChanged) {
                    loadedUserId = user.id
                    lastSuccess = null
                }
                val retained = if (sessionChanged) null else previous
                val role = user?.role
                val ordersResult = when {
                    userResult is AppResult.Error -> userResult
                    role == UserRole.PROVIDER -> orderUseCases.getWorkerOrders()
                    else -> orderUseCases.getClientOrders()
                }
                if (generation != loadGeneration) return@launch
                when (ordersResult) {
                    is AppResult.Success -> {
                        val next = OrdersListUiState.Success(
                            orders = ordersResult.data,
                            filter = retained?.filter ?: OrdersListFilter.ACTIVE,
                            sortPrefs = retained?.sortPrefs ?: OrdersListSortPrefs(),
                            viewerRole = role ?: UserRole.UNKNOWN,
                        )
                        lastSuccess = next
                        _uiState.value = next
                    }
                    is AppResult.Error -> {
                        val message = ordersResult.error.userMessage()
                        val kept = retained ?: lastSuccess
                        if (kept != null && (refreshing || silent) && !sessionChanged) {
                            val restored = kept.copy(isRefreshing = false)
                            lastSuccess = restored
                            _uiState.value = restored
                            emitEffect(OrdersListEffect.ShowRefreshError(message))
                        } else {
                            _uiState.value = OrdersListUiState.Error(message)
                            if (refreshing) emitEffect(OrdersListEffect.ShowRefreshError(message))
                        }
                    }
                }
            } finally {
                if (generation == loadGeneration) {
                    _uiState.update { state ->
                        when (state) {
                            is OrdersListUiState.Success -> state.copy(isRefreshing = false)
                            is OrdersListUiState.Error -> state.copy(isRefreshing = false)
                            OrdersListUiState.Loading -> state
                        }
                    }
                }
            }
        }
    }

    private fun applyCachedOrders(list: List<com.cocido.oficios.domain.model.ServiceOrderListItem>?, workerView: Boolean) {
        if (list == null) return
        val current = _uiState.value as? OrdersListUiState.Success ?: return
        val isWorker = current.viewerRole == UserRole.PROVIDER
        if (workerView != isWorker) return
        val next = current.copy(orders = list, isRefreshing = false)
        lastSuccess = next
        _uiState.value = next
    }

    private fun emitEffect(effect: OrdersListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
