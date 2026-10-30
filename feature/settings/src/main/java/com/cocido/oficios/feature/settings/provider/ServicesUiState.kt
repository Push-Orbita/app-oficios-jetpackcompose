package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.WeeklyAvailability

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
    data class EditAvailability(val service: ManagedProviderService) : ServicesEvent
    data class RequestDelete(val service: ManagedProviderService) : ServicesEvent
    data object ConfirmDelete : ServicesEvent
    data object DismissDelete : ServicesEvent
}

sealed interface ServicesEffect {
    data object NavigateBack : ServicesEffect
    data object NavigateToAdd : ServicesEffect
    data class NavigateToEdit(val serviceId: Long) : ServicesEffect
    data object NavigateToAvailability : ServicesEffect
    data class ShowMessage(val message: String) : ServicesEffect
}

/** Flujo compacto: oficio → dónde/cuándo → cobro y confirmar. */
enum class AddServiceStep { TRADE, ATTENTION, CONFIRM }

data class AddServiceUiState(
    val step: AddServiceStep = AddServiceStep.TRADE,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
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
    val plazoCancelacionHorasInput: String = "",
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
    val weeklyAvailability: WeeklyAvailability = WeeklyAvailability.default(),
    val paymentMethods: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val isEditing: Boolean = false,
    val editingServiceId: Long? = null,
    val isPrimaryService: Boolean = false,
    val lockedRubroName: String = "",
    val editBaseline: AddServiceFormSnapshot? = null,
    val editorSpecialties: List<CatalogOption> = emptyList(),
    val editorSourceSubId: Long? = null,
    val availabilityDrafts: Map<Long, WeeklyAvailability> = emptyMap(),
    val canDisableTurns: Boolean = true,
) {
    val selectedCategoryLabel get() = categories.firstOrNull { it.id == selectedCategoryId }?.label.orEmpty()
    val selectedRubroLabel get() = rubros.firstOrNull { it.id == selectedRubroId }?.label.orEmpty()
        .ifBlank { lockedRubroName }
    val localLatitude: Double? get() = localLocation.latitude
    val localLongitude: Double? get() = localLocation.longitude

    val plazoCancelacionHorasError: String?
        get() {
            val trimmed = plazoCancelacionHorasInput.trim()
            if (trimmed.isEmpty()) return null
            val parsed = trimmed.toIntOrNull()
            return if (parsed == null || parsed !in 1..24) {
                "Ingrese un valor entre 1 y 24, o deje vacío"
            } else null
        }

    val plazoCancelacionHoras: Int?
        get() {
            val trimmed = plazoCancelacionHorasInput.trim()
            return trimmed.toIntOrNull()?.takeIf { it in 1..24 }
        }

    /** Edición no carga categorías: el form ya tiene rubro/especialidades. */
    val hasFormContent: Boolean
        get() = if (isEditing) {
            selectedRubroId != null || subRubros.isNotEmpty()
        } else {
            categories.isNotEmpty()
        }
    val hasUnsavedChanges: Boolean
        get() = if (isEditing) {
            editBaseline != null && formSnapshot() != editBaseline
        } else {
            selectedCategoryId != null ||
                localName.isNotBlank() ||
                localBarrioQuery.isNotBlank() ||
                localStreet.isNotBlank() ||
                localNumber.isNotBlank() ||
                paymentMethods.isNotEmpty()
        }

    val editorSpecialtyName: String
        get() = editorSpecialties.firstOrNull { it.id == editorSourceSubId }?.label.orEmpty()

    fun availabilitySnapshot(): Map<Long, WeeklyAvailability> {
        val drafts = availabilityDrafts.toMutableMap()
        editorSourceSubId?.let { drafts[it] = weeklyAvailability }
        return drafts
    }

    fun formSnapshot() = AddServiceFormSnapshot(
        selectedSubRubroIds = selectedSubRubroIds,
        servesAtClientHome = servesAtClientHome,
        servesAtLocal = servesAtLocal,
        requiresTurns = requiresTurns,
        plazoCancelacionHoras = plazoCancelacionHoras,
        localName = localName,
        localBarrioId = localBarrioId,
        localStreet = localStreet,
        localNumber = localNumber,
        weeklyAvailability = if (isEditing) WeeklyAvailability.default() else weeklyAvailability,
        paymentMethods = paymentMethods,
        availabilityBySubId = availabilitySnapshot(),
    )
}

data class AddServiceFormSnapshot(
    val selectedSubRubroIds: Set<Long>,
    val servesAtClientHome: Boolean,
    val servesAtLocal: Boolean,
    val requiresTurns: Boolean,
    val plazoCancelacionHoras: Int?,
    val localName: String,
    val localBarrioId: Long?,
    val localStreet: String,
    val localNumber: String,
    val weeklyAvailability: WeeklyAvailability,
    val paymentMethods: Set<String>,
    val availabilityBySubId: Map<Long, WeeklyAvailability> = emptyMap(),
)

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
    data class PlazoCancelacionHorasChanged(val value: String) : AddServiceEvent
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
    data class IncludeSunday(val included: Boolean) : AddServiceEvent
    data class WeekDayToggled(val day: String, val enabled: Boolean) : AddServiceEvent
    data class WeekRangeFrom(val day: String, val index: Int, val value: String) : AddServiceEvent
    data class WeekRangeTo(val day: String, val index: Int, val value: String) : AddServiceEvent
    data class AddWeekRange(val day: String) : AddServiceEvent
    data class RemoveWeekRange(val day: String, val index: Int) : AddServiceEvent
    data class PaymentToggled(val method: String, val checked: Boolean) : AddServiceEvent
    data class EditorSpecialtySelected(val id: Long) : AddServiceEvent
    data object Save : AddServiceEvent
}

sealed interface AddServiceEffect {
    data object NavigateBack : AddServiceEffect
    data class Saved(val message: String) : AddServiceEffect
}
