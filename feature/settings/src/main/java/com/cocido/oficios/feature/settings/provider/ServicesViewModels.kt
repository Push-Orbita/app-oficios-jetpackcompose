package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ProviderCommercialLocal
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.addRange
import com.cocido.oficios.domain.model.changeRange
import com.cocido.oficios.domain.model.removeRange
import com.cocido.oficios.domain.model.toWeeklyAvailability
import com.cocido.oficios.domain.model.withToggledDay
import com.cocido.oficios.domain.usecase.AvailabilityUseCases
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.validation.DomicilioFieldValidation
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
class ServicesViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<ServicesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    private var skipNextResume = true

    init {
        viewModelScope.launch {
            accountUseCases.providerDetails.collect { details ->
                if (details != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isDeleting = false,
                            services = details.services,
                            errorMessage = null,
                        )
                    }
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

    fun onEvent(event: ServicesEvent) {
        when (event) {
            ServicesEvent.Retry -> load()
            ServicesEvent.Back -> emit(ServicesEffect.NavigateBack)
            ServicesEvent.AddService -> emit(ServicesEffect.NavigateToAdd)
            is ServicesEvent.EditService -> emit(ServicesEffect.NavigateToEdit(event.service.id))
            is ServicesEvent.RequestDelete -> _uiState.update { it.copy(pendingDelete = event.service) }
            ServicesEvent.DismissDelete -> _uiState.update { it.copy(pendingDelete = null) }
            ServicesEvent.ConfirmDelete -> deletePending()
        }
    }

    fun load() {
        viewModelScope.launch {
            val keepContent = _uiState.value.services.isNotEmpty()
            _uiState.update { it.copy(isLoading = !keepContent, errorMessage = null) }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isDeleting = false,
                        services = result.data.providerDetails?.services.orEmpty(),
                        errorMessage = null,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isDeleting = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun deletePending() {
        val service = _uiState.value.pendingDelete ?: return
        if (service.isPrimary) {
            _uiState.update {
                it.copy(pendingDelete = null, errorMessage = "El servicio principal no se puede eliminar. Puede sumar otros servicios sin perderlo.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, pendingDelete = null, errorMessage = null) }
            when (val result = accountUseCases.deleteService(service.id)) {
                is AppResult.Success -> {
                    load()
                    emit(ServicesEffect.ShowMessage("Servicio eliminado."))
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isDeleting = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun emit(effect: ServicesEffect) = viewModelScope.launch { _effects.send(effect) }
}

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountUseCases: AccountUseCases,
    private val getCategories: GetCategoriesUseCase,
    private val getRubros: GetRubrosByCategoryUseCase,
    private val getSubRubros: GetSubRubrosByRubroUseCase,
    private val searchBarrios: SearchBarriosUseCase,
    private val geocodeLocalAddress: GeocodeLocalAddressUseCase,
    private val availabilityUseCases: AvailabilityUseCases,
) : ViewModel() {
    private val editingServiceId: Long? = savedStateHandle.get<Long>("serviceId")?.takeIf { it > 0L }
    private val _uiState = MutableStateFlow(AddServiceUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<AddServiceEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    private var barrioSearchJob: Job? = null

    init {
        if (editingServiceId != null) loadForEdit() else loadCategories()
    }

    fun onEvent(event: AddServiceEvent) {
        when (event) {
            AddServiceEvent.Retry -> if (editingServiceId != null) loadForEdit() else loadCategories()
            AddServiceEvent.Back -> emit(AddServiceEffect.NavigateBack)
            AddServiceEvent.Next -> next()
            AddServiceEvent.Previous -> previous()
            is AddServiceEvent.CategorySelected -> if (!_uiState.value.isEditing) selectCategory(event.id)
            is AddServiceEvent.RubroSelected -> if (!_uiState.value.isEditing) selectRubro(event.id)
            is AddServiceEvent.SubRubroToggled -> update {
                it.copy(selectedSubRubroIds = it.selectedSubRubroIds.toggled(event.id, event.checked), errorMessage = null)
            }
            is AddServiceEvent.ClientHomeChanged -> update { it.copy(servesAtClientHome = event.checked, errorMessage = null) }
            is AddServiceEvent.LocalChanged -> update { it.copy(servesAtLocal = event.checked, errorMessage = null) }
            is AddServiceEvent.RequiresTurnsChanged -> update { it.copy(requiresTurns = event.checked) }
            is AddServiceEvent.LocalNameChanged -> update { it.copy(localName = event.value) }
            is AddServiceEvent.LocalBarrioChanged -> searchBarrio(event.value)
            AddServiceEvent.LocalBarrioFocused ->
                searchBarrio(_uiState.value.localBarrioQuery, preserveSelection = true)
            is AddServiceEvent.LocalBarrioSelected -> {
                barrioSearchJob?.cancel()
                update {
                    it.copy(
                        localBarrioId = event.option.id,
                        localBarrioQuery = event.option.label,
                        localBarrioSuggestions = emptyList(),
                        isSearchingBarrios = false,
                        localLocation = FormosaAddressGeocoding.invalidate(),
                    )
                }
            }
            AddServiceEvent.LocalBarrioSuggestionsDismissed -> {
                barrioSearchJob?.cancel()
                update { it.copy(localBarrioSuggestions = emptyList(), isSearchingBarrios = false) }
            }
            is AddServiceEvent.LocalStreetChanged -> update {
                it.copy(localStreet = event.value, localLocation = FormosaAddressGeocoding.invalidate())
            }
            is AddServiceEvent.LocalNumberChanged -> update {
                it.copy(localNumber = event.value, localLocation = FormosaAddressGeocoding.invalidate())
            }
            is AddServiceEvent.LocalMapLocationSelected -> update {
                it.copy(
                    localLocation = FormosaAddressGeocoding.pinMoved(it.localLocation, event.latitude, event.longitude),
                    errorMessage = null,
                )
            }
            AddServiceEvent.LocateOnMap -> update {
                val next = FormosaAddressGeocoding.openMap(it.localLocation)
                it.copy(localLocation = next)
            }
            AddServiceEvent.ConfirmPin -> update {
                val confirmed = FormosaAddressGeocoding.confirmPin(it.localLocation)
                it.copy(localLocation = confirmed)
            }
            is AddServiceEvent.WorkDayToggled -> update {
                it.withWeek(it.weeklyAvailability.withToggledDay(event.day, event.checked))
            }
            is AddServiceEvent.TimeFromChanged -> changeSlot(event.index) { copy(from = event.value) }
            is AddServiceEvent.TimeToChanged -> changeSlot(event.index) { copy(to = event.value) }
            AddServiceEvent.AddTimeSlot -> update {
                it.copy(timeSlots = it.timeSlots + RegisterTradeTimeSlotDraft("09:00", "18:00"))
            }
            is AddServiceEvent.RemoveTimeSlot -> update {
                it.copy(timeSlots = it.timeSlots.filterIndexed { index, _ -> index != event.index }.ifEmpty {
                    listOf(RegisterTradeTimeSlotDraft("09:00", "18:00"))
                })
            }
            is AddServiceEvent.IncludeSunday -> update {
                it.withWeek(it.weeklyAvailability.copy(includeSunday = event.included))
            }
            is AddServiceEvent.WeekDayToggled -> update {
                it.withWeek(it.weeklyAvailability.withToggledDay(event.day, event.enabled))
            }
            is AddServiceEvent.WeekRangeFrom -> update {
                it.withWeek(it.weeklyAvailability.changeRange(event.day, event.index, from = event.value))
            }
            is AddServiceEvent.WeekRangeTo -> update {
                it.withWeek(it.weeklyAvailability.changeRange(event.day, event.index, to = event.value))
            }
            is AddServiceEvent.AddWeekRange -> update { it.withWeek(it.weeklyAvailability.addRange(event.day)) }
            is AddServiceEvent.RemoveWeekRange -> update {
                it.withWeek(it.weeklyAvailability.removeRange(event.day, event.index))
            }
            is AddServiceEvent.PaymentToggled -> update {
                it.copy(paymentMethods = it.paymentMethods.toggled(event.method, event.checked), errorMessage = null)
            }
            AddServiceEvent.Save -> save()
        }
    }

    private fun loadCategories() = viewModelScope.launch {
        update { it.copy(isLoading = true, loadFailed = false, errorMessage = null) }
        when (val result = getCategories()) {
            is AppResult.Success -> update {
                it.copy(isLoading = false, loadFailed = false, categories = result.data)
            }
            is AppResult.Error -> update {
                it.copy(
                    isLoading = false,
                    loadFailed = it.categories.isEmpty(),
                    errorMessage = result.error.userMessage(),
                )
            }
        }
    }

    private fun selectCategory(id: Long) = viewModelScope.launch {
        update { it.copy(selectedCategoryId = id, selectedRubroId = null, selectedSubRubroIds = emptySet(), rubros = emptyList(), subRubros = emptyList(), isLoading = true) }
        when (val result = getRubros(id)) {
            is AppResult.Success -> update { it.copy(isLoading = false, rubros = result.data) }
            is AppResult.Error -> update { it.copy(isLoading = false, errorMessage = result.error.userMessage()) }
        }
    }

    private fun selectRubro(id: Long) = viewModelScope.launch {
        update { it.copy(selectedRubroId = id, selectedSubRubroIds = emptySet(), subRubros = emptyList(), isLoading = true) }
        when (val result = getSubRubros(id)) {
            is AppResult.Success -> update { it.copy(isLoading = false, subRubros = result.data) }
            is AppResult.Error -> update { it.copy(isLoading = false, errorMessage = result.error.userMessage()) }
        }
    }

    private fun next() {
        val state = _uiState.value
        val error = when (state.step) {
            AddServiceStep.TRADE -> if (state.selectedRubroId == null || state.selectedSubRubroIds.isEmpty()) {
                "Seleccione al menos una especialidad."
            } else {
                null
            }
            AddServiceStep.MODALITY -> when {
                !state.servesAtClientHome && !state.servesAtLocal -> "Elija al menos una modalidad de atención."
                state.servesAtLocal && (state.localBarrioId == null || state.localStreet.isBlank()) -> "Complete el barrio y la calle de su local."
                state.servesAtLocal && !state.localLocation.canPersist -> {
                    if (state.localLocation.phase == AddressLocationPhase.Idle) {
                        resolveLocalFromAddress()
                    }
                    state.localLocation.message ?: "Confirme la ubicación de su local."
                }
                else -> null
            }
            AddServiceStep.SCHEDULE -> when {
                state.weeklyAvailability.activeDays.isEmpty() -> "Elija al menos un día de trabajo."
                else -> TimeRangeValidation.validateWeek(state.weeklyAvailability)
            }
            AddServiceStep.PAYMENT -> if (state.paymentMethods.isEmpty()) "Elija al menos un medio de cobro." else null
            AddServiceStep.REVIEW -> null
        }
        if (error != null) update { it.copy(errorMessage = error) }
        else update { it.copy(step = AddServiceStep.entries[(it.step.ordinal + 1).coerceAtMost(AddServiceStep.REVIEW.ordinal)], errorMessage = null) }
    }

    private fun previous() {
        val state = _uiState.value
        if (state.step == AddServiceStep.TRADE) emit(AddServiceEffect.NavigateBack)
        else update { it.copy(step = AddServiceStep.entries[it.step.ordinal - 1], errorMessage = null) }
    }

    private fun save() {
        val state = _uiState.value
        if (state.step != AddServiceStep.REVIEW) return
        val rubroId = state.selectedRubroId ?: return
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            val local = if (state.servesAtLocal) ProviderLocalDomicilioRequest(
                barrioId = state.localBarrioId ?: return@launch update { it.copy(isSaving = false, errorMessage = "Seleccione el barrio de su local.") },
                calle = state.localStreet.trim().ifBlank { null },
                numero = state.localNumber.trim().ifBlank { null },
                nombre = state.localName.trim().ifBlank { null },
                latitud = GeoCoordinates.roundForApi(state.localLocation.latitude),
                longitud = GeoCoordinates.roundForApi(state.localLocation.longitude),
            ) else null
            val request = ProviderRubroFullRequest(
                rubroId = rubroId,
                subRubroIds = state.selectedSubRubroIds.toList(),
                servesAtClientHome = state.servesAtClientHome,
                servesAtLocal = state.servesAtLocal,
                workDays = state.weeklyAvailability.activeDays,
                timeSlots = state.timeSlots,
                weeklyAvailability = state.weeklyAvailability,
                requiresTurns = state.requiresTurns,
                domicilioLocal = local,
                paymentMethods = state.paymentMethods.toList(),
            )
            val result = if (state.isEditing && state.editingServiceId != null) {
                accountUseCases.replaceService(
                    existingId = state.editingServiceId,
                    isPrimary = state.isPrimaryService,
                    request = request,
                )
            } else {
                accountUseCases.addService(request)
            }
            when (result) {
                is AppResult.Success -> {
                    syncWeeklyAvailability(rubroId, state.weeklyAvailability)
                    emit(
                        AddServiceEffect.Saved(
                            if (state.isEditing) "Servicio actualizado." else "Servicio agregado a su perfil.",
                        ),
                    )
                }
                is AppResult.Error -> update { it.copy(isSaving = false, errorMessage = result.error.userMessage()) }
            }
        }
    }

    private fun searchBarrio(query: String, preserveSelection: Boolean = false) {
        update {
            it.copy(
                localBarrioQuery = query,
                localBarrioId = if (preserveSelection) it.localBarrioId else null,
                isSearchingBarrios = true,
                errorMessage = null,
            )
        }
        barrioSearchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = barrioSearchJob,
            query = query,
            searchBarrios = searchBarrios,
            onResult = { options ->
                update { it.copy(isSearchingBarrios = false, localBarrioSuggestions = options) }
            },
        )
    }

    private fun resolveLocalFromAddress() {
        val state = _uiState.value
        if (state.isGeocoding) return
        viewModelScope.launch {
            update {
                it.copy(
                    isGeocoding = true,
                    localLocation = it.localLocation.copy(phase = AddressLocationPhase.Geocoding),
                    errorMessage = null,
                )
            }
            when (
                val result = geocodeLocalAddress(
                    LocalAddressQuery(
                        street = state.localStreet,
                        number = state.localNumber,
                        neighborhood = state.localBarrioQuery,
                    ),
                )
            ) {
                is AppResult.Error -> update {
                    it.copy(
                        isGeocoding = false,
                        localLocation = AddressLocationSnapshot(
                            phase = AddressLocationPhase.NeedsMap,
                            message = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                        ),
                    )
                }
                is AppResult.Success -> {
                    val snapshot = FormosaAddressGeocoding.snapshotFor(result.data)
                    update {
                        it.copy(
                            isGeocoding = false,
                            localLocation = snapshot,
                        )
                    }
                }
            }
        }
    }

    private fun changeSlot(index: Int, block: RegisterTradeTimeSlotDraft.() -> RegisterTradeTimeSlotDraft) = update { state ->
        state.copy(timeSlots = state.timeSlots.mapIndexed { itemIndex, slot -> if (itemIndex == index) slot.block() else slot }, errorMessage = null)
    }

    private fun loadForEdit() {
        val serviceId = editingServiceId ?: return
        viewModelScope.launch {
            update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    errorMessage = null,
                    isEditing = true,
                    editingServiceId = serviceId,
                )
            }
            when (val profileResult = accountUseCases.getProfile()) {
                is AppResult.Error -> update {
                    it.copy(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = profileResult.error.userMessage(),
                    )
                }
                is AppResult.Success -> {
                    val details = profileResult.data.providerDetails
                    val service = details?.services?.firstOrNull { it.id == serviceId }
                    if (service == null) {
                        update {
                            it.copy(
                                isLoading = false,
                                loadFailed = true,
                                errorMessage = "No encontramos ese servicio.",
                            )
                        }
                        return@launch
                    }
                    applyService(service, details.paymentMethods.toSet(), details.commercialLocal)
                }
            }
        }
    }

    private suspend fun applyService(
        service: ManagedProviderService,
        payments: Set<String>,
        commercialLocal: ProviderCommercialLocal? = null,
    ) {
        val subRubros = when (val result = getSubRubros(service.rubroId)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> {
                update {
                    it.copy(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = result.error.userMessage(),
                    )
                }
                return
            }
        }
        val fallbackWeek = service.workSchedule.toWeeklyAvailability()
        val week = service.subServices.firstOrNull()?.id?.let { subRubroId ->
            when (val result = availabilityUseCases.getWeekly(subRubroId)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> fallbackWeek
            }
        } ?: fallbackWeek
        val workDays = week.activeDays.toSet()
        val timeSlots = week.days
            .flatMap { day -> day.ranges.map { RegisterTradeTimeSlotDraft(it.start, it.end) } }
            .distinct()
            .ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) }
        val servesAtLocal = service.subServices.any { sub -> sub.servesAtLocal }
        val (street, numberFromStreet) = DomicilioFieldValidation.splitStreetAndNumber(commercialLocal?.street)
        val number = commercialLocal?.number?.trim().orEmpty().ifBlank { numberFromStreet }
        update {
            it.copy(
                isLoading = false,
                loadFailed = false,
                isEditing = true,
                editingServiceId = service.id,
                isPrimaryService = service.isPrimary,
                lockedRubroName = service.rubroName,
                selectedRubroId = service.rubroId,
                selectedSubRubroIds = service.subServices.map { sub -> sub.catalogSubRubroId }.toSet(),
                subRubros = subRubros,
                servesAtClientHome = service.subServices.any { sub -> sub.servesAtClientHome },
                servesAtLocal = servesAtLocal,
                requiresTurns = service.subServices.any { sub -> sub.requiresTurns },
                localName = commercialLocal?.name.orEmpty(),
                localBarrioId = if (servesAtLocal) commercialLocal?.barrioId else it.localBarrioId,
                localBarrioQuery = if (servesAtLocal) commercialLocal?.barrioName.orEmpty() else it.localBarrioQuery,
                localStreet = if (servesAtLocal) street else it.localStreet,
                localNumber = if (servesAtLocal) number else it.localNumber,
                workDays = workDays.ifEmpty { it.workDays },
                timeSlots = timeSlots,
                weeklyAvailability = week,
                paymentMethods = payments.ifEmpty { it.paymentMethods },
                errorMessage = null,
            )
        }
    }

    private suspend fun syncWeeklyAvailability(rubroId: Long, week: WeeklyAvailability) {
        val details = when (val result = accountUseCases.getProfile()) {
            is AppResult.Success -> result.data.providerDetails
            is AppResult.Error -> return
        } ?: return
        val service = details.services
            .filter { it.rubroId == rubroId }
            .maxByOrNull { it.id }
            ?: return
        service.subServices.forEach { sub ->
            availabilityUseCases.replaceWeekly(sub.id, week)
        }
    }

    private fun update(block: (AddServiceUiState) -> AddServiceUiState) = _uiState.update(block)
    private fun emit(effect: AddServiceEffect) = viewModelScope.launch { _effects.send(effect) }
}

private fun <T> Set<T>.toggled(value: T, checked: Boolean) = if (checked) this + value else this - value
private fun AddServiceUiState.withWeek(week: WeeklyAvailability) = copy(
    weeklyAvailability = week,
    workDays = week.activeDays.toSet(),
    timeSlots = week.days
        .flatMap { day -> day.ranges.map { RegisterTradeTimeSlotDraft(it.start, it.end) } }
        .distinct()
        .ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) },
    errorMessage = null,
)
