package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.AddServiceDraft
import com.cocido.oficios.domain.model.AddServiceDraftCodec
import com.cocido.oficios.domain.model.AvailabilityPut
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ManagedSpecialtySchedule
import com.cocido.oficios.domain.model.ProviderCommercialLocal
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.addRange
import com.cocido.oficios.domain.model.availabilityPutsForEdit
import com.cocido.oficios.domain.model.changeRange
import com.cocido.oficios.domain.model.copyMondayToWeekdays
import com.cocido.oficios.domain.model.insertRange
import com.cocido.oficios.domain.model.removeRange
import com.cocido.oficios.domain.model.toManagedSchedules
import com.cocido.oficios.domain.model.toWeeklyAvailability
import com.cocido.oficios.domain.model.withToggledDay
import com.cocido.oficios.domain.usecase.AvailabilityUseCases
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.DebouncedBarrioSearcher
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.launchDebouncedForwardGeocode
import com.cocido.oficios.domain.validation.DomicilioFieldValidation
import com.cocido.oficios.domain.validation.ScheduleValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val availabilityUseCases: AvailabilityUseCases,
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
                    val services = enrichSchedules(details.services)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isDeleting = false,
                            services = services,
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
            ServicesEvent.Refresh -> load(userInitiated = true)
            ServicesEvent.Back -> emit(ServicesEffect.NavigateBack)
            ServicesEvent.AddService -> emit(ServicesEffect.NavigateToAdd)
            is ServicesEvent.EditService -> emit(ServicesEffect.NavigateToEdit(event.service.id))
            is ServicesEvent.EditAvailability -> emit(ServicesEffect.NavigateToAvailability)
            is ServicesEvent.RequestDelete -> _uiState.update { it.copy(pendingDelete = event.service) }
            ServicesEvent.DismissDelete -> _uiState.update { it.copy(pendingDelete = null) }
            ServicesEvent.ConfirmDelete -> deletePending()
        }
    }

    /** Stale-while-revalidate: con lista visible, recarga sin spinner de pantalla completa. */
    fun load(userInitiated: Boolean = false) {
        viewModelScope.launch {
            val keepContent = _uiState.value.services.isNotEmpty()
            _uiState.update {
                it.copy(isLoading = !keepContent, isRefreshing = userInitiated, errorMessage = null)
            }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> {
                    val services = enrichSchedules(result.data.providerDetails?.services.orEmpty())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isDeleting = false,
                            services = services,
                            errorMessage = null,
                        )
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isDeleting = false,
                        errorMessage = result.error.userMessage(),
                    )
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

    private suspend fun enrichSchedules(
        services: List<ManagedProviderService>,
    ): List<ManagedProviderService> = services.map { service ->
        val specialtySchedules = service.subServices.mapNotNull { sub ->
            if (sub.id <= 0L) return@mapNotNull null
            when (val result = availabilityUseCases.getWeekly(sub.id)) {
                is AppResult.Success -> ManagedSpecialtySchedule(
                    name = sub.name,
                    workSchedule = result.data.toManagedSchedules(),
                )
                is AppResult.Error -> null
            }
        }
        if (specialtySchedules.isEmpty()) {
            service
        } else {
            service.copy(
                workSchedule = specialtySchedules.first().workSchedule,
                specialtySchedules = specialtySchedules,
            )
        }
    }

    private fun emit(effect: ServicesEffect) = viewModelScope.launch { _effects.send(effect) }
}

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
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
    private var barrioSearcher: DebouncedBarrioSearcher? = null

    private fun ensureBarrioSearcher(): DebouncedBarrioSearcher =
        barrioSearcher ?: DebouncedBarrioSearcher(
            scope = viewModelScope,
            searchBarrios = searchBarrios,
            onLoading = { update { it.copy(isSearchingBarrios = true) } },
            onResult = { options -> update { it.copy(isSearchingBarrios = false, localBarrioSuggestions = options) } },
        ).also { barrioSearcher = it }
    private var geocodeJob: Job? = null
    private var originalAvailabilityBySubId: Map<Long, WeeklyAvailability> = emptyMap()

    /** Borrador previo a una muerte de proceso; se aplica una sola vez, tras cargar catálogo/servicio. */
    private var pendingRestore: AddServiceDraft? =
        AddServiceDraftCodec.decode(savedStateHandle.get<String>(DRAFT_KEY))
    private var draftPersistJob: Job? = null

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
                if (it.isEditing) {
                    it
                } else {
                    it.copy(selectedSubRubroIds = it.selectedSubRubroIds.toggled(event.id, event.checked), errorMessage = null)
                }
            }
            is AddServiceEvent.ClientHomeChanged -> update { it.copy(servesAtClientHome = event.checked, errorMessage = null) }
            is AddServiceEvent.LocalChanged -> update { it.copy(servesAtLocal = event.checked, errorMessage = null) }
            is AddServiceEvent.RequiresTurnsChanged -> update {
                if (!event.checked && !it.canDisableTurns) {
                    it
                } else {
                    it.copy(requiresTurns = event.checked)
                }
            }
            is AddServiceEvent.PlazoCancelacionHorasChanged -> update {
                it.copy(plazoCancelacionHorasInput = event.value.filter { char -> char.isDigit() }, errorMessage = null)
            }
            is AddServiceEvent.EditorSpecialtySelected -> selectEditorSpecialty(event.id)
            is AddServiceEvent.LocalNameChanged -> update { it.copy(localName = event.value) }
            is AddServiceEvent.LocalBarrioChanged -> searchBarrio(event.value)
            AddServiceEvent.LocalBarrioFocused ->
                searchBarrio(_uiState.value.localBarrioQuery, preserveSelection = true)
            is AddServiceEvent.LocalBarrioSelected -> {
                barrioSearcher?.clearPending()
                update {
                    it.copy(
                        localBarrioId = event.option.id,
                        localBarrioQuery = event.option.label,
                        localBarrioSuggestions = emptyList(),
                        isSearchingBarrios = false,
                    )
                }
                scheduleLiveGeocode()
            }
            AddServiceEvent.LocalBarrioSuggestionsDismissed -> {
                barrioSearcher?.clearPending()
                update { it.copy(localBarrioSuggestions = emptyList(), isSearchingBarrios = false) }
            }
            is AddServiceEvent.LocalStreetChanged -> {
                update { it.copy(localStreet = event.value) }
                scheduleLiveGeocode()
            }
            is AddServiceEvent.LocalNumberChanged -> {
                update { it.copy(localNumber = event.value) }
                scheduleLiveGeocode()
            }
            is AddServiceEvent.LocalMapLocationSelected -> {
                cancelLiveGeocode()
                update {
                    it.copy(
                        localLocation = FormosaAddressGeocoding.pinMoved(it.localLocation, event.latitude, event.longitude),
                        errorMessage = null,
                    )
                }
            }
            AddServiceEvent.LocateOnMap -> {
                cancelLiveGeocode()
                update {
                    val next = FormosaAddressGeocoding.openMap(it.localLocation)
                    it.copy(localLocation = next)
                }
            }
            AddServiceEvent.ConfirmPin -> {
                cancelLiveGeocode()
                update {
                    val confirmed = FormosaAddressGeocoding.confirmPin(it.localLocation)
                    it.copy(localLocation = confirmed)
                }
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
            is AddServiceEvent.InsertWeekRange -> update {
                it.withWeek(
                    it.weeklyAvailability.insertRange(event.day, event.index, TimeRange(event.from, event.to)),
                )
            }
            AddServiceEvent.CopyMondayToWeekdays -> update {
                it.withWeek(it.weeklyAvailability.copyMondayToWeekdays())
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
            is AppResult.Success -> {
                update { it.copy(isLoading = false, loadFailed = false, categories = result.data) }
                pendingRestore?.let { restoreAddDraft(it) }
                startPersistingDraft()
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

    /**
     * Alta tras muerte de proceso: recarga rubros/especialidades del rubro guardado y vuelve a
     * poner lo que el usuario había cargado. Si el catálogo no responde, conserva el resto del
     * formulario y lo devuelve al paso de oficio para que elija de nuevo.
     */
    private suspend fun restoreAddDraft(draft: AddServiceDraft) {
        val rubros = draft.categoryId?.let { (getRubros(it) as? AppResult.Success)?.data }
        val subRubros = if (rubros != null) draft.rubroId?.let { (getSubRubros(it) as? AppResult.Success)?.data } else null
        update { state ->
            val restored = state.applyDraft(draft, restoreTrade = true)
            when {
                rubros == null -> restored.copy(
                    step = AddServiceStep.TRADE,
                    selectedCategoryId = null,
                    selectedRubroId = null,
                    selectedSubRubroIds = emptySet(),
                )
                subRubros == null -> restored.copy(
                    step = AddServiceStep.TRADE,
                    rubros = rubros,
                    selectedRubroId = null,
                    selectedSubRubroIds = emptySet(),
                )
                else -> restored.copy(rubros = rubros, subRubros = subRubros)
            }
        }
        pendingRestore = null
    }

    private fun startPersistingDraft() {
        if (draftPersistJob != null) return
        draftPersistJob = viewModelScope.launch {
            _uiState
                .map { it.toDraft() }
                .distinctUntilChanged()
                .collect { draft -> savedStateHandle[DRAFT_KEY] = AddServiceDraftCodec.encode(draft) }
        }
    }

    private fun clearPersistedDraft() {
        draftPersistJob?.cancel()
        draftPersistJob = null
        savedStateHandle.remove<String>(DRAFT_KEY)
    }

    /** Re-tocar la misma categoría no borra rubro ni especialidades. */
    private fun selectCategory(id: Long) {
        val state = _uiState.value
        if (id == state.selectedCategoryId) {
            if (state.rubros.isEmpty() && !state.isLoading) loadRubros(id)
            return
        }
        update {
            it.rememberSubRubroSelection().copy(
                selectedCategoryId = id,
                selectedRubroId = null,
                selectedSubRubroIds = emptySet(),
                rubros = emptyList(),
                subRubros = emptyList(),
                isLoading = true,
            )
        }
        loadRubros(id)
    }

    /**
     * Re-tocar el rubro ya elegido es un no-op (antes vaciaba las especialidades).
     * Cambiar a otro rubro arranca con su propia selección y guarda la del anterior, que se
     * restaura si el usuario vuelve a elegirlo.
     */
    private fun selectRubro(id: Long) {
        val state = _uiState.value
        if (id == state.selectedRubroId) {
            if (state.subRubros.isEmpty() && !state.isLoading) loadSubRubros(id)
            return
        }
        update {
            val remembered = it.rememberSubRubroSelection()
            remembered.copy(
                selectedRubroId = id,
                selectedSubRubroIds = remembered.subRubroSelectionsByRubro[id].orEmpty(),
                subRubros = emptyList(),
                isLoading = true,
                errorMessage = null,
            )
        }
        loadSubRubros(id)
    }

    private fun loadRubros(categoryId: Long) = viewModelScope.launch {
        update { it.copy(isLoading = true) }
        val result = getRubros(categoryId)
        update {
            if (it.selectedCategoryId != categoryId) return@update it
            when (result) {
                is AppResult.Success -> it.copy(isLoading = false, rubros = result.data)
                is AppResult.Error -> it.copy(isLoading = false, errorMessage = result.error.userMessage())
            }
        }
    }

    private fun loadSubRubros(rubroId: Long) = viewModelScope.launch {
        update { it.copy(isLoading = true) }
        val result = getSubRubros(rubroId)
        update {
            // Respuesta tardía de un rubro que ya no está elegido: se descarta.
            if (it.selectedRubroId != rubroId) return@update it
            when (result) {
                is AppResult.Success -> it.copy(isLoading = false, subRubros = result.data)
                is AppResult.Error -> it.copy(isLoading = false, errorMessage = result.error.userMessage())
            }
        }
    }

    private fun next() {
        val state = _uiState.value
        state.plazoCancelacionHorasError?.let { errorMessage ->
            update { it.copy(errorMessage = errorMessage) }
            return
        }
        val error = when (state.step) {
            AddServiceStep.TRADE -> if (state.selectedRubroId == null || state.selectedSubRubroIds.isEmpty()) {
                "Seleccione al menos una especialidad."
            } else {
                null
            }
            AddServiceStep.ATTENTION -> attentionValidationError(state)
            AddServiceStep.CONFIRM -> if (state.paymentMethods.isEmpty()) {
                "Elija al menos un medio de cobro."
            } else {
                null
            }
        }
        if (error != null) {
            update { it.copy(errorMessage = error) }
            return
        }
        when (state.step) {
            AddServiceStep.TRADE -> update { it.copy(step = AddServiceStep.ATTENTION, errorMessage = null) }
            AddServiceStep.ATTENTION -> update { it.copy(step = AddServiceStep.CONFIRM, errorMessage = null) }
            AddServiceStep.CONFIRM -> Unit
        }
    }

    private fun attentionValidationError(state: AddServiceUiState): String? = when {
        !state.servesAtClientHome && !state.servesAtLocal ->
            "Elija al menos una modalidad de atención."
        state.servesAtLocal && (state.localBarrioId == null || state.localStreet.isBlank()) ->
            "Complete el barrio y la calle de su local."
        state.servesAtLocal && !state.localLocation.canPersist -> {
            if (state.localLocation.phase == AddressLocationPhase.Idle) {
                resolveLocalFromAddress()
            }
            state.localLocation.message ?: "Confirme la ubicación de su local."
        }
        else -> ScheduleValidator.validate(state.weeklyAvailability)
    }

    private fun previous() {
        val state = _uiState.value
        when (state.step) {
            AddServiceStep.TRADE -> emit(AddServiceEffect.NavigateBack)
            AddServiceStep.ATTENTION -> {
                if (state.isEditing) emit(AddServiceEffect.NavigateBack)
                else update { it.copy(step = AddServiceStep.TRADE, errorMessage = null) }
            }
            AddServiceStep.CONFIRM -> update { it.copy(step = AddServiceStep.ATTENTION, errorMessage = null) }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.step != AddServiceStep.CONFIRM) return
        attentionValidationError(state)?.let { message ->
            update { it.copy(errorMessage = message) }
            return
        }
        if (state.paymentMethods.isEmpty()) {
            update { it.copy(errorMessage = "Elija al menos un medio de cobro.") }
            return
        }
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
                plazoCancelacionHoras = state.plazoCancelacionHoras,
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
                    syncWeeklyAvailability(rubroId, state.weeklyAvailability, forceSync = state.isEditing)
                    clearPersistedDraft()
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
                errorMessage = null,
            )
        }
        if (!preserveSelection) scheduleLiveGeocode()
        ensureBarrioSearcher().submit(query)
    }

    private fun scheduleLiveGeocode() {
        val state = _uiState.value
        geocodeJob = viewModelScope.launchDebouncedForwardGeocode(
            previous = geocodeJob,
            query = LocalAddressQuery(
                street = state.localStreet,
                number = state.localNumber,
                neighborhood = state.localBarrioQuery,
            ),
            geocode = geocodeLocalAddress,
            onResult = { result ->
                val outcome = (result as? AppResult.Success)?.data ?: return@launchDebouncedForwardGeocode
                update {
                    it.copy(localLocation = FormosaAddressGeocoding.applyLiveOutcome(outcome, it.localLocation))
                }
            },
        )
    }

    private fun cancelLiveGeocode() {
        geocodeJob?.cancel()
        geocodeJob = null
    }

    private fun resolveLocalFromAddress() {
        cancelLiveGeocode()
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
                    if (_uiState.value.loadFailed) return@launch
                    // Tras muerte de proceso: los datos del servidor son la base (editBaseline)
                    // y encima se reponen los cambios sin guardar del usuario.
                    pendingRestore?.let { draft ->
                        update { it.applyDraft(draft, restoreTrade = false) }
                        pendingRestore = null
                    }
                    startPersistingDraft()
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
        val original = buildMap {
            for (sub in service.subServices) {
                if (sub.id <= 0L) continue
                when (val result = availabilityUseCases.getWeekly(sub.id)) {
                    is AppResult.Success -> put(sub.id, result.data)
                    is AppResult.Error -> Unit
                }
            }
        }
        originalAvailabilityBySubId = original
        val editorSourceSubId = service.subServices.firstOrNull { it.id > 0L }?.id
        val week = editorSourceSubId?.let { original[it] } ?: fallbackWeek
        val editorSpecialties = service.subServices
            .filter { sub -> sub.id > 0L }
            .map { sub -> CatalogOption(sub.id, sub.name) }
        val canDisableTurns = service.subServices.none { sub -> sub.requiresTurns }
        val workDays = week.activeDays.toSet()
        val timeSlots = week.days
            .flatMap { day -> day.ranges.map { RegisterTradeTimeSlotDraft(it.start, it.end) } }
            .distinct()
            .ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) }
        val servesAtLocal = service.subServices.any { sub -> sub.servesAtLocal }
        val (street, numberFromStreet) = DomicilioFieldValidation.splitStreetAndNumber(commercialLocal?.street)
        val number = commercialLocal?.number?.trim().orEmpty().ifBlank { numberFromStreet }
        update {
            val next = it.copy(
                isLoading = false,
                loadFailed = false,
                isEditing = true,
                // Edición: salta el paso de rubro (solo lectura) y entra en modalidad/horarios.
                step = AddServiceStep.ATTENTION,
                editingServiceId = service.id,
                isPrimaryService = service.isPrimary,
                lockedRubroName = service.rubroName,
                selectedRubroId = service.rubroId,
                selectedSubRubroIds = service.subServices.map { sub -> sub.catalogSubRubroId }.toSet(),
                subRubros = subRubros,
                servesAtClientHome = service.subServices.any { sub -> sub.servesAtClientHome },
                servesAtLocal = servesAtLocal,
                requiresTurns = service.subServices.any { sub -> sub.requiresTurns },
                plazoCancelacionHorasInput = service.plazoCancelacionHoras?.toString().orEmpty(),
                localName = commercialLocal?.name.orEmpty(),
                localBarrioId = if (servesAtLocal) commercialLocal?.barrioId else it.localBarrioId,
                localBarrioQuery = if (servesAtLocal) commercialLocal?.barrioName.orEmpty() else it.localBarrioQuery,
                localStreet = if (servesAtLocal) street else it.localStreet,
                localNumber = if (servesAtLocal) number else it.localNumber,
                workDays = workDays.ifEmpty { it.workDays },
                timeSlots = timeSlots,
                weeklyAvailability = week,
                paymentMethods = payments.ifEmpty { it.paymentMethods },
                editorSpecialties = editorSpecialties,
                editorSourceSubId = editorSourceSubId,
                availabilityDrafts = original,
                canDisableTurns = canDisableTurns,
                errorMessage = null,
            )
            next.copy(editBaseline = next.formSnapshot())
        }
    }

    private suspend fun syncWeeklyAvailability(rubroId: Long, week: WeeklyAvailability, forceSync: Boolean = false) {
        val details = when (val result = accountUseCases.getProfile()) {
            is AppResult.Success -> result.data.providerDetails
            is AppResult.Error -> return
        } ?: return
        val service = details.services
            .filter { it.rubroId == rubroId }
            .maxByOrNull { it.id }
            ?: return
        val state = _uiState.value
        val puts = if (forceSync) {
            service.subServices.filter { it.id > 0L }.map { sub ->
                val week = if (sub.id == state.editorSourceSubId) {
                    week
                } else {
                    state.availabilityDrafts[sub.id] ?: originalAvailabilityBySubId[sub.id] ?: week
                }
                AvailabilityPut(sub.id, week)
            }
        } else {
            availabilityPutsForEdit(
                persistedSubIds = service.subServices.map { sub -> sub.id },
                originalBySubId = originalAvailabilityBySubId,
                editorSourceSubId = state.editorSourceSubId,
                wizardWeek = week,
                draftBySubId = state.availabilityDrafts,
            )
        }
        for (put in puts) {
            availabilityUseCases.replaceWeekly(put.subRubroId, put.week)
        }
    }

    private fun selectEditorSpecialty(id: Long) {
        val state = _uiState.value
        if (!state.isEditing || id == state.editorSourceSubId) return
        val drafts = state.availabilityDrafts.toMutableMap()
        state.editorSourceSubId?.let { drafts[it] = state.weeklyAvailability }
        val nextWeek = drafts[id] ?: originalAvailabilityBySubId[id] ?: WeeklyAvailability.default()
        update {
            it.withWeek(nextWeek).copy(
                editorSourceSubId = id,
                availabilityDrafts = drafts,
            )
        }
    }

    private fun update(block: (AddServiceUiState) -> AddServiceUiState) = _uiState.update(block)
    private fun emit(effect: AddServiceEffect) = viewModelScope.launch { _effects.send(effect) }
}

