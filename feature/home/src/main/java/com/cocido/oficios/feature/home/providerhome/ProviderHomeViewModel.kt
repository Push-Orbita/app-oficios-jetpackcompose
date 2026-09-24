package com.cocido.oficios.feature.home.providerhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.GetProviderHomeUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
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
class ProviderHomeViewModel @Inject constructor(
    private val getProviderHomeUseCase: GetProviderHomeUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderHomeUiState>(ProviderHomeUiState.Loading)
    val uiState: StateFlow<ProviderHomeUiState> = _uiState.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _effects = Channel<ProviderHomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadHome()
        refreshUnreadCount()
    }

    fun onEvent(event: ProviderHomeEvent) {
        when (event) {
            ProviderHomeEvent.Retry -> loadHome()
            ProviderHomeEvent.OpenNotifications -> emitEffect(ProviderHomeEffect.NavigateToNotifications)
            ProviderHomeEvent.CompleteProfileClicked -> emitEffect(ProviderHomeEffect.NavigateToCompleteProfile)
            ProviderHomeEvent.ViewFullAgendaClicked -> emitEffect(ProviderHomeEffect.NavigateToAgenda)
            is ProviderHomeEvent.OrderClicked -> emitEffect(ProviderHomeEffect.NavigateToOrderDetail(event.orderId))
            ProviderHomeEvent.NavigateToSettings -> emitEffect(ProviderHomeEffect.NavigateToSettings)
            ProviderHomeEvent.NavigateToFavorites -> emitEffect(ProviderHomeEffect.NavigateToFavorites)
            ProviderHomeEvent.NavigateToHelp -> emitEffect(ProviderHomeEffect.NavigateToHelp)
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

    private fun loadHome() {
        viewModelScope.launch {
            _uiState.update { ProviderHomeUiState.Loading }
            when (val result = getProviderHomeUseCase()) {
                is AppResult.Success -> _uiState.update { ProviderHomeUiState.Success(result.data) }
                is AppResult.Error -> _uiState.update { ProviderHomeUiState.Error(result.error.userMessage()) }
            }
        }
    }

    private fun emitEffect(effect: ProviderHomeEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
