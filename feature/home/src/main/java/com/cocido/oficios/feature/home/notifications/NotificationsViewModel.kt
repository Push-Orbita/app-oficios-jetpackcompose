package com.cocido.oficios.feature.home.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.navigation.PushDeepLinkMapper
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.notifications.GetNotificationsUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.domain.usecase.notifications.MarkAllNotificationsReadUseCase
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
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val orderUseCases: OrderUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<NotificationsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load(pageNumber = 1, append = false)
        refreshUnreadTotal()
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.Retry -> {
                load(pageNumber = 1, append = false)
                refreshUnreadTotal()
            }
            NotificationsEvent.Refresh -> {
                load(pageNumber = 1, append = false, refreshing = true)
                refreshUnreadTotal()
            }
            NotificationsEvent.MarkAllReadClicked -> markAllRead()
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
        // Optimista: se pinta como leída al instante y se revierte si el PATCH falla.
        if (!item.isRead) {
            setReadState(ids = setOf(item.id), read = true)
            viewModelScope.launch {
                when (val result = markNotificationReadUseCase(item.id)) {
                    is AppResult.Success -> Unit
                    is AppResult.Error -> {
                        setReadState(ids = setOf(item.id), read = false)
                        emitEffect(NotificationsEffect.ShowMessage(result.error.userMessage()))
                    }
                }
            }
        }
        viewModelScope.launch {
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

    private fun markAllRead() {
        val state = _uiState.value
        if (!state.canMarkAllRead) return
        val previouslyUnread = state.items.filterNot { it.isRead }.map { it.id }.toSet()
        val previousTotal = state.unreadTotal
        _uiState.update { it.copy(isMarkingAllRead = true) }
        setReadState(ids = previouslyUnread, read = true, unreadTotal = 0)
        viewModelScope.launch {
            when (val result = markAllNotificationsReadUseCase()) {
                is AppResult.Success -> {
                    val failed = result.data.failedIds.toSet()
                    if (failed.isEmpty()) {
                        _uiState.update { it.copy(isMarkingAllRead = false) }
                        emitEffect(NotificationsEffect.ShowSuccess(MARK_ALL_SUCCESS))
                    } else {
                        setReadState(ids = previouslyUnread intersect failed, read = false, unreadTotal = failed.size)
                        _uiState.update { it.copy(isMarkingAllRead = false) }
                        emitEffect(NotificationsEffect.ShowMessage(markAllPartialMessage(failed.size)))
                    }
                }
                is AppResult.Error -> {
                    setReadState(ids = previouslyUnread, read = false, unreadTotal = previousTotal)
                    _uiState.update { it.copy(isMarkingAllRead = false) }
                    emitEffect(NotificationsEffect.ShowMessage(result.error.userMessage()))
                }
            }
            if (_uiState.value.soloNoLeidas) load(pageNumber = 1, append = false, refreshing = true)
        }
    }

    /** Cambia `isRead` de los ids indicados y ajusta el total de no leídas en consecuencia. */
    private fun setReadState(ids: Set<Long>, read: Boolean, unreadTotal: Int? = null) {
        if (ids.isEmpty() && unreadTotal == null) return
        _uiState.update { state ->
            val changed = state.items.count { it.id in ids && it.isRead != read }
            val newTotal = unreadTotal ?: state.unreadTotal?.let {
                (if (read) it - changed else it + changed).coerceAtLeast(0)
            }
            state.copy(
                items = state.items.map { current ->
                    if (current.id in ids) current.copy(isRead = read) else current
                },
                unreadTotal = newTotal,
            )
        }
    }

    private fun refreshUnreadTotal() {
        viewModelScope.launch {
            val result = getUnreadNotificationCountUseCase()
            if (result is AppResult.Success && !_uiState.value.isMarkingAllRead) {
                _uiState.update { it.copy(unreadTotal = result.data) }
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

    internal companion object {
        const val MARK_ALL_SUCCESS = "Marcamos todas las notificaciones como leídas."

        fun markAllPartialMessage(failedCount: Int): String =
            if (failedCount == 1) {
                "No pudimos marcar 1 notificación. Probá de nuevo."
            } else {
                "No pudimos marcar $failedCount notificaciones. Probá de nuevo."
            }
    }
}
