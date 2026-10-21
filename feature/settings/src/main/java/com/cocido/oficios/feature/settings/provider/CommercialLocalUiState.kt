package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption

data class CommercialLocalUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isGeocoding: Boolean = false,
    val missingService: Boolean = false,
    val loadFailed: Boolean = false,
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val isSearchingBarrios: Boolean = false,
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val location: AddressLocationSnapshot = AddressLocationSnapshot(),
    val errorMessage: String? = null,
) {
    val latitud: Double? get() = location.latitude
    val longitud: Double? get() = location.longitude
    val locationPhase: AddressLocationPhase get() = location.phase
    val canSubmit: Boolean
        get() = selectedBarrioId != null && calle.isNotBlank()
}

sealed interface CommercialLocalEvent {
    data object Retry : CommercialLocalEvent
    data object Back : CommercialLocalEvent
    data object AddService : CommercialLocalEvent
    data class BarrioQueryChanged(val value: String) : CommercialLocalEvent
    data object BarrioFocused : CommercialLocalEvent
    data class BarrioSelected(val option: CatalogOption) : CommercialLocalEvent
    data object BarrioSuggestionsDismissed : CommercialLocalEvent
    data class CalleChanged(val value: String) : CommercialLocalEvent
    data class NumeroChanged(val value: String) : CommercialLocalEvent
    data class MapLocationSelected(val latitude: Double, val longitude: Double) : CommercialLocalEvent
    data object LocateOnMap : CommercialLocalEvent
    data object ConfirmPin : CommercialLocalEvent
    data object Submit : CommercialLocalEvent
}

sealed interface CommercialLocalEffect {
    data object NavigateBack : CommercialLocalEffect
    data object NavigateToAddService : CommercialLocalEffect
    data class ShowSuccess(val message: String) : CommercialLocalEffect
    data class ShowError(val message: String) : CommercialLocalEffect
}
