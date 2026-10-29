package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.location.launchDebouncedForwardGeocode
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
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
    val isSearchingBarrios: Boolean = false,
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val nombre: String = "Domicilio principal",
    val location: AddressLocationSnapshot = AddressLocationSnapshot(),
    val isGeocoding: Boolean = false,
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
    private val geocodeLocalAddressUseCase: GeocodeLocalAddressUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientDomicilioUiState())
    val uiState: StateFlow<ClientDomicilioUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ClientDomicilioEffect>()
    val effects: SharedFlow<ClientDomicilioEffect> = _effects.asSharedFlow()

    private var searchJob: Job? = null
    private var geocodeJob: Job? = null

    fun onBarrioQueryChanged(query: String) = searchBarrio(query, preserveSelection = false)

    fun onBarrioFocused() = searchBarrio(_uiState.value.barrioQuery, preserveSelection = true)

    private fun searchBarrio(query: String, preserveSelection: Boolean) {
        _uiState.update {
            it.copy(
                barrioQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                isSearchingBarrios = true,
            )
        }
        if (!preserveSelection) scheduleLiveGeocode()
        searchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = searchJob,
            query = query,
            searchBarrios = searchBarriosUseCase,
            onResult = { options ->
                _uiState.update { it.copy(isSearchingBarrios = false, barrioOptions = options) }
            },
        )
    }

    fun onBarrioSelected(option: CatalogOption) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                barrioQuery = option.label,
                selectedBarrioId = option.id,
                barrioOptions = emptyList(),
                isSearchingBarrios = false,
            )
        }
        scheduleLiveGeocode()
    }

    fun dismissBarrioSuggestions() {
        searchJob?.cancel()
        _uiState.update { it.copy(barrioOptions = emptyList(), isSearchingBarrios = false) }
    }

    fun onCalleChanged(value: String) {
        _uiState.update { it.copy(calle = value, error = null) }
        scheduleLiveGeocode()
    }
    fun onNumeroChanged(value: String) {
        _uiState.update { it.copy(numero = value, error = null) }
        scheduleLiveGeocode()
    }
    
    fun onMapLocationSelected(latitude: Double, longitude: Double) {
        cancelLiveGeocode()
        _uiState.update {
            it.copy(location = FormosaAddressGeocoding.pinMoved(it.location, latitude, longitude), error = null)
        }
    }
    
    fun locateOnMap() {
        cancelLiveGeocode()
        _uiState.update {
            val next = FormosaAddressGeocoding.openMap(it.location)
            it.copy(location = next, error = next.message)
        }
    }
    
    fun confirmPin() {
        cancelLiveGeocode()
        val confirmed = FormosaAddressGeocoding.confirmPin(_uiState.value.location)
        _uiState.update { it.copy(location = confirmed, error = confirmed.message) }
        if (confirmed.canPersist) submit()
    }
    
    fun onLocationPermissionDenied() = _uiState.update {
        it.copy(error = "Permita la ubicación o marque manualmente el punto en el mapa.")
    }
    
    fun clearMapPin() = _uiState.update {
        it.copy(location = AddressLocationSnapshot(), error = null)
    }
    
    fun useCurrentLocation() {
        cancelLiveGeocode()
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, error = null) }
            when (val result = getCurrentLocationUseCase()) {
                is AppResult.Success -> {
                    val coords = result.data
                    _uiState.update {
                        it.copy(
                            isLocating = false,
                            location = FormosaAddressGeocoding.pinMoved(
                                it.location,
                                coords.latitude,
                                coords.longitude
                            ),
                        )
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLocating = false, error = result.error.userMessage())
                }
            }
        }
    }

    fun submit() {
        cancelLiveGeocode()
        val state = _uiState.value
        val barrioId = state.selectedBarrioId
        
        if (barrioId == null || state.calle.isBlank()) {
            _uiState.update { it.copy(error = "Complete barrio y calle") }
            return
        }
        
        if (!state.location.canPersist) {
            resolveFromAddress()
            return
        }
        
        persist()
    }
    
    private fun scheduleLiveGeocode() {
        val state = _uiState.value
        geocodeJob = viewModelScope.launchDebouncedForwardGeocode(
            previous = geocodeJob,
            query = LocalAddressQuery(
                street = state.calle,
                number = state.numero,
                neighborhood = state.barrioQuery,
            ),
            geocode = geocodeLocalAddressUseCase,
            onResult = { result ->
                val outcome = (result as? AppResult.Success)?.data ?: return@launchDebouncedForwardGeocode
                _uiState.update {
                    it.copy(location = FormosaAddressGeocoding.applyLiveOutcome(outcome, it.location))
                }
            },
        )
    }

    private fun cancelLiveGeocode() {
        geocodeJob?.cancel()
        geocodeJob = null
    }

    private fun resolveFromAddress() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeocoding = true,
                    error = null,
                    location = it.location.copy(phase = AddressLocationPhase.Geocoding),
                )
            }
            when (
                val result = geocodeLocalAddressUseCase(
                    LocalAddressQuery(
                        street = state.calle,
                        number = state.numero,
                        neighborhood = state.barrioQuery,
                    ),
                )
            ) {
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isGeocoding = false,
                        location = AddressLocationSnapshot(
                            phase = AddressLocationPhase.NeedsMap,
                            message = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                        ),
                        error = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                    )
                }
                is AppResult.Success -> {
                    val snapshot = FormosaAddressGeocoding.snapshotFor(result.data)
                    _uiState.update {
                        it.copy(isGeocoding = false, location = snapshot, error = snapshot.message)
                    }
                }
            }
        }
    }
    
    private fun persist() {
        val state = _uiState.value
        val barrioId = state.selectedBarrioId ?: return
        val location = state.location
        if (!location.canPersist) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val personaId = when (val user = getCurrentUserUseCase()) {
                is AppResult.Success -> user.data.personaId
                is AppResult.Error -> null
            }
            if (personaId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo obtener su perfil") }
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
                        latitud = GeoCoordinates.roundForApi(location.latitude),
                        longitud = GeoCoordinates.roundForApi(location.longitude),
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
