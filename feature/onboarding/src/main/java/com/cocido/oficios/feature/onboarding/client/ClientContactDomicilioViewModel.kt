package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.profile.GetClientContactosUseCase
import com.cocido.oficios.domain.usecase.profile.GetClientDomiciliosUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOption
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOptions
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.validation.AuthValidators
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

data class ClientContactDomicilioUiState(
    val nombre: String = "",
    val telefono: String = "",
    val selectedCountry: PhoneCountryOption = PhoneCountryOptions.ARGENTINA,
    val email: String = "",
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val isSearchingBarrios: Boolean = false,
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
    val location: AddressLocationSnapshot = AddressLocationSnapshot(),
    val isGeocoding: Boolean = false,
    val isLocating: Boolean = false,
    val isLoading: Boolean = false,
    val isPreloading: Boolean = true,
    val error: String? = null,
) {
    val canSubmit: Boolean get() =
        nombre.isNotBlank() &&
            AuthValidators.validateArgentineMobile(telefono) == null &&
            selectedBarrioId != null &&
            calle.isNotBlank()
}

sealed interface ClientContactDomicilioEffect {
    data object Saved : ClientContactDomicilioEffect
    data class NavigateToPhoneVerification(
        val contactId: Long,
        val telefono: String,
    ) : ClientContactDomicilioEffect
}

