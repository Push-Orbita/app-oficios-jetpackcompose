package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.WorkerSubRubroOption
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

enum class CreateOrderStep {
    FORM,
    SUMMARY,
}

sealed interface CreateOrderUiState {
    data object Loading : CreateOrderUiState
    data class Error(val message: String) : CreateOrderUiState
    data class Success(
        val workerName: String,
        val workerAvatarUrl: String? = null,
        val workerVerified: Boolean = false,
        val workerRating: Float? = null,
        val workerReviewCount: Int? = null,
        val specialtyLabel: String = "",
        val subRubroOptions: List<WorkerSubRubroOption>,
        val domicilios: List<ClientDomicilio>,
        val contactos: List<ClientContacto>,
        val workDayKeys: Set<String> = emptySet(),
        val hasAgenda: Boolean = false,
        val selectedSubRubroId: Long? = null,
        val selectedDomicilioId: Long? = null,
        val selectedContactoId: Long? = null,
        val availableSlots: List<AgendaSlot> = emptyList(),
        val monthSlots: List<AgendaSlot> = emptyList(),
        val dayRanges: List<TimeRange> = emptyList(),
        val dayHasAvailability: Boolean? = null,
        val isLoadingSlots: Boolean = false,
        val slotsErrorMessage: String? = null,
        val submitErrorMessage: String? = null,
        val selectedSlotId: Long? = null,
        val selectedDate: LocalDate? = null,
        val selectedHour: LocalTime? = null,
        val visibleMonth: YearMonth = YearMonth.now(),
        val proposedDateTime: LocalDateTime? = null,
        val description: String = "",
        val step: CreateOrderStep = CreateOrderStep.FORM,
        val submitIdempotencyKey: String? = null,
        val isSubmitting: Boolean = false,
        val hasAttemptedContinue: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
    ) : CreateOrderUiState
}

sealed interface CreateOrderEvent {
    data object Retry : CreateOrderEvent
    data class SubRubroSelected(val id: Long) : CreateOrderEvent
    data class DomicilioSelected(val id: Long) : CreateOrderEvent
    data class ContactoSelected(val id: Long) : CreateOrderEvent
    data class DateSelected(val date: LocalDate) : CreateOrderEvent
    data class HourSelected(val hour: LocalTime) : CreateOrderEvent
    data class MonthChanged(val month: YearMonth) : CreateOrderEvent
    data class DescriptionChanged(val value: String) : CreateOrderEvent
    data object ContinueToSummary : CreateOrderEvent
    data object EditRequest : CreateOrderEvent
    data object Submit : CreateOrderEvent
    data object AddLocation : CreateOrderEvent
    data class LocationPicked(val id: Long) : CreateOrderEvent
}

sealed interface CreateOrderEffect {
    data object RequestSent : CreateOrderEffect
    data class ShowMessage(val message: String) : CreateOrderEffect
    data object NavigateToAddLocation : CreateOrderEffect
}
