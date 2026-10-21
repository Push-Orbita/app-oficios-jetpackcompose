package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.toRubroFullRequest
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CommercialLocalViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val searchBarrios: SearchBarriosUseCase,
    private val geocodeLocalAddress: GeocodeLocalAddressUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommercialLocalUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<CommercialLocalEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var searchJob: Job? = null
    private var targetService: ManagedProviderService? = null
    private var paymentMethods: List<String> = emptyList()
    private var skipNextResume = true

    init {
        load()
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        val state = _uiState.value
        if (state.isSaving) return
        if (state.loadFailed || state.missingService) load()
    }

    fun onEvent(event: CommercialLocalEvent) {
        when (event) {
            CommercialLocalEvent.Retry -> load()
            CommercialLocalEvent.Back -> emit(CommercialLocalEffect.NavigateBack)
            CommercialLocalEvent.AddService -> emit(CommercialLocalEffect.NavigateToAddService)
            is CommercialLocalEvent.BarrioQueryChanged -> searchBarrio(event.value)
            CommercialLocalEvent.BarrioFocused ->
                searchBarrio(_uiState.value.barrioQuery, preserveSelection = true)
            is CommercialLocalEvent.BarrioSelected -> onBarrioSelected(event.option)
            CommercialLocalEvent.BarrioSuggestionsDismissed -> {
                searchJob?.cancel()
                update { it.copy(barrioOptions = emptyList(), isSearchingBarrios = false) }
            }
            is CommercialLocalEvent.CalleChanged -> update {
                it.copy(
                    calle = event.value,
                    errorMessage = null,
                    location = FormosaAddressGeocoding.invalidate(),
                )
            }
            is CommercialLocalEvent.NumeroChanged -> update {
                it.copy(
                    numero = event.value,
                    errorMessage = null,
                    location = FormosaAddressGeocoding.invalidate(),
                )
            }
            is CommercialLocalEvent.MapLocationSelected -> update {
                it.copy(
                    location = FormosaAddressGeocoding.pinMoved(it.location, event.latitude, event.longitude),
                    errorMessage = null,
                )
            }
            CommercialLocalEvent.LocateOnMap -> update {
                val next = FormosaAddressGeocoding.openMap(it.location)
                it.copy(location = next, errorMessage = next.message)
            }
            CommercialLocalEvent.ConfirmPin -> confirmAndSave()
            CommercialLocalEvent.Submit -> submit()
        }
    }

    private fun load() {
        viewModelScope.launch {
            update {
                it.copy(isLoading = true, missingService = false, loadFailed = false, errorMessage = null)
            }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Error -> update {
                    it.copy(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = result.error.userMessage(),
                    )
                }
                is AppResult.Success -> {
                    val details = result.data.providerDetails
                    val service = details?.services?.firstOrNull { it.isPrimary }
                        ?: details?.services?.firstOrNull()
                    if (details == null || service == null) {
                        targetService = null
                        paymentMethods = emptyList()
                        update {
                            it.copy(
                                isLoading = false,
                                missingService = true,
                                loadFailed = false,
                                errorMessage = null,
                            )
                        }
                        return@launch
                    }
                    targetService = service
                    paymentMethods = details.paymentMethods
                    val local = details.commercialLocal
                    update {
                        it.copy(
                            isLoading = false,
                            missingService = false,
                            loadFailed = false,
                            errorMessage = null,
                            barrioQuery = local?.barrioName.orEmpty(),
                            selectedBarrioId = local?.barrioId,
                            calle = local?.street.orEmpty(),
                            numero = "",
                            barrioOptions = emptyList(),
                            location = AddressLocationSnapshot(),
                        )
                    }
                }
            }
        }
    }

    private fun searchBarrio(query: String, preserveSelection: Boolean = false) {
        update {
            it.copy(
                barrioQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                isSearchingBarrios = true,
                errorMessage = null,
                location = if (preserveSelection) it.location else FormosaAddressGeocoding.invalidate(),
            )
        }
        searchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = searchJob,
            query = query,
            searchBarrios = searchBarrios,
            onResult = { options ->
                update { it.copy(isSearchingBarrios = false, barrioOptions = options) }
            },
        )
    }

    private fun onBarrioSelected(option: CatalogOption) {
        searchJob?.cancel()
        update {
            it.copy(
                barrioQuery = option.label,
                selectedBarrioId = option.id,
                barrioOptions = emptyList(),
                isSearchingBarrios = false,
                errorMessage = null,
                location = FormosaAddressGeocoding.invalidate(),
            )
        }
    }

    private fun submit() {
        val state = _uiState.value
        val barrioId = state.selectedBarrioId
        val service = targetService
        if (service == null) {
            update { it.copy(missingService = true) }
            return
        }
        if (barrioId == null || !state.canSubmit) {
            update { it.copy(errorMessage = "Complete barrio, calle y número de su local.") }
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
            update {
                it.copy(
                    isGeocoding = true,
                    errorMessage = null,
                    location = it.location.copy(phase = AddressLocationPhase.Geocoding),
                )
            }
            val result = geocodeLocalAddress(
                LocalAddressQuery(
                    street = state.calle,
                    number = state.numero,
                    neighborhood = state.barrioQuery,
                ),
            )
            when (result) {
                is AppResult.Error -> update {
                    it.copy(
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
                    update {
                        it.copy(
                            isGeocoding = false,
                            location = snapshot,
                            errorMessage = snapshot.message,
                        )
                    }
                }
            }
        }
    }

    private fun confirmAndSave() {
        val confirmed = FormosaAddressGeocoding.confirmPin(_uiState.value.location)
        update { it.copy(location = confirmed, errorMessage = confirmed.message) }
        if (confirmed.canPersist) persist()
    }

    private fun persist() {
        val state = _uiState.value
        val barrioId = state.selectedBarrioId ?: return
        val service = targetService ?: return
        val location = state.location
        if (!location.canPersist) return
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            val streetLine = listOf(state.calle.trim(), state.numero.trim())
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val request = service.toRubroFullRequest(
                paymentMethods = paymentMethods,
                servesAtLocal = true,
                domicilioLocal = ProviderLocalDomicilioRequest(
                    barrioId = barrioId,
                    calle = streetLine,
                    nombre = "Local comercial",
                    latitud = GeoCoordinates.roundForApi(location.latitude),
                    longitud = GeoCoordinates.roundForApi(location.longitude),
                ),
            )
            when (val result = accountUseCases.replaceService(service.id, service.isPrimary, request)) {
                is AppResult.Success -> {
                    update { it.copy(isSaving = false) }
                    emit(CommercialLocalEffect.ShowSuccess("Local comercial guardado."))
                    emit(CommercialLocalEffect.NavigateBack)
                }
                is AppResult.Error -> {
                    val message = result.error.userMessage()
                    update { it.copy(isSaving = false, errorMessage = message) }
                    emit(CommercialLocalEffect.ShowError(message))
                }
            }
        }
    }

    private fun update(block: (CommercialLocalUiState) -> CommercialLocalUiState) = _uiState.update(block)
    private fun emit(effect: CommercialLocalEffect) = viewModelScope.launch { _effects.send(effect) }
}
