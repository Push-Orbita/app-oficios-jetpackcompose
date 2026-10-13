package com.cocido.oficios.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.usecase.GetClientHomeUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
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
class HomeViewModel @Inject constructor(
    private val getClientHomeUseCase: GetClientHomeUseCase,
    private val trackRecentSearchUseCase: TrackRecentSearchUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadHome()
        refreshUnreadCount()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Retry -> loadHome()
            HomeEvent.Refresh -> loadHome(refreshing = true)
            HomeEvent.OpenSearch -> emitEffect(HomeEffect.NavigateToSearch)
            HomeEvent.OpenNotifications -> emitEffect(HomeEffect.NavigateToNotifications)
            is HomeEvent.SearchSubmitted -> onSearchSubmitted(event.query)
            HomeEvent.VoiceSearch -> emitComingSoon()
            is HomeEvent.RecentSearchSelected -> onRecentSearchSelected(event.entry)
            HomeEvent.NavigateToSettings -> emitEffect(HomeEffect.NavigateToSettings)
            HomeEvent.NavigateToFavorites -> emitEffect(HomeEffect.NavigateToFavorites)
            HomeEvent.NavigateToHelp -> emitEffect(HomeEffect.NavigateToHelp)
            is HomeEvent.WorkerViewProfile -> emitEffect(HomeEffect.NavigateToWorker(event.workerId))
            is HomeEvent.WorkerContact -> emitEffect(HomeEffect.NavigateToHire(event.workerId))
            HomeEvent.SeeAllWorkers -> emitEffect(HomeEffect.NavigateToSearchResults())
            HomeEvent.OpenMyOrders -> emitEffect(HomeEffect.NavigateToMyOrders)
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            when (val result = getUnreadNotificationCountUseCase()) {
                is AppResult.Success -> _unreadNotificationCount.update { result.data }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun onRecentSearchSelected(entry: RecentSearchEntry) {
        viewModelScope.launch {
            trackRecentSearchUseCase(entry)
            when (entry.type) {
                RecentSearchType.CATEGORY -> {
                    emitEffect(HomeEffect.NavigateToRubros(entry.id, entry.label))
                }
                RecentSearchType.RUBRO -> {
                    emitEffect(
                        HomeEffect.NavigateToSearchResults(
                            rubroId = entry.rubroId ?: entry.id,
                            rubroLabel = entry.label,
                            categoryId = entry.categoryId,
                        ),
                    )
                }
                RecentSearchType.SUBRUBRO -> {
                    emitEffect(
                        HomeEffect.NavigateToSearchResults(
                            rubroId = entry.rubroId ?: 0L,
                            rubroLabel = entry.label,
                            subRubroId = entry.id,
                            subRubroLabel = entry.label,
                            categoryId = entry.categoryId,
                        ),
                    )
                }
                RecentSearchType.QUERY -> {
                    emitEffect(HomeEffect.NavigateToSearchResults(query = entry.label))
                }
            }
        }
    }

    private fun onSearchSubmitted(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emitEffect(HomeEffect.NavigateToSearch)
            return
        }
        viewModelScope.launch {
            trackRecentSearchUseCase(
                RecentSearchEntry(
                    type = RecentSearchType.QUERY,
                    id = trimmed.hashCode().toLong(),
                    label = trimmed,
                ),
            )
            emitEffect(HomeEffect.NavigateToSearchResults(query = trimmed))
        }
    }

    private fun loadHome(refreshing: Boolean = false) {
        viewModelScope.launch {
            val previous = _uiState.value
            if (refreshing && previous is HomeUiState.Success) {
                _uiState.update { previous.copy(isRefreshing = true) }
            } else if (refreshing && previous is HomeUiState.Error) {
                _uiState.update { previous.copy(isRefreshing = true) }
            } else {
                _uiState.update { HomeUiState.Loading }
            }
            when (val result = getClientHomeUseCase()) {
                is AppResult.Success -> _uiState.update { HomeUiState.Success(result.data) }
                is AppResult.Error -> {
                    if (refreshing && previous is HomeUiState.Success) {
                        _uiState.update { previous.copy(isRefreshing = false) }
                    } else {
                        _uiState.update { HomeUiState.Error(result.error.userMessage()) }
                    }
                }
            }
        }
    }

    private fun emitComingSoon() {
        emitEffect(HomeEffect.ShowComingSoon)
    }

    private fun emitEffect(effect: HomeEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
