package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.domain.model.CatalogOption

data class CommercialLocalUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isLocating: Boolean = false,
    val missingService: Boolean = false,
    val loadFailed: Boolean = false,
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = selectedBarrioId != null && calle.isNotBlank() && latitud != null && longitud != null
}

sealed interface CommercialLocalEvent {
    data object Retry : CommercialLocalEvent
    data object Back : CommercialLocalEvent
    data object AddService : CommercialLocalEvent
    data class BarrioQueryChanged(val value: String) : CommercialLocalEvent
    data class BarrioSelected(val option: CatalogOption) : CommercialLocalEvent
    data object BarrioSuggestionsDismissed : CommercialLocalEvent
    data class CalleChanged(val value: String) : CommercialLocalEvent
    data class MapLocationSelected(val latitude: Double, val longitude: Double) : CommercialLocalEvent
    data object UseCurrentLocation : CommercialLocalEvent
    data object LocationPermissionDenied : CommercialLocalEvent
    data object Submit : CommercialLocalEvent
}

sealed interface CommercialLocalEffect {
    data object NavigateBack : CommercialLocalEffect
    data object NavigateToAddService : CommercialLocalEffect
    data class ShowSuccess(val message: String) : CommercialLocalEffect
    data class ShowError(val message: String) : CommercialLocalEffect
}
