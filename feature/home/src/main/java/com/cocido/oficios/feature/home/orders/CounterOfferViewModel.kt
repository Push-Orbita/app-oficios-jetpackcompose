package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.OrderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CounterOfferViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderUseCases: OrderUseCases,
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L

    private val _uiState = MutableStateFlow<CounterOfferUiState>(CounterOfferUiState.Form())
    val uiState: StateFlow<CounterOfferUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CounterOfferEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: CounterOfferEvent) {
        when (event) {
            is CounterOfferEvent.DateTimeSelected -> updateForm { copy(dateTime = event.dateTime) }
            is CounterOfferEvent.PriceChanged -> updateForm { copy(price = event.value) }
            CounterOfferEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val form = uiState.value as? CounterOfferUiState.Form ?: return
        if (form.isSubmitting) return
        _uiState.update { form.copy(hasAttemptedSubmit = true) }
        if (!form.canSubmit) return
        val dateTime = form.dateTime ?: return

        viewModelScope.launch {
            _uiState.update { form.copy(isSubmitting = true) }
            val zoned = dateTime.atZone(ZoneId.systemDefault())
            val startIso = zoned.toInstant().toString()
            val endIso = zoned.plusHours(2).toInstant().toString()
            val price = form.price.toDoubleOrNull()

            when (val result = orderUseCases.sendCounterOffer(orderId, startIso, endIso, price)) {
                is AppResult.Success -> {
                    _effects.send(CounterOfferEffect.CounterOfferSentSuccess)
                    _effects.send(CounterOfferEffect.NavigateBack)
                }
                is AppResult.Error -> {
                    _uiState.update { form.copy(isSubmitting = false) }
                    _effects.send(CounterOfferEffect.ShowMessage(result.error.userMessage()))
                }
            }
        }
    }

    private inline fun updateForm(transform: CounterOfferUiState.Form.() -> CounterOfferUiState.Form) {
        _uiState.update { current -> if (current is CounterOfferUiState.Form) current.transform() else current }
    }
}