@HiltViewModel
class ClientContactDomicilioViewModel @Inject constructor(
    private val saveClientContactoUseCase: SaveClientContactoUseCase,
    private val saveClientDomicilioUseCase: SaveClientDomicilioUseCase,
    private val getClientContactosUseCase: GetClientContactosUseCase,
    private val getClientDomiciliosUseCase: GetClientDomiciliosUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val geocodeLocalAddressUseCase: GeocodeLocalAddressUseCase,
    private val pendingClientOnboardingStore: PendingClientOnboardingStore,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientContactDomicilioUiState())
    val uiState: StateFlow<ClientContactDomicilioUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ClientContactDomicilioEffect>()
    val effects: SharedFlow<ClientContactDomicilioEffect> = _effects.asSharedFlow()

    private var searchJob: Job? = null

    init {
        preloadExistingData()
    }

    private fun preloadExistingData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreloading = true) }
            val contacto = when (val result = getClientContactosUseCase()) {
                is AppResult.Success -> result.data.firstOrNull { it.favorito } ?: result.data.firstOrNull()
                is AppResult.Error -> null
            }
            val domicilio = when (val result = getClientDomiciliosUseCase()) {
                is AppResult.Success -> result.data.firstOrNull { it.favorito } ?: result.data.firstOrNull()
                is AppResult.Error -> null
            }
            val account = (getCurrentUserUseCase() as? AppResult.Success)?.data
            _uiState.update {
                it.copy(
                    nombre = contacto?.nombre?.takeIf { name -> name.isNotBlank() } ?: account?.displayName().orEmpty(),
                    telefono = contacto?.telefonoWhatsapp?.let(AuthValidators::normalizeArgentinePhone).orEmpty(),
                    email = contacto?.email?.takeIf { mail -> mail.isNotBlank() } ?: account?.email.orEmpty(),
                    barrioQuery = domicilio?.barrioName.orEmpty(),
                    selectedBarrioId = domicilio?.barrioId?.takeIf { id -> id > 0L },
                    calle = domicilio?.calle.orEmpty(),
                    numero = domicilio?.numero.orEmpty(),
                    piso = domicilio?.piso.orEmpty(),
                    departamento = domicilio?.departamento.orEmpty(),
                    location = if (domicilio?.latitud != null && domicilio.longitud != null) {
                        AddressLocationSnapshot(
                            phase = AddressLocationPhase.Confirmed,
                            latitude = domicilio.latitud,
                            longitude = domicilio.longitud,
                            confirmed = true,
                        )
                    } else AddressLocationSnapshot(),
                    isPreloading = false,
                )
            }
        }
    }

    fun onNombreChanged(value: String) = _uiState.update { it.copy(nombre = value, error = null) }
    fun onTelefonoChanged(value: String) = _uiState.update { it.copy(telefono = value, error = null) }
    fun onCountrySelected(value: PhoneCountryOption) = _uiState.update { it.copy(selectedCountry = value) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onCalleChanged(value: String) = _uiState.update { 
        it.copy(calle = value, error = null, location = FormosaAddressGeocoding.invalidate())
    }
    fun onNumeroChanged(value: String) = _uiState.update { 
        it.copy(numero = value, error = null, location = FormosaAddressGeocoding.invalidate())
    }
    fun onPisoChanged(value: String) = _uiState.update { it.copy(piso = value, error = null) }
    fun onDepartamentoChanged(value: String) = _uiState.update { it.copy(departamento = value, error = null) }
    
    fun onMapLocationSelected(latitude: Double, longitude: Double) = _uiState.update {
        it.copy(location = FormosaAddressGeocoding.pinMoved(it.location, latitude, longitude), error = null)
    }
    
    fun locateOnMap() = _uiState.update {
        val next = FormosaAddressGeocoding.openMap(it.location)
        it.copy(location = next, error = next.message)
    }
    
    fun confirmPin() {
        val confirmed = FormosaAddressGeocoding.confirmPin(_uiState.value.location)
        _uiState.update { it.copy(location = confirmed, error = confirmed.message) }
        if (confirmed.canPersist) submit()
    }

    fun onLocationPermissionDenied() = _uiState.update {
        it.copy(error = "Permita la ubicación o marque manualmente el punto de entrada en el mapa.")
    }

    fun clearMapPin() = _uiState.update {
        it.copy(location = AddressLocationSnapshot(), error = null)
    }

    fun useCurrentLocation() {
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
                location = FormosaAddressGeocoding.invalidate(),
            )
        }
    }

    fun dismissBarrioSuggestions() {
        searchJob?.cancel()
        _uiState.update { it.copy(barrioOptions = emptyList(), isSearchingBarrios = false) }
    }

    fun submit() {
        val state = _uiState.value
        
        if (!state.canSubmit) {
            _uiState.update { it.copy(error = "Complete nombre, WhatsApp, barrio y calle") }
            return
        }
        
        if (!state.location.canPersist) {
            resolveFromAddress()
            return
        }
        
        persist()
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
                val contactoResult = saveClientContactoUseCase(
                    ClientContactoRequest(
                        nombre = state.nombre.trim(),
                        telefonoWhatsapp = AuthValidators.normalizeArgentinePhone(state.telefono),
                        email = state.email.trim().ifBlank { null },
                        favorito = true,
                    ),
                )
            ) {
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = contactoResult.error.userMessage()) }
                    return@launch
                }
                is AppResult.Success -> {
                    val contactId = contactoResult.data
                    val domicilioRequest = ClientDomicilioRequest(
                        barrioId = barrioId,
                        personaId = personaId,
                        nombre = "Domicilio principal",
                        calle = state.calle.trim(),
                        numero = state.numero.trim().ifBlank { null },
                        piso = state.piso.trim().ifBlank { null },
                        departamento = state.departamento.trim().ifBlank { null },
                        latitud = GeoCoordinates.roundForApi(location.latitude),
                        longitud = GeoCoordinates.roundForApi(location.longitude),
                        favorito = true,
                    )
                    val existingContact = when (val contacts = getClientContactosUseCase()) {
                        is AppResult.Success -> contacts.data.firstOrNull { it.id == contactId }
                            ?: contacts.data.firstOrNull { it.favorito }
                        is AppResult.Error -> null
                    }
                    val phoneVerified = existingContact?.telefonoVerificado == true
                    if (!phoneVerified) {
                        pendingClientOnboardingStore.pendingDomicilio = domicilioRequest
                        _uiState.update { it.copy(isLoading = false) }
                        _effects.emit(
                            ClientContactDomicilioEffect.NavigateToPhoneVerification(
                                contactId = contactId,
                                telefono = AuthValidators.normalizeArgentinePhone(state.telefono),
                            ),
                        )
                        return@launch
                    }
                    when (val domicilioResult = saveClientDomicilioUseCase(domicilioRequest)) {
                        is AppResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _effects.emit(ClientContactDomicilioEffect.Saved)
                        }
                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Contacto guardado, pero el domicilio falló: ${domicilioResult.error.userMessage()}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun User?.displayName(): String =
    listOfNotNull(this?.firstName, this?.lastName)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
