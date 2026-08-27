package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

enum class AgendaViewMode { MONTH, WEEK }

enum class AgendaSlotFormIssue {
    MISSING_DATE,
    END_BEFORE_START,
    DIFFERENT_DAY,
    PAST_DATE,
    OVERLAP,
}

sealed interface AgendaListUiState {
    data object Loading : AgendaListUiState

    data class Error(val message: String) : AgendaListUiState

    data class Success(
        val subRubroOptions: List<ProviderSubRubroOption> = emptyList(),
        val selectedSubRubroId: Long? = null,
        val slots: List<AgendaSlot> = emptyList(),
        val visibleMonth: YearMonth = YearMonth.now(),
        val selectedDate: LocalDate = LocalDate.now(),
        val viewMode: AgendaViewMode = AgendaViewMode.MONTH,
        val isLoadingSlots: Boolean = false,
        val showSlotForm: Boolean = false,
        val editingSlotId: Long? = null,
        val slotStart: LocalDateTime? = null,
        val slotEnd: LocalDateTime? = null,
        val slotAutoAcceptance: Boolean = false,
        val isSavingSlot: Boolean = false,
        val slotPendingDeleteId: Long? = null,
        val slotsError: String? = null,
        val formIssue: AgendaSlotFormIssue? = null,
        val formBackendError: String? = null,
    ) : AgendaListUiState {
        val selectedSubRubro: ProviderSubRubroOption?
            get() = subRubroOptions.firstOrNull { it.id == selectedSubRubroId }

        val selectedDateSlots: List<AgendaSlot>
            get() = slots.filter { it.start.toLocalDate() == selectedDate }.sortedBy { it.start }

        val datesWithSlots: Set<LocalDate>
            get() = slots.mapTo(mutableSetOf()) { it.start.toLocalDate() }

        val editingSlot: AgendaSlot?
            get() = editingSlotId?.let { id -> slots.firstOrNull { it.id == id } }

        val derivedFormIssue: AgendaSlotFormIssue?
            get() {
                val start = slotStart ?: return AgendaSlotFormIssue.MISSING_DATE
                val end = slotEnd ?: return AgendaSlotFormIssue.MISSING_DATE
                if (!end.isAfter(start)) return AgendaSlotFormIssue.END_BEFORE_START
                if (start.toLocalDate() != end.toLocalDate()) return AgendaSlotFormIssue.DIFFERENT_DAY
                if (slots.any { existing ->
                        existing.id != editingSlotId &&
                            start.isBefore(existing.end) && end.isAfter(existing.start)
                    }
                ) {
                    return AgendaSlotFormIssue.OVERLAP
                }
                return null
            }

        val canSubmitSlot: Boolean
            get() = derivedFormIssue == null && formIssue != AgendaSlotFormIssue.PAST_DATE
    }
}

sealed interface AgendaListEvent {
    data object Retry : AgendaListEvent
    data object RetrySlots : AgendaListEvent
    data class SubRubroSelected(val subRubroId: Long) : AgendaListEvent
    data class ViewModeChanged(val mode: AgendaViewMode) : AgendaListEvent
    data object PreviousPeriod : AgendaListEvent
    data object NextPeriod : AgendaListEvent
    data object GoToToday : AgendaListEvent
    data class DateSelected(val date: LocalDate) : AgendaListEvent
    data object OpenCreateSlot : AgendaListEvent
    data class OpenEditSlot(val slotId: Long) : AgendaListEvent
    data object DismissSlotForm : AgendaListEvent
    data class SlotStartSelected(val dateTime: LocalDateTime) : AgendaListEvent
    data class SlotEndSelected(val dateTime: LocalDateTime) : AgendaListEvent
    data class SlotAutoAcceptanceChanged(val value: Boolean) : AgendaListEvent
    data object SubmitSlot : AgendaListEvent
    data class RequestDeleteSlot(val slotId: Long) : AgendaListEvent
    data object ConfirmDeleteSlot : AgendaListEvent
    data object DismissDeleteSlot : AgendaListEvent
    data object OpenConfig : AgendaListEvent
}

sealed interface AgendaListEffect {
    data class ShowMessage(val message: String) : AgendaListEffect
    data object NavigateToConfig : AgendaListEffect
}
