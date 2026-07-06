package com.cocido.oficios.feature.auth.splash

import com.cocido.oficios.domain.model.InitialDestination

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Ready(val destination: InitialDestination) : SplashUiState
    data class Error(val message: String) : SplashUiState
}
