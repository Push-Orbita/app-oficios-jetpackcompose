package com.cocido.oficios.domain.model

sealed interface InitialDestination {
    data object Login : InitialDestination
    data object ClientOnboarding : InitialDestination
    data object ProviderOnboarding : InitialDestination
    data object ProviderPendingApproval : InitialDestination
    data object ProviderRejected : InitialDestination
    data object ClientHome : InitialDestination
    data object ProviderHome : InitialDestination
    data object ClientBlocked : InitialDestination
    data object ClientInactive : InitialDestination
}
