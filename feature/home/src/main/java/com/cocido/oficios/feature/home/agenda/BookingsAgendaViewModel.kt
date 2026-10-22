package com.cocido.oficios.feature.home.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.nowInArgentina
import com.cocido.oficios.domain.model.upcomingAgendaAppointments
import com.cocido.oficios.domain.usecase.OrderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
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
class BookingsAgendaViewModel @Inject constructor(
    private val orderUseCases: OrderUseCases,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingsAgendaUiState>(BookingsAgendaUiState.Loading)
    val uiState: StateFlow<BookingsAgendaUiState> = _uiState.asStateFlow()

    private val _effects = Channel<BookingsAgendaEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadJob: Job? = null
    private var skipNextResume = true

    init {
        viewModelScope.launch {
            orderUseCases.workerOrders.collect { list ->
                if (list == null) return@collect
                applyOrders(list)
            }
        }
        viewModelScope.launch {
            orderUseCases.ordersChanged.collect {
                load(refreshing = _uiState.value is BookingsAgendaUiState.Success)
            }
        }
        load()
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        load(refreshing = _uiState.value is BookingsAgendaUiState.Success)
    }

    fun onEvent(event: BookingsAgendaEvent) {
        when (event) {
            BookingsAgendaEvent.Retry -> load()
            BookingsAgendaEvent.Refresh -> load(refreshing = true)
            BookingsAgendaEvent.OpenSlots -> emitEffect(BookingsAgendaEffect.NavigateToSlots)
            is BookingsAgendaEvent.OrderClicked ->
                emitEffect(BookingsAgendaEffect.NavigateToOrderDetail(event.orderId))
        }
    }

    private fun load(refreshing: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val previous = _uiState.value
            try {
                if (refreshing && previous is BookingsAgendaUiState.Success) {
                    _uiState.update { previous.copy(isRefreshing = true) }
                } else if (refreshing && previous is BookingsAgendaUiState.Error) {
                    _uiState.update { previous.copy(isRefreshing = true) }
                } else if (!refreshing) {
                    _uiState.update { BookingsAgendaUiState.Loading }
                }
                when (val result = orderUseCases.getWorkerOrders()) {
                    is AppResult.Success -> applyOrders(result.data)
                    is AppResult.Error -> {
                        val message = result.error.userMessage()
                        if (previous is BookingsAgendaUiState.Success) {
                            _uiState.update { previous.copy(isRefreshing = false) }
                            emitEffect(BookingsAgendaEffect.ShowMessage(message))
                        } else {
                            _uiState.update { BookingsAgendaUiState.Error(message) }
                            if (refreshing) emitEffect(BookingsAgendaEffect.ShowMessage(message))
                        }
                    }
                }
            } finally {
                _uiState.update { state ->
                    when (state) {
                        is BookingsAgendaUiState.Success -> state.copy(isRefreshing = false)
                        is BookingsAgendaUiState.Error -> state.copy(isRefreshing = false)
                        BookingsAgendaUiState.Loading -> state
                    }
                }
            }
        }
    }

    private fun applyOrders(orders: List<ServiceOrderListItem>) {
        val now = nowInArgentina(clock)
        _uiState.update {
            BookingsAgendaUiState.Success(
                orders = upcomingAgendaAppointments(orders, now),
                now = now,
            )
        }
    }

    private fun emitEffect(effect: BookingsAgendaEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
