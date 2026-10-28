package com.cocido.oficios.feature.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.core.common.splash.SplashGate
import com.cocido.oficios.domain.usecase.auth.ClearEphemeralSessionIfNeededUseCase
import com.cocido.oficios.domain.usecase.auth.RefreshTokenIfNeededUseCase
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val resolveInitialDestinationUseCase: ResolveInitialDestinationUseCase,
    private val clearEphemeralSessionIfNeededUseCase: ClearEphemeralSessionIfNeededUseCase,
    private val refreshTokenIfNeededUseCase: RefreshTokenIfNeededUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        resolveDestination()
    }

    fun retry() {
        resolveDestination()
    }

    private fun resolveDestination() {
        viewModelScope.launch {
            _uiState.update { SplashUiState.Loading }
            clearEphemeralSessionIfNeededUseCase()
            refreshTokenIfNeededUseCase()
            when (val result = resolveInitialDestinationUseCase()) {
                is AppResult.Success -> _uiState.update { SplashUiState.Ready(result.data) }
                is AppResult.Error -> _uiState.update { SplashUiState.Error(result.error.userMessage()) }
            }
            SplashGate.release()
        }
    }
}
