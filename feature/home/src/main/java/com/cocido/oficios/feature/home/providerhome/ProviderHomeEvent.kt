package com.cocido.oficios.feature.home.providerhome

sealed interface ProviderHomeEvent {
    data object Retry : ProviderHomeEvent
    data object Refresh : ProviderHomeEvent
    data object OpenNotifications : ProviderHomeEvent
    data object GalleryTileClicked : ProviderHomeEvent
    data object CommercialLocalTileClicked : ProviderHomeEvent
    data object CertificateTileClicked : ProviderHomeEvent
    data object CompleteProfileLinkClicked : ProviderHomeEvent
    data object ViewFullAgendaClicked : ProviderHomeEvent
    data object ConfigureAgendaClicked : ProviderHomeEvent
    data class OrderClicked(val orderId: Long) : ProviderHomeEvent
    data object NavigateToSettings : ProviderHomeEvent
    data object NavigateToFavorites : ProviderHomeEvent
    data object NavigateToHelp : ProviderHomeEvent
}

sealed interface ProviderHomeEffect {
    data object NavigateToSettings : ProviderHomeEffect
    data object NavigateToGallery : ProviderHomeEffect
    data object NavigateToCommercialLocal : ProviderHomeEffect
    data object NavigateToCertificate : ProviderHomeEffect
    data object NavigateToOrdersList : ProviderHomeEffect
    data object NavigateToAgenda : ProviderHomeEffect
    data object NavigateToAgendaConfig : ProviderHomeEffect
    data class NavigateToOrderDetail(val orderId: Long) : ProviderHomeEffect
    data class ShowMessage(val message: String) : ProviderHomeEffect
    data object NavigateToFavorites : ProviderHomeEffect
    data object NavigateToHelp : ProviderHomeEffect
    data object NavigateToNotifications : ProviderHomeEffect
}
