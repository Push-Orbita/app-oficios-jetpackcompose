package com.cocido.oficios.feature.settings.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.location.launchDebouncedForwardGeocode
import com.cocido.oficios.domain.validation.AuthValidators
import com.cocido.oficios.domain.validation.DomicilioFieldValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ClientDataViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val geocodeLocalAddress: GeocodeLocalAddressUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientDataUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<ClientDataEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    private var barrioSearchJob: Job? = null
    private var geocodeJob: Job? = null
    private var skipNextResume = true

    init {
        viewModelScope.launch {
            accountUseCases.domicilios.collect { list ->
                if (list != null) update { it.copy(domicilios = list) }
            }
        }
        viewModelScope.launch {
            accountUseCases.contactos.collect { list ->
                if (list != null) update { it.copy(contacts = list) }
            }
        }
        load()
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        load()
    }

    fun onEvent(event: ClientDataEvent) {
        when (event) {
            ClientDataEvent.Retry -> load()
            ClientDataEvent.Back -> emit(ClientDataEffect.NavigateBack)
            ClientDataEvent.CloseEditor -> closeEditor()
            ClientDataEvent.AddContact -> openContact()
            is ClientDataEvent.EditContact -> openContact(event.contact)
            ClientDataEvent.AddDomicilio -> openDomicilio()
            is ClientDataEvent.EditDomicilio -> openDomicilio(event.domicilio)
            is ClientDataEvent.ContactNameChanged -> update {
                it.copy(contactName = event.value, errorMessage = null).withContactFieldErrors()
            }
            is ClientDataEvent.ContactPhoneChanged -> update {
                it.copy(contactPhone = event.value, errorMessage = null).withContactFieldErrors()
            }
            is ClientDataEvent.ContactEmailChanged -> update {
                it.copy(contactEmail = event.value, errorMessage = null).withContactFieldErrors()
            }
            is ClientDataEvent.ContactFavoriteChanged -> update { it.copy(contactFavorite = event.value) }
            ClientDataEvent.SaveContact -> saveContact()
            is ClientDataEvent.AddressNameChanged -> update { it.copy(addressName = event.value) }
            is ClientDataEvent.BarrioQueryChanged -> searchBarrio(event.value)
            ClientDataEvent.BarrioFocused ->
                searchBarrio(_uiState.value.barrioQuery, preserveSelection = true)
            is ClientDataEvent.BarrioSelected -> {
                barrioSearchJob?.cancel()
                update {
                    it.copy(
                        barrioId = event.option.id,
                        barrioQuery = event.option.label,
                        barrioSuggestions = emptyList(),
                        isSearchingBarrios = false,
                        errorMessage = null,
                    ).withAddressFieldErrors()
                }
                scheduleLiveGeocode()
            }
            ClientDataEvent.BarrioSuggestionsDismissed -> {
                barrioSearchJob?.cancel()
                update { it.copy(barrioSuggestions = emptyList(), isSearchingBarrios = false) }
            }
            is ClientDataEvent.StreetChanged -> {
                update {
                    it.copy(street = event.value, errorMessage = null).withAddressFieldErrors()
                }
                scheduleLiveGeocode()
            }
            is ClientDataEvent.NumberChanged -> {
                update {
                    it.copy(number = event.value, errorMessage = null).withAddressFieldErrors()
                }
                scheduleLiveGeocode()
            }
            is ClientDataEvent.FloorChanged -> update { it.copy(floor = event.value) }
            is ClientDataEvent.ApartmentChanged -> update { it.copy(apartment = event.value) }
            is ClientDataEvent.HouseTowerChanged -> update { it.copy(houseTower = event.value) }
            is ClientDataEvent.MapLocationSelected -> {
                cancelLiveGeocode()
                update {
                    it.copy(
                        addressLatitude = event.latitude,
                        addressLongitude = event.longitude,
                        errorMessage = null,
                    )
                }
            }
            ClientDataEvent.UseCurrentLocation -> useCurrentLocation()
            ClientDataEvent.LocationPermissionDenied -> update {
                it.copy(errorMessage = "Para usar su ubicación, permita el acceso cuando Android se lo solicite. También puede marcar el punto manualmente.")
            }
            is ClientDataEvent.AddressFavoriteChanged -> update { it.copy(addressFavorite = event.value) }
            ClientDataEvent.SaveDomicilio -> saveDomicilio()
            is ClientDataEvent.ToggleContactFavorite -> toggleContactFavorite(event.id, event.favorite)
            is ClientDataEvent.ToggleDomicilioFavorite -> toggleDomicilioFavorite(event.id, event.favorite)
            is ClientDataEvent.RequestDelete -> update { it.copy(pendingDelete = event.target) }
            ClientDataEvent.ConfirmDelete -> confirmDelete()
            ClientDataEvent.DismissDelete -> update { it.copy(pendingDelete = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val keepContent = _uiState.value.contacts.isNotEmpty() || _uiState.value.domicilios.isNotEmpty()
            _uiState.update { it.copy(isLoading = !keepContent, errorMessage = null) }
            val result = coroutineScope {
                val profile = async { accountUseCases.getProfile() }
                val contacts = async { accountUseCases.getContactos() }
                val domicilios = async { accountUseCases.getDomicilios() }
                Triple(profile.await(), contacts.await(), domicilios.await())
            }
            val profile = result.first
            val contacts = result.second
            val domicilios = result.third
            val error = listOf(profile, contacts, domicilios).filterIsInstance<AppResult.Error>().firstOrNull()
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, isSaving = false, errorMessage = error.error.userMessage()) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSaving = false,
                    personaId = (profile as AppResult.Success).data.user.personaId,
                    contacts = (contacts as AppResult.Success).data,
                    domicilios = (domicilios as AppResult.Success).data,
                    errorMessage = null,
                )
            }
        }
    }

    private fun openContact(contact: com.cocido.oficios.domain.model.ClientContacto? = null) {
        _uiState.update {
            it.copy(
                mode = ClientDataMode.CONTACT,
                editingId = contact?.id,
                contactName = contact?.nombre.orEmpty(),
                contactPhone = contact?.telefonoWhatsapp?.let(AuthValidators::normalizeArgentinePhone).orEmpty(),
                contactEmail = contact?.email.orEmpty(),
                contactFavorite = contact?.favorito ?: it.contacts.isEmpty(),
                originalPhone = contact?.telefonoWhatsapp?.let(AuthValidators::normalizeArgentinePhone).orEmpty(),
                originalPhoneVerified = contact?.telefonoVerificado ?: false,
                isSaving = false,
                errorMessage = null,
            ).withContactFieldErrors()
        }
    }

    private fun openDomicilio(domicilio: com.cocido.oficios.domain.model.ClientDomicilio? = null) {
        _uiState.update {
            it.copy(
                mode = ClientDataMode.DOMICILIO,
                editingId = domicilio?.id,
                addressName = domicilio?.nombre.orEmpty(),
                barrioQuery = domicilio?.barrioName.orEmpty(),
                barrioId = domicilio?.barrioId?.takeIf { id -> id > 0L },
                street = domicilio?.calle.orEmpty(),
                number = domicilio?.numero.orEmpty(),
                floor = domicilio?.piso.orEmpty(),
                apartment = domicilio?.departamento.orEmpty(),
                houseTower = domicilio?.casaTorre.orEmpty(),
                addressLatitude = domicilio?.latitud,
                addressLongitude = domicilio?.longitud,
                isLocating = false,
                addressFavorite = domicilio?.favorito ?: it.domicilios.isEmpty(),
                barrioSuggestions = emptyList(),
                isSaving = false,
                errorMessage = null,
            ).withAddressFieldErrors()
        }
    }

    private fun closeEditor() {
        barrioSearchJob?.cancel()
        cancelLiveGeocode()
        _uiState.update {
            it.copy(mode = ClientDataMode.LIST, editingId = null, errorMessage = null, barrioSuggestions = emptyList())
        }
    }

    private fun saveContact() {
        val validated = _uiState.value.withContactFieldErrors(showBlank = true)
        _uiState.value = validated
        if (!validated.canSaveContact) return
        val state = validated
        val phone = AuthValidators.normalizeArgentinePhone(state.contactPhone)
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            val id = state.editingId
            val result = if (id == null) {
                accountUseCases.createContacto(
                    ClientContactoRequest(
                        nombre = state.contactName.trim(),
                        telefonoWhatsapp = phone,
                        email = state.contactEmail.trim().ifBlank { null },
                        favorito = state.contactFavorite,
                    ),
                )
            } else {
                accountUseCases.updateContacto(
                    id,
                    UpdateClientContactoRequest(
                        nombre = state.contactName.trim(),
                        telefonoWhatsapp = phone,
                        email = state.contactEmail.trim().ifBlank { null },
                        favorito = state.contactFavorite,
                    ),
                ).let { updateResult ->
                    when (updateResult) {
                        is AppResult.Success -> AppResult.Success(id)
                        is AppResult.Error -> updateResult
                    }
                }
            }
            when (result) {
                is AppResult.Success -> {
                    val needsVerification = !state.originalPhoneVerified || phone != state.originalPhone
                    closeEditor()
                    load()
                    if (needsVerification) {
                        emit(ClientDataEffect.VerifyPhone(result.data, phone))
                    } else {
                        emit(ClientDataEffect.ShowMessage("Contacto actualizado."))
                    }
                }
                is AppResult.Error -> update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun saveDomicilio() {
        cancelLiveGeocode()
        val validated = _uiState.value.withAddressFieldErrors(showBlank = true)
        if (validated.editingId == null && validated.personaId == null) {
            update { validated.copy(errorMessage = "No pudimos identificar a qué cuenta pertenece el domicilio.") }
            return
        }
        _uiState.value = validated
        if (!validated.canSaveDomicilio) return
        val state = validated
        val barrioId = state.barrioId
        val personaId = state.personaId
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (state.editingId == null) {
                accountUseCases.createDomicilio(
                    ClientDomicilioRequest(
                        barrioId = barrioId!!,
                        personaId = personaId!!,
                        nombre = state.addressName.trim().ifBlank { null },
                        calle = state.street.trim(),
                        numero = state.number.trim(),
                        casaTorre = state.houseTower.trim().ifBlank { null },
                        departamento = state.apartment.trim().ifBlank { null },
                        piso = state.floor.trim().ifBlank { null },
                        latitud = GeoCoordinates.roundForApi(state.addressLatitude),
                        longitud = GeoCoordinates.roundForApi(state.addressLongitude),
                        favorito = state.addressFavorite,
                    ),
                )
            } else {
                accountUseCases.updateDomicilio(
                    state.editingId,
                    UpdateClientDomicilioRequest(
                        barrioId = barrioId!!,
                        nombre = state.addressName.trim().ifBlank { null },
                        calle = state.street.trim(),
                        numero = state.number.trim(),
                        casaTorre = state.houseTower.trim().ifBlank { null },
                        departamento = state.apartment.trim().ifBlank { null },
                        piso = state.floor.trim().ifBlank { null },
                        latitud = GeoCoordinates.roundForApi(state.addressLatitude),
                        longitud = GeoCoordinates.roundForApi(state.addressLongitude),
                        favorito = state.addressFavorite,
                    ),
                )
            }
            when (result) {
                is AppResult.Success -> {
                    closeEditor()
                    load()
                    emit(ClientDataEffect.ShowMessage("Domicilio guardado."))
                }
                is AppResult.Error -> update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun searchBarrio(query: String, preserveSelection: Boolean = false) {
        update {
            it.copy(
                barrioQuery = query,
                barrioId = if (preserveSelection) it.barrioId else null,
                isSearchingBarrios = true,
                errorMessage = null,
            ).withAddressFieldErrors()
        }
        if (!preserveSelection) scheduleLiveGeocode()
        barrioSearchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = barrioSearchJob,
            query = query,
            searchBarrios = searchBarriosUseCase,
            onResult = { options ->
                update { it.copy(isSearchingBarrios = false, barrioSuggestions = options) }
            },
        )
    }

    private fun useCurrentLocation() {
        cancelLiveGeocode()
        viewModelScope.launch {
            update { it.copy(isLocating = true, errorMessage = null) }
            when (val result = getCurrentLocationUseCase()) {
                is AppResult.Success -> update {
                    it.copy(
                        isLocating = false,
                        addressLatitude = result.data.latitude,
                        addressLongitude = result.data.longitude,
                    )
                }
                is AppResult.Error -> update {
                    it.copy(isLocating = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun toggleContactFavorite(id: Long, favorite: Boolean) = runAction(
        action = { accountUseCases.setFavoriteContacto(id, favorite) },
        successMessage = if (favorite) "Contacto principal actualizado." else "Contacto desmarcado.",
    )

    private fun toggleDomicilioFavorite(id: Long, favorite: Boolean) = runAction(
        action = { accountUseCases.setFavoriteDomicilio(id, favorite) },
        successMessage = if (favorite) "Domicilio principal actualizado." else "Domicilio desmarcado.",
    )

    private fun confirmDelete() {
        val target = _uiState.value.pendingDelete ?: return
        runAction(
            action = {
                when (target) {
                    is ClientDataDeleteTarget.Contact -> accountUseCases.deleteContacto(target.id)
                    is ClientDataDeleteTarget.Domicilio -> accountUseCases.deleteDomicilio(target.id)
                }
            },
            successMessage = "Se eliminó ${target.label}.",
        )
        update { it.copy(pendingDelete = null) }
    }

    private fun runAction(action: suspend () -> AppResult<Unit>, successMessage: String) {
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = action()) {
                is AppResult.Success -> {
                    load()
                    emit(ClientDataEffect.ShowMessage(successMessage))
                }
                is AppResult.Error -> update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun scheduleLiveGeocode() {
        val state = _uiState.value
        geocodeJob = viewModelScope.launchDebouncedForwardGeocode(
            previous = geocodeJob,
            query = LocalAddressQuery(
                street = state.street,
                number = state.number,
                neighborhood = state.barrioQuery,
            ),
            geocode = geocodeLocalAddress,
            onResult = { result ->
                val outcome = (result as? AppResult.Success)?.data ?: return@launchDebouncedForwardGeocode
                val current = AddressLocationSnapshot(
                    latitude = _uiState.value.addressLatitude,
                    longitude = _uiState.value.addressLongitude,
                )
                val live = FormosaAddressGeocoding.applyLiveOutcome(outcome, current)
                if (live.latitude != null && live.longitude != null) {
                    update {
                        it.copy(
                            addressLatitude = live.latitude,
                            addressLongitude = live.longitude,
                        )
                    }
                }
            },
        )
    }

    private fun cancelLiveGeocode() {
        geocodeJob?.cancel()
        geocodeJob = null
    }

    private fun update(block: (ClientDataUiState) -> ClientDataUiState) = _uiState.update(block)
    private fun emit(effect: ClientDataEffect) { viewModelScope.launch { _effects.send(effect) } }
}

private fun ClientDataUiState.withContactFieldErrors(showBlank: Boolean = false): ClientDataUiState {
    val nameError = AuthValidators.validateContactName(contactName)
    val phoneError = AuthValidators.validateArgentineMobile(contactPhone)
    val emailError = AuthValidators.validateOptionalEmail(contactEmail)
    return copy(
        contactNameError = nameError.takeIf { showBlank || contactName.isNotBlank() },
        contactPhoneError = phoneError.takeIf { showBlank || contactPhone.isNotBlank() },
        contactEmailError = emailError.takeIf { showBlank || contactEmail.isNotBlank() },
        canSaveContact = nameError == null && phoneError == null && emailError == null,
    )
}

private fun ClientDataUiState.withAddressFieldErrors(showBlank: Boolean = false): ClientDataUiState {
    val errors = DomicilioFieldValidation.validate(
        barrioId = barrioId,
        barrioQuery = barrioQuery,
        street = street,
        number = number,
        personaMissing = editingId == null && personaId == null,
        showBlank = showBlank,
    )
    return copy(
        addressBarrioError = errors.barrioError,
        addressStreetError = errors.streetError,
        addressNumberError = errors.numberError,
        canSaveDomicilio = errors.canSave,
    )
}
