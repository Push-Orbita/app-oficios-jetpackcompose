package com.cocido.oficios.feature.home.orders

import java.time.LocalDateTime

sealed interface CounterOfferUiState {
    data object Idle : CounterOfferUiState
    data class Form(
        val dateTime: LocalDateTime? = null,
        val price: String = "",
        val isSubmitting: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
    ) : CounterOfferUiState {
        val priceError: String?
            get() = if (price.isNotBlank() && price.toDoubleOrNull() == null) {
                "Ingrese un monto válido."
            } else {
                null
            }
        val canSubmit: Boolean get() = dateTime != null && priceError == null
    }
}

sealed interface CounterOfferEvent {
    data class DateTimeSelected(val dateTime: LocalDateTime) : CounterOfferEvent
    data class PriceChanged(val value: String) : CounterOfferEvent
    data object Submit : CounterOfferEvent
}

sealed interface CounterOfferEffect {
    data object NavigateBack : CounterOfferEffect
    data class ShowMessage(val message: String) : CounterOfferEffect
    data object CounterOfferSentSuccess : CounterOfferEffect
}
