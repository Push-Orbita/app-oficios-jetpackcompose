package com.cocido.oficios.feature.home.orders

import java.time.LocalDateTime

sealed interface CounterOfferUiState {
    data object Idle : CounterOfferUiState
    data class Form(
        val dateTime: LocalDateTime? = null,
        val price: String = "",
        val isSubmitting: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
    ) : CounterOfferUiState
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
