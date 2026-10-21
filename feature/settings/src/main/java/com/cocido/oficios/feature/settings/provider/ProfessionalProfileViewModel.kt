package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
            ProfessionalProfileEvent.Back -> emit(ProfessionalProfileEffect.NavigateBack)
            ProfessionalProfileEvent.EditRubro -> emit(ProfessionalProfileEffect.NavigateToServices)
            ProfessionalProfileEvent.AddWork,
            ProfessionalProfileEvent.AddCertificate,
            -> emit(ProfessionalProfileEffect.NavigateToPortfolio)
            ProfessionalProfileEvent.DeleteAccount -> emit(ProfessionalProfileEffect.NavigateToDeleteAccount)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, loadFailed = false)
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> {
                    val provider = result.data.providerDetails
                    if (provider == null) {
                        _uiState.value = ProfessionalProfileUiState(
                            isLoading = false,
                            loadFailed = true,
                            errorMessage = "Esta sección es solo para trabajadores de oficio.",
                        )
                    } else {
                        val oficios = provider.services.map { it.rubroName }.filter { it.isNotBlank() }.distinct()
                        val specialized = provider.services
                            .flatMap { service -> service.subServices.map { it.name } }
                            .filter { it.isNotBlank() }
                            .distinct()
                        _uiState.value = ProfessionalProfileUiState(
                            isLoading = false,
                            oficiosLabel = oficios.joinToString(", "),
                            specializedLabel = specialized.joinToString(", "),
                            gallery = provider.gallery,
                            certificates = provider.certificates.map { it.toUi() },
                        )
                    }
                }
                is AppResult.Error -> _uiState.value = ProfessionalProfileUiState(
                    isLoading = false,
                    loadFailed = true,
                    errorMessage = result.error.userMessage(),
                )
            }
        }
    }

    private fun emit(effect: ProfessionalProfileEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
