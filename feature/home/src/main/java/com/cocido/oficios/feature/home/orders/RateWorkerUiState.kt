package com.cocido.oficios.feature.home.orders

sealed interface RateWorkerUiState {
    data class Form(
        val workerName: String,
        val qualityStars: Int = 0,
        val punctualityStars: Int = 0,
        val comment: String = "",
        val isSubmitting: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
    ) : RateWorkerUiState
    data class Submitted(val workerName: String) : RateWorkerUiState
}

sealed interface RateWorkerEvent {
    data class QualitySelected(val stars: Int) : RateWorkerEvent
    data class PunctualitySelected(val stars: Int) : RateWorkerEvent
    data class CommentChanged(val value: String) : RateWorkerEvent
    data object Submit : RateWorkerEvent
}

sealed interface RateWorkerEffect {
    data class ShowMessage(val message: String) : RateWorkerEffect
}
