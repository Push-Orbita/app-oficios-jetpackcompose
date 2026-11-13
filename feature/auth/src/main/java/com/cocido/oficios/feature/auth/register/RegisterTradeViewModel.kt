package com.cocido.oficios.feature.auth.register

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.addRange
import com.cocido.oficios.domain.model.changeRange
import com.cocido.oficios.domain.model.copyMondayToWeekdays
import com.cocido.oficios.domain.model.insertRange
import com.cocido.oficios.domain.model.removeRange
import com.cocido.oficios.domain.model.toFlatTimeSlots
import com.cocido.oficios.domain.model.withToggledDay
import com.cocido.oficios.domain.validation.ScheduleValidator
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.DebouncedBarrioSearcher
import com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase
import com.cocido.oficios.feature.auth.R
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

@HiltViewModel
class RegisterTradeViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val getSubRubrosByRubroUseCase: GetSubRubrosByRubroUseCase,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val submitRegisterTradeDraftUseCase: SubmitRegisterTradeDraftUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterTradeUiState())
    val uiState: StateFlow<RegisterTradeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<RegisterTradeEffect>()
    val effects: SharedFlow<RegisterTradeEffect> = _effects.asSharedFlow()

    private var barrioSearcher: DebouncedBarrioSearcher? = null

    private fun ensureBarrioSearcher(): DebouncedBarrioSearcher =
        barrioSearcher ?: DebouncedBarrioSearcher(
            scope = viewModelScope,
            searchBarrios = searchBarriosUseCase,
            onLoading = { update { it.copy(isSearchingBarrios = true, barrioSearchError = null) } },
            onResult = { options ->
                update { it.copy(isSearchingBarrios = false, neighborhoodOptions = options, barrioSearchError = null) }
            },
            onError = { error ->
                update {
                    it.copy(
                        isSearchingBarrios = false,
                        neighborhoodOptions = emptyList(),
                        barrioSearchError = error.userMessage(),
                    )
                }
            },
        ).also { barrioSearcher = it }

    private var rubrosJob: Job? = null
    private var subRubrosJob: Job? = null

    init {
        _uiState.update {
            it.withWeek(WeeklyAvailability.default()).copy(
                experienceRanges = RegisterTradeUiState.defaultExperienceRanges,
                timeOptions = RegisterTradeUiState.defaultTimeOptions(),
                workDayOptions = RegisterTradeUiState.defaultWorkDays.map { day -> day.first },
            ).validated()
        }
        loadCategories()
    }

    fun onEvent(event: RegisterTradeEvent) {
        when (event) {
            is RegisterTradeEvent.CategoryGridSelected -> onCategoryChanged(event.option.label)
            is RegisterTradeEvent.CategoryChanged -> onCategoryChanged(event.value)
            is RegisterTradeEvent.TradeChanged -> onTradeChanged(event.value)
            is RegisterTradeEvent.SpecializationToggled -> update {
                val updated = it.selectedSpecializations.toMutableSet()
                if (event.checked) updated.add(event.id) else updated.remove(event.id)
                it.copy(selectedSpecializations = updated)
            }
            is RegisterTradeEvent.OtherSpecializationChanged -> update {
                it.copy(otherSpecialization = event.value)
            }
            is RegisterTradeEvent.ExperienceRangeChanged -> update {
                it.copy(selectedExperienceRange = event.value)
            }
            is RegisterTradeEvent.NeighborhoodQueryChanged -> onNeighborhoodQueryChanged(event.value)
            RegisterTradeEvent.NeighborhoodFocused ->
                onNeighborhoodQueryChanged(_uiState.value.neighborhoodQuery, preserveSelection = true)
            is RegisterTradeEvent.NeighborhoodSelected -> {
                barrioSearcher?.clearPending()
                update {
                    it.copy(
                        neighborhoodQuery = event.option.label,
                        selectedBarrioId = event.option.id,
                        neighborhoodOptions = emptyList(),
                        isSearchingBarrios = false,
                        barrioSearchError = null,
                    )
                }
            }
            RegisterTradeEvent.RetryNeighborhoodSearch -> ensureBarrioSearcher().retry()
            RegisterTradeEvent.NeighborhoodSuggestionsDismissed -> {
                barrioSearcher?.clearPending()
                update { it.copy(neighborhoodOptions = emptyList(), isSearchingBarrios = false, barrioSearchError = null) }
            }
            is RegisterTradeEvent.TimeSlotFromChanged -> update {
                val day = it.weeklySchedule.activeDays.firstOrNull() ?: return@update it
                it.withWeek(it.weeklySchedule.changeRange(day, event.index, from = event.value))
            }
            is RegisterTradeEvent.TimeSlotToChanged -> update {
                val day = it.weeklySchedule.activeDays.firstOrNull() ?: return@update it
                it.withWeek(it.weeklySchedule.changeRange(day, event.index, to = event.value))
            }
            RegisterTradeEvent.AddTimeSlot -> update {
                val day = it.weeklySchedule.activeDays.firstOrNull() ?: return@update it
                it.withWeek(it.weeklySchedule.addRange(day))
            }
            is RegisterTradeEvent.WorkDayToggled -> update {
                it.withWeek(it.weeklySchedule.withToggledDay(event.day, event.checked))
            }
            is RegisterTradeEvent.IncludeSunday -> update {
                it.withWeek(it.weeklySchedule.copy(includeSunday = event.included))
            }
            is RegisterTradeEvent.WeekDayToggled -> update {
                it.withWeek(it.weeklySchedule.withToggledDay(event.day, event.enabled))
            }
            is RegisterTradeEvent.WeekRangeFrom -> update {
                it.withWeek(it.weeklySchedule.changeRange(event.day, event.index, from = event.value))
            }
            is RegisterTradeEvent.WeekRangeTo -> update {
                it.withWeek(it.weeklySchedule.changeRange(event.day, event.index, to = event.value))
            }
            is RegisterTradeEvent.AddWeekRange -> update { it.withWeek(it.weeklySchedule.addRange(event.day)) }
            is RegisterTradeEvent.RemoveWeekRange -> update {
                it.withWeek(it.weeklySchedule.removeRange(event.day, event.index))
            }
            is RegisterTradeEvent.InsertWeekRange -> update {
                it.withWeek(
                    it.weeklySchedule.insertRange(event.day, event.index, TimeRange(event.from, event.to)),
                )
            }
            RegisterTradeEvent.CopyMondayToWeekdays -> update {
                it.withWeek(it.weeklySchedule.copyMondayToWeekdays())
            }
            is RegisterTradeEvent.ServesAtClientHomeChanged -> update {
                it.copy(servesAtClientHome = event.value)
            }
            is RegisterTradeEvent.ServesAtLocalChanged -> update {
                it.copy(servesAtLocal = event.value)
            }
            is RegisterTradeEvent.RequiresTurnsChanged -> update {
                it.copy(
                    requiresTurns = event.value,
                    turnDurationMinutes = if (event.value && it.turnDurationMinutes.isBlank()) "60" else it.turnDurationMinutes,
                )
            }
            is RegisterTradeEvent.TurnDurationChanged -> update {
                val filtered = event.value.filter { it.isDigit() }
                it.copy(turnDurationMinutes = filtered)
            }
            is RegisterTradeEvent.TurnIntervalChanged -> update {
                val filtered = event.value.filter { it.isDigit() }
                it.copy(turnIntervalMinutes = filtered)
            }
            is RegisterTradeEvent.PaymentMethodToggled -> update {
                val updated = it.selectedPaymentMethods.toMutableSet()
                if (event.checked) updated.add(event.method) else updated.remove(event.method)
                it.copy(selectedPaymentMethods = updated)
            }
            is RegisterTradeEvent.LocalCalleChanged -> update { it.copy(localCalle = event.value) }
            is RegisterTradeEvent.LocalNumeroChanged -> update { it.copy(localNumero = event.value) }
            RegisterTradeEvent.Continue -> continueToOnboarding()
            RegisterTradeEvent.RetryLoadCatalog -> loadCategories()
            RegisterTradeEvent.ClearStepValidationError -> update { it.copy(stepValidationError = null) }
        }
    }

    /**
     * Valida el paso del wizard. Si falta algo, deja un mensaje visible y no avanza.
     * @return true si el paso está completo.
     */
    fun attemptContinueSection(section: RegisterTradeFormSection): Boolean {
        val state = _uiState.value
        val can = when (section) {
            RegisterTradeFormSection.OFICIO, RegisterTradeFormSection.FULL -> state.canContinueOficio
            RegisterTradeFormSection.ZONA -> state.canContinueZona
            RegisterTradeFormSection.AGENDA -> state.canContinueAgenda
            RegisterTradeFormSection.COBRO -> state.canContinueCobro
            RegisterTradeFormSection.IDENTIDAD -> state.canContinueIdentidad
        }
        if (can) {
            _uiState.update { it.copy(stepValidationError = null) }
            return true
        }
        _uiState.update {
            it.copy(stepValidationError = stepIncompleteMessage(section))
        }
        return false
    }

    @StringRes
    private fun stepIncompleteMessage(section: RegisterTradeFormSection): Int = when (section) {
        RegisterTradeFormSection.OFICIO, RegisterTradeFormSection.FULL ->
            R.string.auth_wizard_step_oficio_incomplete
        RegisterTradeFormSection.ZONA -> R.string.auth_wizard_step_zona_incomplete
        RegisterTradeFormSection.AGENDA -> R.string.auth_wizard_step_agenda_incomplete
        RegisterTradeFormSection.COBRO -> R.string.auth_wizard_step_cobro_incomplete
        RegisterTradeFormSection.IDENTIDAD -> R.string.auth_wizard_step_identidad_incomplete
    }

    /**
     * Re-elegir la categoría ya seleccionada es un no-op: no debe borrar rubro ni especialidades.
     * Cambiar de categoría sí limpia el rubro, pero las especialidades elegidas quedan
     * recordadas por rubro ([RegisterTradeUiState.specializationsByTrade]).
     */
    private fun onCategoryChanged(label: String) {
        val state = _uiState.value
        val category = state.categoryOptions.find { it.label == label } ?: return
        if (category.id == state.selectedCategoryId) {
            if (state.tradeOptions.isEmpty() && !state.isLoadingTrades) loadRubros(category.id)
            return
        }
        update {
            it.rememberCurrentSpecializations().copy(
                selectedCategoryId = category.id,
                selectedTradeId = null,
                tradeOptions = emptyList(),
                specializationOptions = emptyList(),
                selectedSpecializations = emptySet(),
                otherSpecialization = "",
                catalogError = null,
            )
        }
        loadRubros(category.id)
    }

    /**
     * Re-tocar el mismo rubro (grilla o dropdown) es un no-op: conserva las especialidades.
     * Cambiar a otro rubro arranca su propia selección, pero guarda la del rubro anterior
     * para restaurarla si el usuario vuelve a elegirlo (no se pierde en silencio).
     */
    private fun onTradeChanged(label: String) {
        val state = _uiState.value
        val rubro = state.tradeOptions.find { it.label == label } ?: return
        if (rubro.id == state.selectedTradeId) {
            if (state.specializationOptions.isEmpty() && !state.isLoadingSpecializations) loadSubRubros(rubro.id)
            return
        }
        update {
            val remembered = it.rememberCurrentSpecializations()
            val restored = remembered.specializationsByTrade[rubro.id]
            remembered.copy(
                selectedTradeId = rubro.id,
                specializationOptions = emptyList(),
                selectedSpecializations = restored?.selected.orEmpty(),
                otherSpecialization = restored?.other.orEmpty(),
                catalogError = null,
            )
        }
        loadSubRubros(rubro.id)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCatalog = true, catalogError = null) }
            when (val result = getCategoriesUseCase()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCatalog = false,
                            categoryOptions = result.data,
                            catalogError = null,
                        ).validated()
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCatalog = false,
                            catalogError = result.error.userMessage(),
                        ).validated()
                    }
                }
            }
        }
    }

    private fun loadRubros(categoryId: Long) {
        rubrosJob?.cancel()
        rubrosJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrades = true, catalogError = null) }
            when (val result = getRubrosByCategoryUseCase(categoryId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        if (it.selectedCategoryId != categoryId) return@update it
                        it.copy(
                            isLoadingTrades = false,
                            tradeOptions = result.data,
                            catalogError = null,
                        ).validated()
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingTrades = false,
                            catalogError = result.error.userMessage(),
                        ).validated()
                    }
                }
            }
        }
    }

    private fun loadSubRubros(rubroId: Long) {
        subRubrosJob?.cancel()
        subRubrosJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSpecializations = true, catalogError = null) }
            when (val result = getSubRubrosByRubroUseCase(rubroId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        // Respuesta tardía de un rubro que ya no está seleccionado: se descarta.
                        if (it.selectedTradeId != rubroId) return@update it
                        it.copy(
                            isLoadingSpecializations = false,
                            specializationOptions = result.data,
                            catalogError = null,
                        ).validated()
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingSpecializations = false,
                            catalogError = result.error.userMessage(),
                        ).validated()
                    }
                }
            }
        }
    }

    private fun continueToOnboarding() {
        val state = _uiState.value
        if (!state.canContinue) {
            _uiState.update {
                it.copy(stepValidationError = R.string.auth_wizard_step_oficio_incomplete)
            }
            return
        }
        val draft = snapshotDraft() ?: return
        viewModelScope.launch {
            registerTradeDraftRepository.save(draft)
            // Intento inmediato: casi siempre falla porque el DNI todavía no está aprobado
            // en este punto (recién se sube en onboarding); el checklist reintenta esto
            // mismo una vez que la identidad quede verificada (ProviderOnboardingViewModel).
            _uiState.update { it.copy(isLoading = true) }
            submitRegisterTradeDraftUseCase(requireDraft = true)
            _uiState.update { it.copy(isLoading = false) }
            _effects.emit(RegisterTradeEffect.NavigateToOnboarding)
        }
    }

    fun applyDraft(draft: RegisterTradeDraft) {
        update {
            it.copy(
                selectedCategoryId = draft.categoryId,
                selectedTradeId = draft.rubroId,
                selectedSpecializations = draft.subRubroIds.map { id -> id.toString() }.toSet(),
                otherSpecialization = draft.otherSpecialization.orEmpty(),
                selectedExperienceRange = draft.experienceRange,
                neighborhoodQuery = draft.neighborhoodQuery,
                selectedBarrioId = draft.barrioId,
                servesAtClientHome = draft.servesAtClientHome,
                servesAtLocal = draft.servesAtLocal,
                requiresTurns = draft.requiresTurns,
                turnDurationMinutes = draft.turnDurationMinutes?.toString().orEmpty(),
                turnIntervalMinutes = draft.turnIntervalMinutes?.toString().orEmpty(),
                selectedPaymentMethods = draft.paymentMethods.toSet(),
                localCalle = draft.localCalle.orEmpty(),
                localNumero = draft.localNumero.orEmpty(),
            ).withWeek(draft.weeklySchedule ?: WeeklyAvailability.default())
        }
        loadRubros(draft.categoryId)
        loadSubRubros(draft.rubroId)
    }

    fun snapshotDraft(): RegisterTradeDraft? {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: return null
        val rubroId = state.selectedTradeId ?: return null
        val durationMinutes = state.turnDurationMinutes.toIntOrNull()?.coerceIn(15, 480)
        val intervalMinutes = state.turnIntervalMinutes.toIntOrNull()?.coerceIn(0, 240)
        return RegisterTradeDraft(
            categoryId = categoryId,
            categoryName = state.selectedCategory,
            rubroId = rubroId,
            rubroName = state.selectedTrade,
            subRubroIds = state.selectedSpecializations.mapNotNull { it.toLongOrNull() },
            otherSpecialization = state.otherSpecialization.trim().ifBlank { null },
            experienceRange = state.selectedExperienceRange,
            neighborhoodQuery = state.neighborhoodQuery,
            barrioId = state.selectedBarrioId,
            workDays = state.weeklySchedule.activeDays,
            servesAtClientHome = state.servesAtClientHome,
            servesAtLocal = state.servesAtLocal,
            requiresTurns = state.requiresTurns,
            turnDurationMinutes = durationMinutes,
            turnIntervalMinutes = intervalMinutes,
            paymentMethods = state.selectedPaymentMethods.toList(),
            timeSlots = state.weeklySchedule.toFlatTimeSlots(),
            weeklySchedule = state.weeklySchedule,
            localCalle = state.localCalle.trim().ifBlank { null },
            localNumero = state.localNumero.trim().ifBlank { null },
        )
    }

    private fun onNeighborhoodQueryChanged(query: String, preserveSelection: Boolean = false) {
        update {
            it.copy(
                neighborhoodQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                barrioSearchError = null,
            )
        }
        ensureBarrioSearcher().submit(query)
    }

    private fun update(block: (RegisterTradeUiState) -> RegisterTradeUiState) {
        _uiState.update { block(it).validated().copy(stepValidationError = null) }
    }

    private fun RegisterTradeUiState.validated(): RegisterTradeUiState {
        val hasSpecialization = selectedSpecializations.isNotEmpty()
        val turnFieldsValid = !requiresTurns || turnDurationMinutes.toIntOrNull()?.let { it in 15..480 } == true
        val canContinue = selectedCategoryId != null &&
            selectedTradeId != null &&
            hasSpecialization &&
            selectedExperienceRange.isNotBlank() &&
            selectedBarrioId != null &&
            neighborhoodQuery.isNotBlank() &&
            selectedPaymentMethods.isNotEmpty() &&
            (servesAtClientHome || servesAtLocal) &&
            ScheduleValidator.validate(weeklySchedule) == null &&
            turnFieldsValid
        return copy(canContinue = canContinue)
    }

}

private fun RegisterTradeUiState.rememberCurrentSpecializations(): RegisterTradeUiState {
    val tradeId = selectedTradeId ?: return this
    if (selectedSpecializations.isEmpty() && otherSpecialization.isBlank()) return this
    return copy(
        specializationsByTrade = specializationsByTrade +
            (tradeId to TradeSpecializationSelection(selectedSpecializations, otherSpecialization)),
    )
}

private fun RegisterTradeUiState.withWeek(week: WeeklyAvailability) = copy(
    weeklySchedule = week,
    selectedWorkDays = week.activeDays.toSet(),
    timeSlots = week.toFlatTimeSlots().map { RegisterTradeTimeSlot(it.from, it.to) },
)