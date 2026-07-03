package com.cocido.oficios.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
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

@HiltViewModel
class RegisterTradeViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val getSubRubrosByRubroUseCase: GetSubRubrosByRubroUseCase,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
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
            is RegisterTradeEvent.NeighborhoodSelected -> update {
                it.copy(
                    neighborhoodQuery = event.option.label,
                    selectedBarrioId = event.option.id,
                    neighborhoodOptions = emptyList(),
                )
            }
            RegisterTradeEvent.NeighborhoodSuggestionsDismissed -> update {
                it.copy(neighborhoodOptions = emptyList())
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
            RegisterTradeEvent.Continue -> continueToProviderRegister()
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
                            catalogError = result.error.message,
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
                            catalogError = result.error.message,
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
                            catalogError = result.error.message,
                        ).validated()
                    }
                }
            }
        }
    }

    private fun continueToProviderRegister() {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: return
        val rubroId = state.selectedTradeId ?: return

        val subRubroIds = state.selectedSpecializations
            .filter { it != OTHER_SPECIALIZATION_ID }
            .mapNotNull { it.toLongOrNull() }

        viewModelScope.launch {
            registerTradeDraftRepository.save(
                RegisterTradeDraft(
                    categoryId = categoryId,
                    categoryName = state.selectedCategory,
                    rubroId = rubroId,
                    rubroName = state.selectedTrade,
                    subRubroIds = subRubroIds,
                    otherSpecialization = state.otherSpecialization.takeIf {
                        OTHER_SPECIALIZATION_ID in state.selectedSpecializations && it.isNotBlank()
                    },
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
            _effects.emit(RegisterTradeEffect.NavigateToProviderRegister)
        }
    }

    private fun onNeighborhoodQueryChanged(query: String) {
        update {
            it.copy(
                neighborhoodQuery = query,
                selectedBarrioId = null,
            )
        }
        neighborhoodSearchJob?.cancel()
        if (query.length < 2) {
            update { it.copy(neighborhoodOptions = emptyList()) }
            return
        }
        neighborhoodSearchJob = viewModelScope.launch {
            delay(300)
            when (val result = searchBarriosUseCase(query)) {
                is AppResult.Success -> update { it.copy(neighborhoodOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun update(block: (RegisterTradeUiState) -> RegisterTradeUiState) {
        _uiState.update { block(it).validated() }
    }

    private fun RegisterTradeUiState.validated(): RegisterTradeUiState {
        val hasSpecialization = selectedSpecializations.isNotEmpty() &&
            (OTHER_SPECIALIZATION_ID !in selectedSpecializations || otherSpecialization.isNotBlank())
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

    private companion object {
        const val OTHER_SPECIALIZATION_ID = RegisterTradeUiState.OTHER_SPECIALIZATION_ID
    }
}
