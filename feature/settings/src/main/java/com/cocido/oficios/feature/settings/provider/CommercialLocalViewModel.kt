package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.toRubroFullRequest
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
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
    private val getCurrentLocation: GetCurrentLocationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommercialLocalUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<CommercialLocalEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var searchJob: Job? = null
    private var targetService: ManagedProviderService? = null
    private var paymentMethods: List<String> = emptyList()

    init {
        load()
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
            is CommercialLocalEvent.CalleChanged ->
                update { it.copy(calle = event.value, errorMessage = null) }
            is CommercialLocalEvent.MapLocationSelected -> update {
                it.copy(latitud = event.latitude, longitud = event.longitude, errorMessage = null)
            }
            CommercialLocalEvent.UseCurrentLocation -> useCurrentLocation()
            CommercialLocalEvent.LocationPermissionDenied -> update {
                it.copy(errorMessage = "Permita la ubicación o marque manualmente la entrada de su local en el mapa.")
            }
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
                            barrioOptions = emptyList(),
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
            )
        }
    }

    private fun useCurrentLocation() {
        viewModelScope.launch {
            update { it.copy(isLocating = true, errorMessage = null) }
            when (val result = getCurrentLocation()) {
                is AppResult.Success -> update {
                    it.copy(
                        isLocating = false,
                        latitud = result.data.latitude,
                        longitud = result.data.longitude,
                    )
                }
                is AppResult.Error -> update {
                    it.copy(isLocating = false, errorMessage = result.error.userMessage())
                }
            }
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
            update { it.copy(errorMessage = "Complete barrio, calle y la ubicación en el mapa.") }
            return
        }
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            val request = service.toRubroFullRequest(
                paymentMethods = paymentMethods,
                servesAtLocal = true,
                domicilioLocal = ProviderLocalDomicilioRequest(
                    barrioId = barrioId,
                    calle = state.calle.trim(),
                    nombre = "Local comercial",
                    latitud = GeoCoordinates.roundForApi(state.latitud),
                    longitud = GeoCoordinates.roundForApi(state.longitud),
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
