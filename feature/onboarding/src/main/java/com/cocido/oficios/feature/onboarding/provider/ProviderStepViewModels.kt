package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.mapper.toRubroFullRequest
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.RetryPendingTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitProviderRubroFullUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderProfessionalInfoUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
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

data class ProviderDniUiState(
    val documento: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface ProviderStepEffect {
    data object Saved : ProviderStepEffect
    data object Finalized : ProviderStepEffect
}

@HiltViewModel
class ProviderDniViewModel @Inject constructor(
    private val updateProviderDniNumberUseCase: UpdateProviderDniNumberUseCase,
    private val uploadProviderDniImagesUseCase: UploadProviderDniImagesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderDniUiState())
    val uiState: StateFlow<ProviderDniUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    fun onDocumentoChanged(value: String) = _uiState.update { it.copy(documento = value, error = null) }
    fun onFrontImage(path: String?) = _uiState.update { it.copy(frontImagePath = path, error = null) }
    fun onBackImage(path: String?) = _uiState.update { it.copy(backImagePath = path, error = null) }

    fun submitNumber() {
        val documento = _uiState.value.documento.trim()
        if (documento.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = updateProviderDniNumberUseCase(documento)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }

    fun submitImages() {
        val state = _uiState.value
        val front = state.frontImagePath ?: return
        val back = state.backImagePath ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = uploadProviderDniImagesUseCase(front, back)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }
}

data class ProviderProfessionalInfoUiState(
    val descripcion: String = "",
    val experiencia: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProviderProfessionalInfoViewModel @Inject constructor(
    private val updateProviderProfessionalInfoUseCase: UpdateProviderProfessionalInfoUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderProfessionalInfoUiState())
    val uiState: StateFlow<ProviderProfessionalInfoUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            registerTradeDraftRepository.get()?.let { draft ->
                _uiState.update {
                    it.copy(
                        descripcion = draft.otherSpecialization ?: draft.rubroName,
                        experiencia = draft.experienceRange,
                    )
                }
            }
        }
    }

    fun onDescripcionChanged(value: String) = _uiState.update { it.copy(descripcion = value, error = null) }
    fun onExperienciaChanged(value: String) = _uiState.update { it.copy(experiencia = value, error = null) }

    fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (
                val result = updateProviderProfessionalInfoUseCase(
                    ProviderProfessionalInfoRequest(
                        descripcion = state.descripcion.trim(),
                        experiencia = state.experiencia.trim(),
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }
}

data class ProviderRubrosUiState(
    val categoryOptions: List<CatalogOption> = emptyList(),
    val tradeOptions: List<CatalogOption> = emptyList(),
    val specializationOptions: List<CatalogOption> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedTradeId: Long? = null,
    val selectedSubRubroIds: Set<Long> = emptySet(),
    val selectedWorkDays: Set<String> = setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"),
    val timeFrom: String = "09:00",
    val timeTo: String = "18:00",
    val servesAtClientHome: Boolean = true,
    val servesAtLocal: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = selectedTradeId != null && selectedSubRubroIds.isNotEmpty()
}

@HiltViewModel
class ProviderRubrosViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val getSubRubrosByRubroUseCase: GetSubRubrosByRubroUseCase,
    private val submitProviderRubroFullUseCase: SubmitProviderRubroFullUseCase,
    private val retryPendingTradeDraftUseCase: RetryPendingTradeDraftUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderRubrosUiState())
    val uiState: StateFlow<ProviderRubrosUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is AppResult.Success -> _uiState.update { it.copy(categoryOptions = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(error = result.error.userMessage()) }
            }
            registerTradeDraftRepository.get()?.let { draft ->
                _uiState.update {
                    it.copy(
                        selectedCategoryId = draft.categoryId,
                        selectedTradeId = draft.rubroId,
                        selectedSubRubroIds = draft.subRubroIds.toSet(),
                        selectedWorkDays = draft.workDays.toSet().ifEmpty { it.selectedWorkDays },
                        timeFrom = draft.timeSlots.firstOrNull()?.from ?: it.timeFrom,
                        timeTo = draft.timeSlots.firstOrNull()?.to ?: it.timeTo,
                        servesAtClientHome = draft.servesAtClientHome,
                        servesAtLocal = draft.servesAtLocal,
                    )
                }
                loadRubros(draft.categoryId)
                loadSubRubros(draft.rubroId)
            }
        }
    }

    fun onCategorySelected(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id, selectedTradeId = null, tradeOptions = emptyList()) }
        loadRubros(id)
    }

    fun onTradeSelected(id: Long) {
        _uiState.update { it.copy(selectedTradeId = id, specializationOptions = emptyList()) }
        loadSubRubros(id)
    }

    fun onSubRubroToggled(id: Long, checked: Boolean) = _uiState.update {
        val updated = it.selectedSubRubroIds.toMutableSet()
        if (checked) updated.add(id) else updated.remove(id)
        it.copy(selectedSubRubroIds = updated)
    }

    private fun loadRubros(categoryId: Long) {
        viewModelScope.launch {
            when (val result = getRubrosByCategoryUseCase(categoryId)) {
                is AppResult.Success -> _uiState.update { it.copy(tradeOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun loadSubRubros(rubroId: Long) {
        viewModelScope.launch {
            when (val result = getSubRubrosByRubroUseCase(rubroId)) {
                is AppResult.Success -> _uiState.update { it.copy(specializationOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val rubroId = state.selectedTradeId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val draftResult = retryPendingTradeDraftUseCase()
            if (draftResult is AppResult.Success) {
                _uiState.update { it.copy(isLoading = false) }
                _effects.emit(ProviderStepEffect.Saved)
                return@launch
            }
            val request = ProviderRubroFullRequest(
                rubroId = rubroId,
                subRubroIds = state.selectedSubRubroIds.toList(),
                servesAtClientHome = state.servesAtClientHome,
                servesAtLocal = state.servesAtLocal,
                workDays = state.selectedWorkDays.toList(),
                timeSlots = listOf(RegisterTradeTimeSlotDraft(state.timeFrom, state.timeTo)),
            )
            when (val result = submitProviderRubroFullUseCase(request)) {
                is AppResult.Success -> {
                    registerTradeDraftRepository.clear()
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }
}

data class ProviderLocalUiState(
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val nombre: String = "Local comercial",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProviderLocalViewModel @Inject constructor(
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val submitProviderRubroFullUseCase: SubmitProviderRubroFullUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderLocalUiState())
    val uiState: StateFlow<ProviderLocalUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()
    private var searchJob: Job? = null

    fun onBarrioQueryChanged(query: String) {
        _uiState.update { it.copy(barrioQuery = query, selectedBarrioId = null) }
        searchJob?.cancel()
        if (query.length < 2) return
        searchJob = viewModelScope.launch {
            delay(300)
            when (val result = searchBarriosUseCase(query)) {
                is AppResult.Success -> _uiState.update { it.copy(barrioOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun onBarrioSelected(option: CatalogOption) {
        _uiState.update {
            it.copy(barrioQuery = option.label, selectedBarrioId = option.id, barrioOptions = emptyList())
        }
    }

    fun onCalleChanged(value: String) = _uiState.update { it.copy(calle = value, error = null) }

    fun submit() {
        val state = _uiState.value
        val barrioId = state.selectedBarrioId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val draft = registerTradeDraftRepository.get()
            if (draft == null || draft.subRubroIds.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Completá primero rubro y subrubros") }
                return@launch
            }
            val updatedDraft = draft.copy(
                barrioId = barrioId,
                neighborhoodQuery = state.calle.trim(),
                servesAtLocal = true,
            )
            val request = updatedDraft.toRubroFullRequest().copy(
                domicilioLocal = ProviderLocalDomicilioRequest(
                    barrioId = barrioId,
                    calle = state.calle.trim(),
                    nombre = state.nombre,
                ),
            )
            when (val result = submitProviderRubroFullUseCase(request)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }
}

@HiltViewModel
class ProviderFinalizeViewModel @Inject constructor(
    private val finalizeProviderProfileUseCase: FinalizeProviderProfileUseCase,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    fun finalize() {
        viewModelScope.launch {
            _isLoading.update { true }
            when (val result = finalizeProviderProfileUseCase()) {
                is AppResult.Success -> {
                    _isLoading.update { false }
                    _effects.emit(ProviderStepEffect.Finalized)
                }
                is AppResult.Error -> _isLoading.update { false }
            }
        }
    }
}
