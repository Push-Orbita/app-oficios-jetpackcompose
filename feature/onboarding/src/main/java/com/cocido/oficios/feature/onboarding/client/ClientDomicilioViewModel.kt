package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientDomicilioUiState(
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val nombre: String = "Domicilio principal",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val isLocating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = selectedBarrioId != null && calle.isNotBlank()
}

sealed interface ClientDomicilioEffect {
    data object Saved : ClientDomicilioEffect
}

@HiltViewModel
class ClientDomicilioViewModel @Inject constructor(
    private val saveClientDomicilioUseCase: SaveClientDomicilioUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientDomicilioUiState())
    val uiState: StateFlow<ClientDomicilioUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ClientDomicilioEffect>()
    val effects: SharedFlow<ClientDomicilioEffect> = _effects.asSharedFlow()

    private var searchJob: Job? = null

    fun onBarrioQueryChanged(query: String) {
        _uiState.update { it.copy(barrioQuery = query, selectedBarrioId = null) }
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(barrioOptions = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            when (val result = searchBarriosUseCase(query)) {
                is AppResult.Success -> _uiState.update { it.copy(barrioOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun onBarrioSelected(option: CatalogOption) {
        _uiState.update {
            it.copy(barrioQuery = option.label, selectedBarrioId = option.id, barrioOptions = emptyList())
        }
    }

    fun dismissBarrioSuggestions() {
        _uiState.update { it.copy(barrioOptions = emptyList()) }
    }

    fun onCalleChanged(value: String) = _uiState.update { it.copy(calle = value, error = null) }
    fun onNumeroChanged(value: String) = _uiState.update { it.copy(numero = value, error = null) }
    fun onMapLocationSelected(latitude: Double, longitude: Double) = _uiState.update {
        it.copy(latitud = latitude, longitud = longitude, error = null)
    }
    fun onLocationPermissionDenied() = _uiState.update {
        it.copy(error = "Permití la ubicación o marcá manualmente el punto en el mapa.")
    }
    fun useCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, error = null) }
            when (val result = getCurrentLocationUseCase()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLocating = false,
                        latitud = result.data.latitude,
                        longitud = result.data.longitude,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLocating = false, error = result.error.userMessage())
                }
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val barrioId = state.selectedBarrioId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val personaId = when (val user = getCurrentUserUseCase()) {
                is AppResult.Success -> user.data.personaId
                is AppResult.Error -> null
            }
            if (personaId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo obtener tu perfil") }
                return@launch
            }
            when (
                val result = saveClientDomicilioUseCase(
                    ClientDomicilioRequest(
                        barrioId = barrioId,
                        personaId = personaId,
                        nombre = state.nombre,
                        calle = state.calle.trim(),
                        numero = state.numero.trim().ifBlank { null },
                        latitud = state.latitud,
                        longitud = state.longitud,
                        favorito = true,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ClientDomicilioEffect.Saved)
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
                }
            }
        }
    }
}
