package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import java.time.LocalDateTime

sealed interface AgendaListUiState {
    data object Loading : AgendaListUiState

    data class Error(val message: String) : AgendaListUiState

    data class Success(
        val subRubroOptions: List<ProviderSubRubroOption> = emptyList(),
        val selectedSubRubroId: Long? = null,
        val slots: List<AgendaSlot> = emptyList(),
        val isLoadingSlots: Boolean = false,
        val showCreateSlotForm: Boolean = false,
        val newSlotStart: LocalDateTime? = null,
        val newSlotEnd: LocalDateTime? = null,
        val newSlotAutoAcceptance: Boolean = false,
        val isSavingSlot: Boolean = false,
        val slotPendingDeleteId: Long? = null,
        val error: String? = null,
    ) : AgendaListUiState {
        val selectedSubRubro: ProviderSubRubroOption?
            get() = subRubroOptions.firstOrNull { it.id == selectedSubRubroId }

        val canSubmitNewSlot: Boolean
            get() = newSlotStart != null && newSlotEnd != null && newSlotEnd.isAfter(newSlotStart)
    }
}

sealed interface AgendaListEvent {
    data object Retry : AgendaListEvent
    data class SubRubroSelected(val subRubroId: Long) : AgendaListEvent
    data object OpenCreateSlot : AgendaListEvent
    data object DismissCreateSlot : AgendaListEvent
    data class NewSlotStartSelected(val dateTime: LocalDateTime) : AgendaListEvent
    data class NewSlotEndSelected(val dateTime: LocalDateTime) : AgendaListEvent
    data class NewSlotAutoAcceptanceChanged(val value: Boolean) : AgendaListEvent
    data object SubmitNewSlot : AgendaListEvent
    data class RequestDeleteSlot(val slotId: Long) : AgendaListEvent
    data object ConfirmDeleteSlot : AgendaListEvent
    data object DismissDeleteSlot : AgendaListEvent
    data object OpenConfig : AgendaListEvent
}

sealed interface AgendaListEffect {
    data class ShowMessage(val message: String) : AgendaListEffect
    data object NavigateToConfig : AgendaListEffect
}
