package com.cocido.oficios.feature.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.validation.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<ChangePasswordEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { loadEmail() }

    fun onEvent(event: ChangePasswordEvent) {
        when (event) {
            is ChangePasswordEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null)
            }
            is ChangePasswordEvent.ConfirmationChanged -> _uiState.update {
                it.copy(confirmation = event.value, confirmationError = null)
            }
            ChangePasswordEvent.Submit -> submit()
            ChangePasswordEvent.Retry -> loadEmail()
            ChangePasswordEvent.Back -> emit(ChangePasswordEffect.NavigateBack)
        }
    }

    private fun loadEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, email = result.data.user.email)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val passwordError = AuthValidators.validatePassword(state.password)
        val confirmationError = AuthValidators.validatePasswordConfirmation(state.password, state.confirmation)
        if (passwordError != null || confirmationError != null) {
            _uiState.update { it.copy(passwordError = passwordError, confirmationError = confirmationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = accountUseCases.changePassword(state.email, state.password, state.confirmation)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, password = "", confirmation = "") }
                    emit(ChangePasswordEffect.Saved("Tu contraseña se actualizó correctamente."))
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun emit(effect: ChangePasswordEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
