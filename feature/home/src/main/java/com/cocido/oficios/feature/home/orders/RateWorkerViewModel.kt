package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.OrderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RateWorkerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderUseCases: OrderUseCases,
) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L
    private val workerName: String = Uri.decode(savedStateHandle.get<String>("workerName").orEmpty())

    private val _uiState = MutableStateFlow<RateWorkerUiState>(RateWorkerUiState.Form(workerName = workerName))
    val uiState: StateFlow<RateWorkerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RateWorkerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: RateWorkerEvent) {
        when (event) {
            is RateWorkerEvent.QualitySelected -> updateForm { copy(qualityStars = event.stars, submitError = null) }
            is RateWorkerEvent.PunctualitySelected -> updateForm { copy(punctualityStars = event.stars, submitError = null) }
            is RateWorkerEvent.CommentChanged -> updateForm { copy(comment = event.value, submitError = null) }
            RateWorkerEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val form = uiState.value as? RateWorkerUiState.Form ?: return
        if (form.isSubmitting) return
        _uiState.update { form.copy(hasAttemptedSubmit = true) }
        if (form.qualityStars == 0 || form.punctualityStars == 0) return

        viewModelScope.launch {
            _uiState.update { form.copy(isSubmitting = true, hasAttemptedSubmit = true, submitError = null) }
            // Backend: calidad y puntualidad en escala 1–5, igual que el selector.
            val result = orderUseCases.submitRating(
                bookingId = bookingId,
                quality = form.qualityStars,
                punctuality = form.punctualityStars,
                comment = form.comment.ifBlank { null },
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { RateWorkerUiState.Submitted(workerName = form.workerName) }
                    _effects.send(RateWorkerEffect.RatingSubmittedSuccess)
                }
                is AppResult.Error -> {
                    val message = result.error.toRatingMessage()
                    _uiState.update { form.copy(isSubmitting = false, hasAttemptedSubmit = true, submitError = message) }
                    _effects.send(RateWorkerEffect.ShowMessage(message))
                }
            }
        }
    }

    private fun AppError.toRatingMessage(): String = when (this) {
        is AppError.Validation -> RATING_VALIDATION_MESSAGE
        is AppError.Conflict -> RATING_ALREADY_SENT_MESSAGE
        else -> userMessage()
    }

    private inline fun updateForm(transform: RateWorkerUiState.Form.() -> RateWorkerUiState.Form) {
        _uiState.update { current -> if (current is RateWorkerUiState.Form) current.transform() else current }
    }

    internal companion object {
        const val RATING_VALIDATION_MESSAGE =
            "No pudimos enviar la calificación. Revise las estrellas e intente nuevamente."
        const val RATING_ALREADY_SENT_MESSAGE = "Este servicio ya fue calificado."
    }
}
