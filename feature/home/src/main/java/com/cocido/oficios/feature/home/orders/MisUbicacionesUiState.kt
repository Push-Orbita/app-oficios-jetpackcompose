package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientDomicilio

enum class MisUbicacionesMode {
    PICKER,
    ADD,
}

sealed interface MisUbicacionesUiState {
    data object Loading : MisUbicacionesUiState
    data class Error(val message: String) : MisUbicacionesUiState
    data class Success(
        val domicilios: List<ClientDomicilio>,
        val selectedId: Long? = null,
        val mode: MisUbicacionesMode = MisUbicacionesMode.PICKER,
        val barrioQuery: String = "",
        val barrioId: Long? = null,
        val barrioSuggestions: List<CatalogOption> = emptyList(),
        val isSearchingBarrios: Boolean = false,
        val street: String = "",
        val number: String = "",
        val location: AddressLocationSnapshot = AddressLocationSnapshot(),
        val isGeocoding: Boolean = false,
        val addressFavorite: Boolean = false,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
    ) : MisUbicacionesUiState {
        val addressLatitude: Double? get() = location.latitude
        val addressLongitude: Double? get() = location.longitude
    }
}

sealed interface MisUbicacionesEvent {
    data object Retry : MisUbicacionesEvent
    data class LocationSelected(val id: Long) : MisUbicacionesEvent
    data class FavoriteToggled(val id: Long, val favorite: Boolean) : MisUbicacionesEvent
    data object AddClicked : MisUbicacionesEvent
    data object MarkOnMapClicked : MisUbicacionesEvent
    data object CancelAdd : MisUbicacionesEvent
    data class BarrioQueryChanged(val value: String) : MisUbicacionesEvent
    data object BarrioFocused : MisUbicacionesEvent
    data object BarrioSuggestionsDismissed : MisUbicacionesEvent
    data class BarrioSelected(val option: CatalogOption) : MisUbicacionesEvent
    data class StreetChanged(val value: String) : MisUbicacionesEvent
    data class NumberChanged(val value: String) : MisUbicacionesEvent
    data class MapLocationSelected(val latitude: Double, val longitude: Double) : MisUbicacionesEvent
    data object LocateOnMap : MisUbicacionesEvent
    data object ConfirmPin : MisUbicacionesEvent
    data class FavoriteNewChanged(val value: Boolean) : MisUbicacionesEvent
    data object SaveNew : MisUbicacionesEvent
    data object Confirm : MisUbicacionesEvent
}

sealed interface MisUbicacionesEffect {
    data class Confirmed(val selectedId: Long) : MisUbicacionesEffect
    data class ShowMessage(val message: String) : MisUbicacionesEffect
}

internal fun ClientDomicilio.pickerLabel(): String {
    val street = listOfNotNull(calle, numero).joinToString(" ").ifBlank { null }
    return street ?: nombre ?: barrioName ?: "Domicilio $id"
}
