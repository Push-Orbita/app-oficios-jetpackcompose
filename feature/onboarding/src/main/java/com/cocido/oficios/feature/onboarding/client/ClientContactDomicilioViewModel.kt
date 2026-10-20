package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.profile.GetClientContactosUseCase
import com.cocido.oficios.domain.usecase.profile.GetClientDomiciliosUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOption
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOptions
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.validation.AuthValidators
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

data class ClientContactDomicilioUiState(
    val nombre: String = "",
    val telefono: String = "",
    val selectedCountry: PhoneCountryOption = PhoneCountryOptions.ARGENTINA,
    val email: String = "",
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val isLocating: Boolean = false,
    val isLoading: Boolean = false,
    val isPreloading: Boolean = true,
    val error: String? = null,
) {
    val canSubmit: Boolean get() =
        nombre.isNotBlank() &&
            telefono.isNotBlank() &&
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
                    nombre = contacto?.nombre?.takeIf { name -> name.isNotBlank() } ?: account.displayName(),
                    telefono = contacto?.telefonoWhatsapp?.let(AuthValidators::normalizeArgentinePhone).orEmpty(),
                    email = contacto?.email?.takeIf { mail -> mail.isNotBlank() } ?: account?.email.orEmpty(),
                    barrioQuery = domicilio?.barrioName.orEmpty(),
                    selectedBarrioId = domicilio?.barrioId?.takeIf { id -> id > 0L },
                    calle = domicilio?.calle.orEmpty(),
                    numero = domicilio?.numero.orEmpty(),
                    piso = domicilio?.piso.orEmpty(),
                    departamento = domicilio?.departamento.orEmpty(),
                    latitud = domicilio?.latitud,
                    longitud = domicilio?.longitud,
                    isPreloading = false,
                )
            }
        }
    }

    fun onNombreChanged(value: String) = _uiState.update { it.copy(nombre = value, error = null) }
    fun onTelefonoChanged(value: String) = _uiState.update { it.copy(telefono = value, error = null) }
    fun onCountrySelected(value: PhoneCountryOption) = _uiState.update { it.copy(selectedCountry = value) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onCalleChanged(value: String) = _uiState.update { it.copy(calle = value, error = null) }
    fun onNumeroChanged(value: String) = _uiState.update { it.copy(numero = value, error = null) }
    fun onPisoChanged(value: String) = _uiState.update { it.copy(piso = value, error = null) }
    fun onDepartamentoChanged(value: String) = _uiState.update { it.copy(departamento = value, error = null) }
    fun onMapLocationSelected(latitude: Double, longitude: Double) = _uiState.update {
        it.copy(latitud = latitude, longitud = longitude, error = null)
    }

    fun onLocationPermissionDenied() = _uiState.update {
        it.copy(error = "Permití la ubicación o marcá manualmente el punto de entrada en el mapa.")
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

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) {
            _uiState.update { it.copy(error = "Complete nombre, WhatsApp, barrio y calle") }
            return
        }
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
                        latitud = GeoCoordinates.roundForApi(state.latitud),
                        longitud = GeoCoordinates.roundForApi(state.longitud),
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