internal const val DRAFT_KEY = "add_service_draft"

private fun AddServiceUiState.rememberSubRubroSelection(): AddServiceUiState {
    val rubroId = selectedRubroId ?: return this
    if (selectedSubRubroIds.isEmpty()) return this
    return copy(subRubroSelectionsByRubro = subRubroSelectionsByRubro + (rubroId to selectedSubRubroIds))
}

internal fun AddServiceUiState.toDraft() = AddServiceDraft(
    step = step.name,
    categoryId = selectedCategoryId,
    rubroId = selectedRubroId,
    subRubroIds = selectedSubRubroIds.toList(),
    subRubroIdsByRubro = subRubroSelectionsByRubro.mapValues { (_, ids) -> ids.toList() },
    servesAtClientHome = servesAtClientHome,
    servesAtLocal = servesAtLocal,
    requiresTurns = requiresTurns,
    plazoCancelacionHorasInput = plazoCancelacionHorasInput,
    localName = localName,
    localBarrioQuery = localBarrioQuery,
    localBarrioId = localBarrioId,
    localStreet = localStreet,
    localNumber = localNumber,
    localLatitude = localLocation.latitude,
    localLongitude = localLocation.longitude,
    localPinConfirmed = localLocation.canPersist,
    weeklyAvailability = weeklyAvailability,
    paymentMethods = paymentMethods.toList(),
    editorSourceSubId = editorSourceSubId,
    availabilityDrafts = availabilityDrafts,
)

