package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfessionalProfileViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfessionalProfileUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<ProfessionalProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { load() }

    fun onEvent(event: ProfessionalProfileEvent) {
        when (event) {
            ProfessionalProfileEvent.Retry -> load()
            is ProfessionalProfileEvent.DescriptionChanged -> _uiState.update {
                it.copy(description = event.value, errorMessage = null)
            }
            is ProfessionalProfileEvent.ExperienceChanged -> _uiState.update {
                it.copy(experience = event.value, errorMessage = null)
            }
            is ProfessionalProfileEvent.PaymentToggled -> _uiState.update {
                val next = it.paymentMethods.toMutableSet().apply {
                    if (event.checked) add(event.method) else remove(event.method)
                }
                it.copy(paymentMethods = next)
            }
            ProfessionalProfileEvent.Save -> save()
            ProfessionalProfileEvent.Back -> emit(ProfessionalProfileEffect.NavigateBack)
            ProfessionalProfileEvent.EditRubro -> emit(ProfessionalProfileEffect.NavigateToServices)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> {
                    val provider = result.data.providerDetails
                    if (provider == null) {
                        _uiState.update { it.copy(isLoading = false, loadFailed = true, errorMessage = "Esta sección es solo para trabajadores de oficio.") }
                    } else {
                        _uiState.update {
                            ProfessionalProfileUiState(
                                isLoading = false,
                                description = provider.description.orEmpty(),
                                experience = provider.experience.orEmpty(),
                                paymentMethods = provider.paymentMethods.toSet(),
                            )
                        }
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, loadFailed = true, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.description.isBlank() || state.experience.isBlank() || state.paymentMethods.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Complete su presentación, experiencia y al menos un medio de cobro.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = ProviderProfessionalInfoRequest(
                descripcion = state.description.trim(),
                experiencia = state.experience.trim(),
                paymentMethods = state.paymentMethods.toList(),
            )
            when (val result = accountUseCases.updateProfessionalInfo(request)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    emit(ProfessionalProfileEffect.Saved("Tu presentación profesional se actualizó."))
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun emit(effect: ProfessionalProfileEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
