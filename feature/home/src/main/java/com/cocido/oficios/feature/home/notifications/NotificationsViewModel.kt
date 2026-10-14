package com.cocido.oficios.feature.home.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.navigation.PushDeepLinkMapper
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.notifications.GetNotificationsUseCase
import com.cocido.oficios.domain.usecase.notifications.MarkNotificationReadUseCase
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
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val orderUseCases: OrderUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<NotificationsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load(pageNumber = 1, append = false)
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.Retry -> load(pageNumber = 1, append = false)
            NotificationsEvent.Refresh -> load(pageNumber = 1, append = false, refreshing = true)
            NotificationsEvent.LoadMore -> {
                val state = _uiState.value
                if (!state.hasMore || state.isLoadingMore) return
                load(pageNumber = state.pageNumber + 1, append = true)
            }
            NotificationsEvent.ToggleUnreadFilter -> {
                _uiState.update { it.copy(soloNoLeidas = !it.soloNoLeidas, pageNumber = 1) }
                load(pageNumber = 1, append = false)
            }
            is NotificationsEvent.NotificationClicked -> onNotificationClicked(event.item)
        }
    }

    private fun onNotificationClicked(item: NotificationItem) {
        viewModelScope.launch {
            if (!item.isRead) {
                when (val result = markNotificationReadUseCase(item.id)) {
                    is AppResult.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                items = state.items.map { current ->
                                    if (current.id == item.id) current.copy(isRead = true) else current
                                },
                            )
                        }
                    }
                    is AppResult.Error -> emitEffect(NotificationsEffect.ShowMessage(result.error.userMessage()))
                }
            }
            val orderId = item.orderId ?: item.bookingId?.let { bookingId ->
                (orderUseCases.findOrderIdForBooking(bookingId) as? AppResult.Success)?.data
            }
            val route = PushDeepLinkMapper.toInternalRoute(item.deepLink)
                ?: orderId?.let { "orders/$it" }
            if (route != null) {
                emitEffect(NotificationsEffect.NavigateToDeepLink(route))
            }
        }
    }

    private fun load(pageNumber: Int, append: Boolean, refreshing: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            val hadItems = current.items.isNotEmpty()
            try {
                _uiState.update {
                    it.copy(
                        isLoading = !append && !refreshing,
                        isRefreshing = refreshing,
                        isLoadingMore = append,
                        errorMessage = if (refreshing) it.errorMessage else null,
                    )
                }
                when (
                    val result = getNotificationsUseCase(
                        pageNumber = pageNumber,
                        soloNoLeidas = current.soloNoLeidas,
                    )
                ) {
                    is AppResult.Success -> {
                        val page = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                items = if (append) it.items + page.items else page.items,
                                pageNumber = page.pageNumber,
                                hasMore = page.hasMore,
                                errorMessage = null,
                            )
                        }
                    }
                    is AppResult.Error -> {
                        val message = result.error.userMessage()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                errorMessage = if (hadItems) null else message,
                            )
                        }
                        if (refreshing || hadItems) emitEffect(NotificationsEffect.ShowMessage(message))
                    }
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false, isLoadingMore = false) }
            }
        }
    }

    private fun emitEffect(effect: NotificationsEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