/**
 * Repone un borrador. En edición el rubro y las especialidades son de solo lectura, así que
 * [restoreTrade] = false y se toman del servidor.
 */
private fun AddServiceUiState.applyDraft(draft: AddServiceDraft, restoreTrade: Boolean): AddServiceUiState {
    val draftStep = AddServiceStep.entries.firstOrNull { it.name == draft.step } ?: step
    val restoredStep = if (isEditing && draftStep == AddServiceStep.TRADE) AddServiceStep.ATTENTION else draftStep
    val lat = draft.localLatitude
    val lng = draft.localLongitude
    val location = if (draft.localPinConfirmed && lat != null && lng != null) {
        AddressLocationSnapshot(latitude = lat, longitude = lng, phase = AddressLocationPhase.Confirmed, confirmed = true)
    } else {
        // Sin pin confirmado se vuelve a geocodificar al validar el paso.
        AddressLocationSnapshot()
    }
    val withTrade = if (restoreTrade) {
        copy(
            selectedCategoryId = draft.categoryId,
            selectedRubroId = draft.rubroId,
            selectedSubRubroIds = draft.subRubroIds.toSet(),
            subRubroSelectionsByRubro = draft.subRubroIdsByRubro.mapValues { (_, ids) -> ids.toSet() },
        )
    } else {
        this
    }
    val editorSubId = draft.editorSourceSubId?.takeIf { id -> editorSpecialties.any { it.id == id } }
    return withTrade.withWeek(draft.weeklyAvailability).copy(
        step = restoredStep,
        servesAtClientHome = draft.servesAtClientHome,
        servesAtLocal = draft.servesAtLocal,
        requiresTurns = if (!draft.requiresTurns && !canDisableTurns) true else draft.requiresTurns,
        plazoCancelacionHorasInput = draft.plazoCancelacionHorasInput,
        localName = draft.localName,
        localBarrioQuery = draft.localBarrioQuery,
        localBarrioId = draft.localBarrioId,
        localStreet = draft.localStreet,
        localNumber = draft.localNumber,
        localLocation = location,
        paymentMethods = draft.paymentMethods.toSet(),
        editorSourceSubId = if (isEditing) editorSubId ?: editorSourceSubId else editorSourceSubId,
        availabilityDrafts = if (isEditing) draft.availabilityDrafts else availabilityDrafts,
    )
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
