package com.cocido.oficios.feature.settings.client

import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientDomicilio

enum class ClientDataMode { LIST, CONTACT, DOMICILIO }

sealed interface ClientDataDeleteTarget {
    val id: Long
    val label: String

    data class Contact(override val id: Long, override val label: String) : ClientDataDeleteTarget
    data class Domicilio(override val id: Long, override val label: String) : ClientDataDeleteTarget
}

data class ClientDataUiState(
    val isLoading: Boolean = true,
    val personaId: Long? = null,
    val contacts: List<ClientContacto> = emptyList(),
    val domicilios: List<ClientDomicilio> = emptyList(),
    val mode: ClientDataMode = ClientDataMode.LIST,
    val editingId: Long? = null,
    val contactName: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val contactFavorite: Boolean = false,
    val originalPhone: String = "",
    val originalPhoneVerified: Boolean = false,
    val addressName: String = "",
    val barrioQuery: String = "",
    val barrioId: Long? = null,
    val barrioSuggestions: List<CatalogOption> = emptyList(),
    val street: String = "",
    val number: String = "",
    val floor: String = "",
    val apartment: String = "",
    val houseTower: String = "",
    val addressLatitude: Double? = null,
    val addressLongitude: Double? = null,
    val isLocating: Boolean = false,
    val addressFavorite: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val pendingDelete: ClientDataDeleteTarget? = null,
)

sealed interface ClientDataEvent {
    data object Retry : ClientDataEvent
    data object Back : ClientDataEvent
    data object CloseEditor : ClientDataEvent
    data object AddContact : ClientDataEvent
    data class EditContact(val contact: ClientContacto) : ClientDataEvent
    data object AddDomicilio : ClientDataEvent
    data class EditDomicilio(val domicilio: ClientDomicilio) : ClientDataEvent
    data class ContactNameChanged(val value: String) : ClientDataEvent
    data class ContactPhoneChanged(val value: String) : ClientDataEvent
    data class ContactEmailChanged(val value: String) : ClientDataEvent
    data class ContactFavoriteChanged(val value: Boolean) : ClientDataEvent
    data object SaveContact : ClientDataEvent
    data class AddressNameChanged(val value: String) : ClientDataEvent
    data class BarrioQueryChanged(val value: String) : ClientDataEvent
    data class BarrioSelected(val option: CatalogOption) : ClientDataEvent
    data class StreetChanged(val value: String) : ClientDataEvent
    data class NumberChanged(val value: String) : ClientDataEvent
    data class FloorChanged(val value: String) : ClientDataEvent
    data class ApartmentChanged(val value: String) : ClientDataEvent
    data class HouseTowerChanged(val value: String) : ClientDataEvent
    data class MapLocationSelected(val latitude: Double, val longitude: Double) : ClientDataEvent
    data object UseCurrentLocation : ClientDataEvent
    data object LocationPermissionDenied : ClientDataEvent
    data class AddressFavoriteChanged(val value: Boolean) : ClientDataEvent
    data object SaveDomicilio : ClientDataEvent
    data class ToggleContactFavorite(val id: Long, val favorite: Boolean) : ClientDataEvent
    data class ToggleDomicilioFavorite(val id: Long, val favorite: Boolean) : ClientDataEvent
    data class RequestDelete(val target: ClientDataDeleteTarget) : ClientDataEvent
    data object ConfirmDelete : ClientDataEvent
    data object DismissDelete : ClientDataEvent
}

sealed interface ClientDataEffect {
    data object NavigateBack : ClientDataEffect
    data class VerifyPhone(val contactId: Long, val phone: String) : ClientDataEffect
    data class ShowMessage(val message: String) : ClientDataEffect
}
