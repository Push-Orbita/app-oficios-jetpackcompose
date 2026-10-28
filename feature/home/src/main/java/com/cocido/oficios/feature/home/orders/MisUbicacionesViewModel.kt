package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.validation.DomicilioFieldValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MisUbicacionesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountUseCases: AccountUseCases,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val geocodeLocalAddress: GeocodeLocalAddressUseCase,
) : ViewModel() {

    private val initialSelectedId: Long? = savedStateHandle.get<Long>("selectedId")?.takeIf { it > 0L }

    private val _uiState = MutableStateFlow<MisUbicacionesUiState>(MisUbicacionesUiState.Loading)
    val uiState: StateFlow<MisUbicacionesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MisUbicacionesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var barrioSearchJob: Job? = null
    private var personaId: Long? = null
    private var skipNextResume = true

    init {
        viewModelScope.launch {
            accountUseCases.domicilios.collect { list ->
                if (list == null) return@collect
                updateSuccess {
                    copy(
                        domicilios = list,
                        selectedId = list.firstOrNull { it.id == selectedId }?.id
                            ?: list.firstOrNull { it.favorito }?.id
                            ?: list.singleOrNull()?.id,
                    )
                }
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

    fun onEvent(event: MisUbicacionesEvent) {
        when (event) {
            MisUbicacionesEvent.Retry -> load()
            is MisUbicacionesEvent.LocationSelected -> updateSuccess {
                copy(selectedId = event.id, errorMessage = null)
            }
            is MisUbicacionesEvent.FavoriteToggled -> toggleFavorite(event.id, event.favorite)
            MisUbicacionesEvent.AddClicked,
            MisUbicacionesEvent.MarkOnMapClicked,
            -> openAdd()
            MisUbicacionesEvent.CancelAdd -> updateSuccess {
                copy(mode = MisUbicacionesMode.PICKER, errorMessage = null)
            }
            is MisUbicacionesEvent.BarrioQueryChanged -> searchBarrio(event.value)
            MisUbicacionesEvent.BarrioFocused -> {
                val query = (uiState.value as? MisUbicacionesUiState.Success)?.barrioQuery.orEmpty()
                searchBarrio(query, preserveSelection = true)
            }
            MisUbicacionesEvent.BarrioSuggestionsDismissed -> updateSuccess {
                copy(barrioSuggestions = emptyList())
            }
            is MisUbicacionesEvent.BarrioSelected -> updateSuccess {
                copy(
                    barrioQuery = event.option.label,
                    barrioId = event.option.id,
                    barrioSuggestions = emptyList(),
                    errorMessage = null,
                    location = FormosaAddressGeocoding.invalidate(),
                ).withAddressFieldErrors()
            }
            is MisUbicacionesEvent.StreetChanged -> updateSuccess {
                copy(
                    street = event.value,
                    errorMessage = null,
                    location = FormosaAddressGeocoding.invalidate(),
                ).withAddressFieldErrors()
            }
            is MisUbicacionesEvent.NumberChanged -> updateSuccess {
                copy(
                    number = event.value,
                    errorMessage = null,
                    location = FormosaAddressGeocoding.invalidate(),
                ).withAddressFieldErrors()
            }
            is MisUbicacionesEvent.MapLocationSelected -> updateSuccess {
                copy(
                    location = FormosaAddressGeocoding.pinMoved(location, event.latitude, event.longitude),
                    errorMessage = null,
                )
            }
            MisUbicacionesEvent.LocateOnMap -> updateSuccess {
                val next = FormosaAddressGeocoding.openMap(location)
                copy(location = next, errorMessage = next.message)
            }
            MisUbicacionesEvent.ConfirmPin -> {
                val current = uiState.value as? MisUbicacionesUiState.Success ?: return
                val confirmed = FormosaAddressGeocoding.confirmPin(current.location)
                updateSuccess { copy(location = confirmed, errorMessage = confirmed.message) }
                if (confirmed.canPersist) saveNew()
            }
            is MisUbicacionesEvent.FavoriteNewChanged -> updateSuccess {
                copy(addressFavorite = event.value)
            }
            MisUbicacionesEvent.SaveNew -> saveNew()
            MisUbicacionesEvent.Confirm -> confirm()
        }
    }

    private fun load() {
        viewModelScope.launch { loadInternal() }
    }

    private suspend fun loadInternal(preferredId: Long? = null) {
        _uiState.update { current ->
            if (current is MisUbicacionesUiState.Success) current.copy(errorMessage = null) else MisUbicacionesUiState.Loading
        }
        val profile = accountUseCases.getProfile()
        val domicilios = accountUseCases.getDomicilios()
        val error = listOf(profile, domicilios).filterIsInstance<AppResult.Error>().firstOrNull()
        if (error != null) {
            _uiState.update { MisUbicacionesUiState.Error(error.error.userMessage()) }
            return
        }
        personaId = (profile as AppResult.Success).data.user.personaId
        val list = (domicilios as AppResult.Success).data
        val previousSelected = (uiState.value as? MisUbicacionesUiState.Success)?.selectedId
        val selected = list.firstOrNull { it.id == preferredId }?.id
            ?: list.firstOrNull { it.id == previousSelected }?.id
            ?: list.firstOrNull { it.id == initialSelectedId }?.id
            ?: list.firstOrNull { it.favorito }?.id
            ?: list.singleOrNull()?.id
        _uiState.update {
            MisUbicacionesUiState.Success(
                domicilios = list,
                selectedId = selected,
                addressFavorite = list.isEmpty(),
            )
        }
    }

    private fun openAdd() {
        updateSuccess {
            copy(
                mode = MisUbicacionesMode.ADD,
                barrioQuery = "",
                barrioId = null,
                barrioSuggestions = emptyList(),
                street = "",
                number = "",
                location = AddressLocationSnapshot(),
                isGeocoding = false,
                addressFavorite = domicilios.isEmpty(),
                isSaving = false,
                errorMessage = null,
            ).withAddressFieldErrors()
        }
    }

    private fun saveNew() {
        val state = (uiState.value as? MisUbicacionesUiState.Success)
            ?.withAddressFieldErrors(showBlank = true) ?: return
        updateSuccess { state }
        val barrioId = state.barrioId
        val ownerId = personaId
        if (!state.canSaveNew) {
            if (ownerId == null) {
                updateSuccess { copy(errorMessage = DomicilioFieldValidation.PERSONA_REQUIRED) }
            }
            return
        }
        if (!state.location.canPersist) {
            resolveFromAddress()
            return
        }
        viewModelScope.launch {
            updateSuccess { copy(isSaving = true, errorMessage = null) }
            val result = accountUseCases.createDomicilio(
                ClientDomicilioRequest(
                    barrioId = barrioId!!,
                    personaId = ownerId!!,
                    calle = state.street.trim(),
                    numero = state.number.trim(),
                    latitud = GeoCoordinates.roundForApi(state.location.latitude),
                    longitud = GeoCoordinates.roundForApi(state.location.longitude),
                    favorito = state.addressFavorite,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    val createdStreet = state.street.trim()
                    val createdNumber = state.number.trim()
                    loadInternal()
                    updateSuccess {
                        val match = domicilios.lastOrNull {
                            it.calle.equals(createdStreet, ignoreCase = true) &&
                                it.numero.equals(createdNumber, ignoreCase = true)
                        }
                        copy(selectedId = match?.id ?: selectedId, mode = MisUbicacionesMode.PICKER)
                    }
                }
                is AppResult.Error -> updateSuccess {
                    copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun toggleFavorite(id: Long, favorite: Boolean) {
        viewModelScope.launch {
            when (val result = accountUseCases.setFavoriteDomicilio(id, favorite)) {
                is AppResult.Success -> loadInternal(preferredId = id)
                is AppResult.Error -> updateSuccess { copy(errorMessage = result.error.userMessage()) }
            }
        }
    }

    private fun confirm() {
        val selected = (uiState.value as? MisUbicacionesUiState.Success)?.selectedId
        if (selected == null) {
            updateSuccess { copy(errorMessage = "Seleccione una ubicación para continuar.") }
            return
        }
        viewModelScope.launch {
            _effects.send(MisUbicacionesEffect.Confirmed(selected))
        }
    }

    private fun searchBarrio(query: String, preserveSelection: Boolean = false) {
        updateSuccess {
            copy(
                barrioQuery = query,
                barrioId = if (preserveSelection) barrioId else null,
                isSearchingBarrios = true,
                errorMessage = null,
            ).withAddressFieldErrors()
        }
        barrioSearchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = barrioSearchJob,
            query = query,
            searchBarrios = searchBarriosUseCase,
            onResult = { options ->
                updateSuccess { copy(isSearchingBarrios = false, barrioSuggestions = options) }
            },
        )
    }

    private fun resolveFromAddress() {
        val state = uiState.value as? MisUbicacionesUiState.Success ?: return
        viewModelScope.launch {
            updateSuccess {
                copy(
                    isGeocoding = true,
                    location = location.copy(phase = AddressLocationPhase.Geocoding),
                    errorMessage = null,
                )
            }
            when (
                val result = geocodeLocalAddress(
                    LocalAddressQuery(
                        street = state.street,
                        number = state.number,
                        neighborhood = state.barrioQuery,
                    ),
                )
            ) {
                is AppResult.Error -> updateSuccess {
                    copy(
                        isGeocoding = false,
                        location = AddressLocationSnapshot(
                            phase = AddressLocationPhase.NeedsMap,
                            message = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                        ),
                        errorMessage = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                    )
                }
                is AppResult.Success -> {
                    val snapshot = FormosaAddressGeocoding.snapshotFor(result.data)
                    updateSuccess {
                        copy(isGeocoding = false, location = snapshot, errorMessage = snapshot.message)
                    }
                }
            }
        }
    }

    private fun MisUbicacionesUiState.Success.withAddressFieldErrors(
        showBlank: Boolean = false,
    ): MisUbicacionesUiState.Success {
        val errors = DomicilioFieldValidation.validate(
            barrioId = barrioId,
            barrioQuery = barrioQuery,
            street = street,
            number = number,
            personaMissing = personaId == null,
            showBlank = showBlank,
        )
        return copy(
            addressBarrioError = errors.barrioError,
            addressStreetError = errors.streetError,
            addressNumberError = errors.numberError,
            canSaveNew = errors.canSave,
        )
    }

    private inline fun updateSuccess(
        transform: MisUbicacionesUiState.Success.() -> MisUbicacionesUiState.Success,
    ) {
        _uiState.update { current ->
            if (current is MisUbicacionesUiState.Success) current.transform() else current
        }
    }
}
