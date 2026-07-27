package com.cocido.oficios.feature.home.providerhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.GetProviderHomeUseCase
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderHomeUiState>(ProviderHomeUiState.Loading)
    val uiState: StateFlow<ProviderHomeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ProviderHomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadHome()
    }

    fun onEvent(event: ProviderHomeEvent) {
        when (event) {
            ProviderHomeEvent.Retry -> loadHome()
            ProviderHomeEvent.OpenNotifications -> emitEffect(ProviderHomeEffect.ShowComingSoon)
            ProviderHomeEvent.CompleteProfileClicked -> emitEffect(ProviderHomeEffect.NavigateToCompleteProfile)
            ProviderHomeEvent.ViewFullAgendaClicked -> emitEffect(ProviderHomeEffect.NavigateToOrdersList)
            is ProviderHomeEvent.OrderClicked -> emitEffect(ProviderHomeEffect.NavigateToOrderDetail(event.orderId))
            ProviderHomeEvent.NavigateToSettings -> emitEffect(ProviderHomeEffect.NavigateToSettings)
            ProviderHomeEvent.NavigateToProfile -> emitEffect(ProviderHomeEffect.ShowComingSoon)
            ProviderHomeEvent.NavigateToFavorites -> emitEffect(ProviderHomeEffect.ShowComingSoon)
            ProviderHomeEvent.NavigateToHelp -> emitEffect(ProviderHomeEffect.ShowComingSoon)
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
