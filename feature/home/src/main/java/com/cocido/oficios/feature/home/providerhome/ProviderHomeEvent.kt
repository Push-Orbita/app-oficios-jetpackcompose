package com.cocido.oficios.feature.home.providerhome

sealed interface ProviderHomeEvent {
    data object Retry : ProviderHomeEvent
    data object OpenNotifications : ProviderHomeEvent
    data object CompleteProfileClicked : ProviderHomeEvent
    data object ViewFullAgendaClicked : ProviderHomeEvent
    data object NavigateToSettings : ProviderHomeEvent
    data object NavigateToProfile : ProviderHomeEvent
    data object NavigateToFavorites : ProviderHomeEvent
    data object NavigateToHelp : ProviderHomeEvent
}

sealed interface ProviderHomeEffect {
    data object NavigateToSettings : ProviderHomeEffect
    data object NavigateToCompleteProfile : ProviderHomeEffect
    data class ShowMessage(val message: String) : ProviderHomeEffect
    data object ShowComingSoon : ProviderHomeEffect
}
