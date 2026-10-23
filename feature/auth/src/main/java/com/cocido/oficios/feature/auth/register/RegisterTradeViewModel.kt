package com.cocido.oficios.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase
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

    private var neighborhoodSearchJob: Job? = null

    init {
        _uiState.update {
            it.copy(
                experienceRanges = RegisterTradeUiState.defaultExperienceRanges,
                timeOptions = RegisterTradeUiState.defaultTimeOptions(),
                workDayOptions = RegisterTradeUiState.defaultWorkDays.map { day -> day.first },
                selectedWorkDays = RegisterTradeUiState.defaultSelectedWorkDays,
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
                neighborhoodSearchJob?.cancel()
                update {
                    it.copy(
                        neighborhoodQuery = event.option.label,
                        selectedBarrioId = event.option.id,
                        neighborhoodOptions = emptyList(),
                        isSearchingBarrios = false,
                    )
                }
            }
            RegisterTradeEvent.NeighborhoodSuggestionsDismissed -> {
                neighborhoodSearchJob?.cancel()
                update { it.copy(neighborhoodOptions = emptyList(), isSearchingBarrios = false) }
            }
            is RegisterTradeEvent.TimeSlotFromChanged -> update {
                it.copy(timeSlots = it.timeSlots.updateAt(event.index) { slot -> slot.copy(from = event.value) })
            }
            is RegisterTradeEvent.TimeSlotToChanged -> update {
                it.copy(timeSlots = it.timeSlots.updateAt(event.index) { slot -> slot.copy(to = event.value) })
            }
            RegisterTradeEvent.AddTimeSlot -> update {
                it.copy(timeSlots = it.timeSlots + RegisterTradeTimeSlot())
            }
            is RegisterTradeEvent.WorkDayToggled -> update {
                val updated = it.selectedWorkDays.toMutableSet()
                if (event.checked) updated.add(event.day) else updated.remove(event.day)
                it.copy(selectedWorkDays = updated)
            }
            is RegisterTradeEvent.ServesAtClientHomeChanged -> update {
                it.copy(servesAtClientHome = event.value)
            }
            is RegisterTradeEvent.ServesAtLocalChanged -> update {
                it.copy(servesAtLocal = event.value)
            }
            is RegisterTradeEvent.PaymentMethodToggled -> update {
                val updated = it.selectedPaymentMethods.toMutableSet()
                if (event.checked) updated.add(event.method) else updated.remove(event.method)
                it.copy(selectedPaymentMethods = updated)
            }
            RegisterTradeEvent.Continue -> continueToOnboarding()
            RegisterTradeEvent.RetryLoadCatalog -> loadCategories()
        }
    }

    private fun onCategoryChanged(label: String) {
        val category = _uiState.value.categoryOptions.find { it.label == label } ?: return
        update {
            it.copy(
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

    private fun onTradeChanged(label: String) {
        val rubro = _uiState.value.tradeOptions.find { it.label == label } ?: return
        update {
            it.copy(
                selectedTradeId = rubro.id,
                specializationOptions = emptyList(),
                selectedSpecializations = emptySet(),
                otherSpecialization = "",
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrades = true, catalogError = null) }
            when (val result = getRubrosByCategoryUseCase(categoryId)) {
                is AppResult.Success -> {
                    _uiState.update {
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSpecializations = true, catalogError = null) }
            when (val result = getSubRubrosByRubroUseCase(rubroId)) {
                is AppResult.Success -> {
                    _uiState.update {
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
            _uiState.update { it.copy(catalogError = "Complete los datos del oficio antes de continuar") }
            return
        }
        val categoryId = state.selectedCategoryId ?: return
        val rubroId = state.selectedTradeId ?: return

        val subRubroIds = state.selectedSpecializations.mapNotNull { it.toLongOrNull() }

        viewModelScope.launch {
            registerTradeDraftRepository.save(
                RegisterTradeDraft(
                    categoryId = categoryId,
                    categoryName = state.selectedCategory,
                    rubroId = rubroId,
                    rubroName = state.selectedTrade,
                    subRubroIds = subRubroIds,
                    otherSpecialization = state.otherSpecialization.trim().ifBlank { null },
                    experienceRange = state.selectedExperienceRange,
                    neighborhoodQuery = state.neighborhoodQuery,
                    barrioId = state.selectedBarrioId,
                    workDays = state.selectedWorkDays.toList(),
                    servesAtClientHome = state.servesAtClientHome,
                    servesAtLocal = state.servesAtLocal,
                    paymentMethods = state.selectedPaymentMethods.toList(),
                    timeSlots = state.timeSlots
                        .filter { it.from.isNotBlank() && it.to.isNotBlank() }
                        .map { RegisterTradeTimeSlotDraft(from = it.from, to = it.to) },
                ),
            )
            // Intento inmediato: casi siempre falla porque el DNI todavía no está aprobado
            // en este punto (recién se sube en onboarding); el checklist reintenta esto
            // mismo una vez que la identidad quede verificada (ProviderOnboardingViewModel).
            _uiState.update { it.copy(isLoading = true) }
            submitRegisterTradeDraftUseCase(requireDraft = true)
            _uiState.update { it.copy(isLoading = false) }
            _effects.emit(RegisterTradeEffect.NavigateToOnboarding)
        }
    }

    private fun onNeighborhoodQueryChanged(query: String, preserveSelection: Boolean = false) {
        update {
            it.copy(
                neighborhoodQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                isSearchingBarrios = true,
            )
        }
        neighborhoodSearchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = neighborhoodSearchJob,
            query = query,
            searchBarrios = searchBarriosUseCase,
            onResult = { options ->
                update { it.copy(isSearchingBarrios = false, neighborhoodOptions = options) }
            },
        )
    }

    private fun update(block: (RegisterTradeUiState) -> RegisterTradeUiState) {
        _uiState.update { block(it).validated() }
    }

    private fun RegisterTradeUiState.validated(): RegisterTradeUiState {
        val hasSpecialization = selectedSpecializations.isNotEmpty()
        val canContinue = selectedCategoryId != null &&
            selectedTradeId != null &&
            hasSpecialization &&
            selectedExperienceRange.isNotBlank() &&
            selectedBarrioId != null &&
            neighborhoodQuery.isNotBlank() &&
            selectedPaymentMethods.isNotEmpty() &&
            selectedWorkDays.isNotEmpty() &&
            (servesAtClientHome || servesAtLocal) &&
            timeSlots.any { it.from.isNotBlank() && it.to.isNotBlank() }
        return copy(canContinue = canContinue)
    }

    private fun <T> List<T>.updateAt(index: Int, transform: (T) -> T): List<T> =
        mapIndexed { currentIndex, item ->
            if (currentIndex == index) transform(item) else item
        }
}
