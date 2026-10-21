package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
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
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
) : ViewModel() {

    private val initialSelectedId: Long? = savedStateHandle.get<Long>("selectedId")?.takeIf { it > 0L }

    private val _uiState = MutableStateFlow<MisUbicacionesUiState>(MisUbicacionesUiState.Loading)
    val uiState: StateFlow<MisUbicacionesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MisUbicacionesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var barrioSearchJob: Job? = null
    private var personaId: Long? = null

    init {
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
                )
            }
            is MisUbicacionesEvent.StreetChanged -> updateSuccess {
                copy(street = event.value, errorMessage = null)
            }
            is MisUbicacionesEvent.NumberChanged -> updateSuccess {
                copy(number = event.value, errorMessage = null)
            }
            is MisUbicacionesEvent.MapLocationSelected -> updateSuccess {
                copy(addressLatitude = event.latitude, addressLongitude = event.longitude, errorMessage = null)
            }
            MisUbicacionesEvent.UseCurrentLocation -> useCurrentLocation()
            MisUbicacionesEvent.LocationPermissionDenied -> updateSuccess {
                copy(
                    errorMessage = "Para usar su ubicación, permita el acceso cuando Android se lo solicite. También puede marcar el punto manualmente.",
                )
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
                addressLatitude = null,
                addressLongitude = null,
                isLocating = false,
                addressFavorite = domicilios.isEmpty(),
                isSaving = false,
                errorMessage = null,
            )
        }
    }

    private fun saveNew() {
        val state = uiState.value as? MisUbicacionesUiState.Success ?: return
        val barrioId = state.barrioId
        val ownerId = personaId
        val error = when {
            barrioId == null -> "Seleccione un barrio de la lista."
            state.street.isBlank() -> "Ingrese la calle."
            state.number.isBlank() -> "Ingrese la altura."
            ownerId == null -> "No pudimos identificar a qué cuenta pertenece el domicilio."
            else -> null
        }
        if (error != null) {
            updateSuccess { copy(errorMessage = error) }
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
                    latitud = GeoCoordinates.roundForApi(state.addressLatitude),
                    longitud = GeoCoordinates.roundForApi(state.addressLongitude),
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
            )
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

    private fun useCurrentLocation() {
        viewModelScope.launch {
            updateSuccess { copy(isLocating = true, errorMessage = null) }
            when (val result = getCurrentLocationUseCase()) {
                is AppResult.Success -> updateSuccess {
                    copy(
                        isLocating = false,
                        addressLatitude = result.data.latitude,
                        addressLongitude = result.data.longitude,
                    )
                }
                is AppResult.Error -> updateSuccess {
                    copy(isLocating = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private inline fun updateSuccess(
        transform: MisUbicacionesUiState.Success.() -> MisUbicacionesUiState.Success,
    ) {
        _uiState.update { current ->
            if (current is MisUbicacionesUiState.Success) current.transform() else current
        }
    }
}
