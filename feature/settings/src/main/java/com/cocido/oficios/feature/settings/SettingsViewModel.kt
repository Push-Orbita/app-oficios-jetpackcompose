package com.cocido.oficios.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.domain.usecase.auth.LogoutUseCase
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val accountUseCases: AccountUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.Retry -> load()
            SettingsEvent.OpenPersonalData -> emitEffect(SettingsEffect.NavigateToPersonalData)
            SettingsEvent.OpenClientData -> emitEffect(SettingsEffect.NavigateToClientData)
            SettingsEvent.OpenProfessionalData -> emitEffect(SettingsEffect.NavigateToProfessionalData)
            SettingsEvent.OpenServices -> emitEffect(SettingsEffect.NavigateToServices)
            SettingsEvent.OpenPortfolio -> emitEffect(SettingsEffect.NavigateToPortfolio)
            SettingsEvent.OpenChangePassword -> emitEffect(SettingsEffect.NavigateToChangePassword)
            SettingsEvent.OpenDeleteAccount -> emitEffect(SettingsEffect.NavigateToDeleteAccount)
            SettingsEvent.OpenTerms -> emitEffect(SettingsEffect.NavigateToTerms)
            SettingsEvent.Logout -> logout()
            SettingsEvent.NavigateBack -> Unit
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, profile = result.data, errorMessage = null)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun logout() {
        if (_uiState.value.isLoggingOut) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            logoutUseCase()
            _loggedOut.update { true }
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }

    private fun emitEffect(effect: SettingsEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
