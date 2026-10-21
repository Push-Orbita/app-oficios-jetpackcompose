package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft

data class ServicesUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val services: List<ManagedProviderService> = emptyList(),
    val errorMessage: String? = null,
    val pendingDelete: ManagedProviderService? = null,
)

sealed interface ServicesEvent {
    data object Retry : ServicesEvent
    data object Back : ServicesEvent
    data object AddService : ServicesEvent
    data class EditService(val service: ManagedProviderService) : ServicesEvent
    data class RequestDelete(val service: ManagedProviderService) : ServicesEvent
    data object ConfirmDelete : ServicesEvent
    data object DismissDelete : ServicesEvent
}

sealed interface ServicesEffect {
    data object NavigateBack : ServicesEffect
    data object NavigateToAdd : ServicesEffect
    data class NavigateToEdit(val serviceId: Long) : ServicesEffect
    data class ShowMessage(val message: String) : ServicesEffect
}

enum class AddServiceStep { TRADE, MODALITY, SCHEDULE, PAYMENT, REVIEW }

data class AddServiceUiState(
    val step: AddServiceStep = AddServiceStep.TRADE,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val categories: List<CatalogOption> = emptyList(),
    val rubros: List<CatalogOption> = emptyList(),
    val subRubros: List<CatalogOption> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedRubroId: Long? = null,
    val selectedSubRubroIds: Set<Long> = emptySet(),
    val servesAtClientHome: Boolean = true,
    val servesAtLocal: Boolean = false,
    val requiresTurns: Boolean = false,
    val localName: String = "",
    val localBarrioQuery: String = "",
    val localBarrioId: Long? = null,
    val localBarrioSuggestions: List<CatalogOption> = emptyList(),
    val isSearchingBarrios: Boolean = false,
    val localStreet: String = "",
    val localNumber: String = "",
    val localLocation: AddressLocationSnapshot = AddressLocationSnapshot(),
    val isGeocoding: Boolean = false,
    val workDays: Set<String> = setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"),
    val timeSlots: List<RegisterTradeTimeSlotDraft> = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
    val paymentMethods: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val isEditing: Boolean = false,
    val editingServiceId: Long? = null,
    val isPrimaryService: Boolean = false,
    val lockedRubroName: String = "",
) {
    val selectedCategoryLabel get() = categories.firstOrNull { it.id == selectedCategoryId }?.label.orEmpty()
    val selectedRubroLabel get() = rubros.firstOrNull { it.id == selectedRubroId }?.label.orEmpty()
        .ifBlank { lockedRubroName }
    val localLatitude: Double? get() = localLocation.latitude
    val localLongitude: Double? get() = localLocation.longitude
}

sealed interface AddServiceEvent {
    data object Retry : AddServiceEvent
    data object Back : AddServiceEvent
    data object Next : AddServiceEvent
    data object Previous : AddServiceEvent
    data class CategorySelected(val id: Long) : AddServiceEvent
    data class RubroSelected(val id: Long) : AddServiceEvent
    data class SubRubroToggled(val id: Long, val checked: Boolean) : AddServiceEvent
    data class ClientHomeChanged(val checked: Boolean) : AddServiceEvent
    data class LocalChanged(val checked: Boolean) : AddServiceEvent
    data class RequiresTurnsChanged(val checked: Boolean) : AddServiceEvent
    data class LocalNameChanged(val value: String) : AddServiceEvent
    data class LocalBarrioChanged(val value: String) : AddServiceEvent
    data object LocalBarrioFocused : AddServiceEvent
    data class LocalBarrioSelected(val option: CatalogOption) : AddServiceEvent
    data object LocalBarrioSuggestionsDismissed : AddServiceEvent
    data class LocalStreetChanged(val value: String) : AddServiceEvent
    data class LocalNumberChanged(val value: String) : AddServiceEvent
    data class LocalMapLocationSelected(val latitude: Double, val longitude: Double) : AddServiceEvent
    data object LocateOnMap : AddServiceEvent
    data object ConfirmPin : AddServiceEvent
    data class WorkDayToggled(val day: String, val checked: Boolean) : AddServiceEvent
    data class TimeFromChanged(val index: Int, val value: String) : AddServiceEvent
    data class TimeToChanged(val index: Int, val value: String) : AddServiceEvent
    data object AddTimeSlot : AddServiceEvent
    data class RemoveTimeSlot(val index: Int) : AddServiceEvent
    data class PaymentToggled(val method: String, val checked: Boolean) : AddServiceEvent
    data object Save : AddServiceEvent
}

sealed interface AddServiceEffect {
    data object NavigateBack : AddServiceEffect
    data class Saved(val message: String) : AddServiceEffect
}
