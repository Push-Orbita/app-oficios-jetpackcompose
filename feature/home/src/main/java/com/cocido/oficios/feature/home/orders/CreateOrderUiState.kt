package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.WorkerSubRubroOption
import java.time.LocalDateTime

sealed interface CreateOrderUiState {
    data object Loading : CreateOrderUiState
    data class Error(val message: String) : CreateOrderUiState
    data class Success(
        val workerName: String,
        val subRubroOptions: List<WorkerSubRubroOption>,
        val domicilios: List<ClientDomicilio>,
        val contactos: List<ClientContacto>,
        val selectedSubRubroId: Long? = null,
        val selectedDomicilioId: Long? = null,
        val selectedContactoId: Long? = null,
        val availableSlots: List<AgendaSlot> = emptyList(),
        val isLoadingSlots: Boolean = false,
        val slotsErrorMessage: String? = null,
        val selectedSlotId: Long? = null,
        val proposedDateTime: LocalDateTime? = null,
        val description: String = "",
        val submitIdempotencyKey: String? = null,
        val isSubmitting: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
    ) : CreateOrderUiState
}

sealed interface CreateOrderEvent {
    data object Retry : CreateOrderEvent
    data class SubRubroSelected(val id: Long) : CreateOrderEvent
    data class DomicilioSelected(val id: Long) : CreateOrderEvent
    data class ContactoSelected(val id: Long) : CreateOrderEvent
    data class SlotSelected(val id: Long) : CreateOrderEvent
    data class DateTimeSelected(val dateTime: LocalDateTime) : CreateOrderEvent
    data class DescriptionChanged(val value: String) : CreateOrderEvent
    data object Submit : CreateOrderEvent
}

sealed interface CreateOrderEffect {
    data class NavigateToOrderDetail(val orderId: Long) : CreateOrderEffect
    data class ShowMessage(val message: String) : CreateOrderEffect
}
