package com.cocido.oficios.feature.home

sealed interface HomeEvent {
    data object LoadContent : HomeEvent
    data object Retry : HomeEvent
    data object SimulateError : HomeEvent
    data object NavigateToSettings : HomeEvent
}
