package com.cocido.oficios.feature.home.providerhome

sealed interface ProviderHomeEvent {
    data object Retry : ProviderHomeEvent
    data object Refresh : ProviderHomeEvent
    data object OpenNotifications : ProviderHomeEvent
    data object CompleteProfileClicked : ProviderHomeEvent
    data object ViewFullAgendaClicked : ProviderHomeEvent
    data class OrderClicked(val orderId: Long) : ProviderHomeEvent
    data object NavigateToSettings : ProviderHomeEvent
    data object NavigateToFavorites : ProviderHomeEvent
    data object NavigateToHelp : ProviderHomeEvent
}

sealed interface ProviderHomeEffect {
    data object NavigateToSettings : ProviderHomeEffect
    data object NavigateToCompleteProfile : ProviderHomeEffect
    data object NavigateToOrdersList : ProviderHomeEffect
    data object NavigateToAgenda : ProviderHomeEffect
    data class NavigateToOrderDetail(val orderId: Long) : ProviderHomeEffect
    data class ShowMessage(val message: String) : ProviderHomeEffect
    data object NavigateToFavorites : ProviderHomeEffect
    data object NavigateToHelp : ProviderHomeEffect
    data object NavigateToNotifications : ProviderHomeEffect
}
