package com.cocido.oficios.feature.home.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.usecase.search.SearchWorkersUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
import com.cocido.oficios.feature.home.common.persistTo
import com.cocido.oficios.feature.home.common.toWorkerSearchFiltersWithExtras
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val QUERY_DEBOUNCE_MS = 350L

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchWorkersUseCase: SearchWorkersUseCase,
    private val trackRecentSearchUseCase: TrackRecentSearchUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val getSubRubrosByRubroUseCase: GetSubRubrosByRubroUseCase,
    private val searchBarriosUseCase: SearchBarriosUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchResultsUiState>(SearchResultsUiState.Loading)
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    private val _filterSheetState = MutableStateFlow(FilterSheetUiState())
    val filterSheetState: StateFlow<FilterSheetUiState> = _filterSheetState.asStateFlow()

    private val _effects = Channel<SearchResultsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var activeFilters: WorkerSearchFilters = savedStateHandle.toWorkerSearchFiltersWithExtras()

    private val _searchQuery = MutableStateFlow(activeFilters.query.orEmpty())
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private var currentPage = 0
    private var endReached = false
    private var loadJob: Job? = null
    private var barrioSearchJob: Job? = null
    private var queryDebounceJob: Job? = null
    private var pendingTrackOnSuccess = true
    private var trackedKey: String? = null

    init {
        loadInitial()
    }

    fun onEvent(event: SearchResultsEvent) {
        when (event) {
            SearchResultsEvent.Retry -> refresh()
            SearchResultsEvent.Refresh -> pullToRefresh()
            SearchResultsEvent.Back -> emitEffect(SearchResultsEffect.NavigateBack)
            SearchResultsEvent.LoadNextPage -> loadNextPage()
            SearchResultsEvent.OpenFilters -> openFilters()
            SearchResultsEvent.DismissFilters -> _filterSheetState.update { it.copy(visible = false) }
            SearchResultsEvent.ApplyFilters -> applyFilters()
            SearchResultsEvent.ClearFilters -> clearFilters()
            is SearchResultsEvent.QueryChanged -> onQueryChanged(event.query)
            SearchResultsEvent.QuerySubmitted -> onQuerySubmitted()
            SearchResultsEvent.VoiceSearch -> emitEffect(SearchResultsEffect.ShowComingSoon)
            is SearchResultsEvent.WorkerViewProfile -> {
                emitEffect(SearchResultsEffect.NavigateToWorker(event.worker.id))
            }
            is SearchResultsEvent.WorkerContact -> {
                emitEffect(SearchResultsEffect.NavigateToHire(event.worker.id))
            }
            is SearchResultsEvent.FilterCategorySelected -> onFilterCategorySelected(event.category)
            is SearchResultsEvent.FilterRubroSelected -> onFilterRubroSelected(event.rubro)
            is SearchResultsEvent.FilterSubRubroSelected -> onFilterSubRubroSelected(event.subRubro)
            is SearchResultsEvent.FilterBarrioQueryChanged -> onBarrioQueryChanged(event.query)
            is SearchResultsEvent.FilterBarrioSelected -> onBarrioSelected(event.barrio)
            is SearchResultsEvent.FilterModalitySelected -> {
                _filterSheetState.update { it.copy(selectedModality = event.modality) }
            }
            is SearchResultsEvent.FilterDateChanged -> {
                _filterSheetState.update { it.copy(selectedDate = event.date) }
            }
            is SearchResultsEvent.RemoveFilterChip -> removeFilterChip(event.chip)
        }
    }

    private fun onQueryChanged(query: String) {
        _searchQuery.value = query
        activeFilters = activeFilters.copy(query = query.takeIf { it.isNotBlank() })
        activeFilters.persistTo(savedStateHandle)
        pendingTrackOnSuccess = false
        queryDebounceJob?.cancel()
        queryDebounceJob = viewModelScope.launch {
            delay(QUERY_DEBOUNCE_MS)
            refresh()
        }
    }

    private fun onQuerySubmitted() {
        queryDebounceJob?.cancel()
        val trimmed = _searchQuery.value.trim()
        _searchQuery.value = trimmed
        activeFilters = activeFilters.copy(query = trimmed.takeIf { it.isNotBlank() })
        activeFilters.persistTo(savedStateHandle)
        pendingTrackOnSuccess = true
        refresh()
    }

    private fun loadInitial() {
        currentPage = 0
        endReached = false
        loadPage(page = 1, append = false)
    }

    private fun refresh() {
        currentPage = 0
        endReached = false
        loadPage(page = 1, append = false)
    }

    private fun pullToRefresh() {
        currentPage = 0
        endReached = false
        loadPage(page = 1, append = false, refreshing = true)
    }

    private fun loadNextPage() {
        val state = _uiState.value
        if (state !is SearchResultsUiState.Success || state.isLoadingPage || endReached) return
        loadPage(page = currentPage + 1, append = true)
    }

    private fun loadPage(page: Int, append: Boolean, refreshing: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val previous = _uiState.value
            try {
            if (!append) {
                if (refreshing) {
                    _uiState.update {
                        when (previous) {
                            is SearchResultsUiState.Success -> previous.copy(isRefreshing = true)
                            is SearchResultsUiState.Empty -> previous.copy(isRefreshing = true)
                            is SearchResultsUiState.Error -> previous.copy(isRefreshing = true)
                            SearchResultsUiState.Loading -> SearchResultsUiState.Loading
                        }
                    }
                } else {
                    _uiState.update { SearchResultsUiState.Loading }
                }
            } else {
                val current = _uiState.value
                if (current is SearchResultsUiState.Success) {
                    _uiState.update { current.copy(isLoadingPage = true) }
                }
            }

            when (val result = searchWorkersUseCase(activeFilters, page)) {
                is AppResult.Success -> {
                    currentPage = result.data.pageNumber
                    endReached = !result.data.hasMore
                    val chips = buildActiveFilterChips(activeFilters)
                    if (append) {
                        val current = _uiState.value as? SearchResultsUiState.Success
                        val merged = (current?.workers.orEmpty()) + result.data.items
                        _uiState.update {
                            SearchResultsUiState.Success(
                                workers = merged,
                                activeFilters = activeFilters,
                                filterChips = chips,
                                isLoadingPage = false,
                                endReached = endReached,
                            )
                        }
                    } else {
                        if (result.data.items.isEmpty()) {
                            pendingTrackOnSuccess = false
                            _uiState.update {
                                SearchResultsUiState.Empty(activeFilters, chips)
                            }
                        } else {
                            maybeTrackSuccessfulSearch()
                            _uiState.update {
                                SearchResultsUiState.Success(
                                    workers = result.data.items,
                                    activeFilters = activeFilters,
                                    filterChips = chips,
                                    isLoadingPage = false,
                                    endReached = endReached,
                                )
                            }
                        }
                    }
                }
                is AppResult.Error -> {
                    val message = result.error.userMessage()
                    if (append) {
                        val current = _uiState.value
                        if (current is SearchResultsUiState.Success) {
                            _uiState.update { current.copy(isLoadingPage = false, isRefreshing = false) }
                        }
                        emitEffect(SearchResultsEffect.ShowMessage(message))
                    } else if (previous is SearchResultsUiState.Success || previous is SearchResultsUiState.Empty) {
                        _uiState.update {
                            when (previous) {
                                is SearchResultsUiState.Success -> previous.copy(isRefreshing = false)
                                is SearchResultsUiState.Empty -> previous.copy(isRefreshing = false)
                                else -> previous
                            }
                        }
                        if (refreshing) emitEffect(SearchResultsEffect.ShowMessage(message))
                    } else {
                        _uiState.update { SearchResultsUiState.Error(message) }
                        if (refreshing) emitEffect(SearchResultsEffect.ShowMessage(message))
                    }
                }
            }
            } finally {
                if (isActive) {
                    _uiState.update { state ->
                        when (state) {
                            is SearchResultsUiState.Success -> state.copy(isRefreshing = false, isLoadingPage = false)
                            is SearchResultsUiState.Empty -> state.copy(isRefreshing = false)
                            is SearchResultsUiState.Error -> state.copy(isRefreshing = false)
                            SearchResultsUiState.Loading -> state
                        }
                    }
                }
            }
        }
    }

    private fun openFilters() {
        viewModelScope.launch {
            val categories = when (val result = getCategoriesUseCase()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> emptyList()
            }
            val rubros = activeFilters.categoryId?.let { categoryId ->
                when (val result = getRubrosByCategoryUseCase(categoryId)) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> emptyList()
                }
            }.orEmpty()
            val subRubros = activeFilters.rubroId?.let { rubroId ->
                when (val result = getSubRubrosByRubroUseCase(rubroId)) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> emptyList()
                }
            }.orEmpty()

            _filterSheetState.update {
                FilterSheetUiState(
                    visible = true,
                    categories = categories,
                    rubros = rubros,
                    subRubros = subRubros,
                    selectedCategoryId = activeFilters.categoryId,
                    selectedCategoryLabel = activeFilters.categoryLabel,
                    selectedRubroId = activeFilters.rubroId,
                    selectedRubroLabel = activeFilters.rubroLabel,
                    selectedSubRubroId = activeFilters.subRubroId,
                    selectedSubRubroLabel = activeFilters.subRubroLabel,
                    barrioQuery = activeFilters.barrioLabel.orEmpty(),
                    selectedBarrioId = activeFilters.barrioId,
                    selectedBarrioLabel = activeFilters.barrioLabel,
                    selectedModality = activeFilters.modalidadAtencion,
                    selectedDate = activeFilters.fechaDeseada,
                )
            }
        }
    }

    private fun applyFilters() {
        val sheet = _filterSheetState.value
        activeFilters = activeFilters.copy(
            categoryId = sheet.selectedCategoryId,
            categoryLabel = sheet.selectedCategoryLabel,
            rubroId = sheet.selectedRubroId,
            rubroLabel = sheet.selectedRubroLabel,
            subRubroId = sheet.selectedSubRubroId,
            subRubroLabel = sheet.selectedSubRubroLabel,
            barrioId = sheet.selectedBarrioId,
            barrioLabel = sheet.selectedBarrioLabel,
            modalidadAtencion = sheet.selectedModality,
            fechaDeseada = sheet.selectedDate,
        )
        activeFilters.persistTo(savedStateHandle)
        _filterSheetState.update { it.copy(visible = false) }
        pendingTrackOnSuccess = true
        refresh()
    }

    private fun clearFilters() {
        activeFilters = WorkerSearchFilters(query = activeFilters.query)
        activeFilters.persistTo(savedStateHandle)
        _filterSheetState.update { FilterSheetUiState(visible = it.visible) }
        refresh()
    }

    private fun onFilterCategorySelected(category: CatalogOption) {
        viewModelScope.launch {
            val rubros = when (val result = getRubrosByCategoryUseCase(category.id)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> emptyList()
            }
            _filterSheetState.update {
                it.copy(
                    selectedCategoryId = category.id,
                    selectedCategoryLabel = category.label,
                    rubros = rubros,
                    selectedRubroId = null,
                    selectedRubroLabel = null,
                    subRubros = emptyList(),
                    selectedSubRubroId = null,
                    selectedSubRubroLabel = null,
                )
            }
        }
    }

    private fun onFilterRubroSelected(rubro: CatalogOption) {
        viewModelScope.launch {
            val subRubros = when (val result = getSubRubrosByRubroUseCase(rubro.id)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> emptyList()
            }
            _filterSheetState.update {
                it.copy(
                    selectedRubroId = rubro.id,
                    selectedRubroLabel = rubro.label,
                    subRubros = subRubros,
                    selectedSubRubroId = null,
                    selectedSubRubroLabel = null,
                )
            }
        }
    }

    private fun onFilterSubRubroSelected(subRubro: CatalogOption) {
        _filterSheetState.update {
            it.copy(
                selectedSubRubroId = subRubro.id,
                selectedSubRubroLabel = subRubro.label,
            )
        }
    }

    private fun onBarrioQueryChanged(query: String) {
        _filterSheetState.update { it.copy(barrioQuery = query, barrioSuggestions = emptyList()) }
        barrioSearchJob?.cancel()
        if (query.length < 2) return
        barrioSearchJob = viewModelScope.launch {
            when (val result = searchBarriosUseCase(query)) {
                is AppResult.Success -> {
                    _filterSheetState.update { it.copy(barrioSuggestions = result.data) }
                }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun onBarrioSelected(barrio: CatalogOption) {
        _filterSheetState.update {
            it.copy(
                barrioQuery = barrio.label,
                selectedBarrioId = barrio.id,
                selectedBarrioLabel = barrio.label,
                barrioSuggestions = emptyList(),
            )
        }
    }

    private fun removeFilterChip(chip: FilterChip) {
        activeFilters = when (chip.type) {
            FilterChipType.CATEGORY -> activeFilters.copy(
                categoryId = null,
                categoryLabel = null,
                rubroId = null,
                rubroLabel = null,
                subRubroId = null,
                subRubroLabel = null,
            )
            FilterChipType.RUBRO -> activeFilters.copy(
                rubroId = null,
                rubroLabel = null,
                subRubroId = null,
                subRubroLabel = null,
            )
            FilterChipType.SUBRUBRO -> activeFilters.copy(
                subRubroId = null,
                subRubroLabel = null,
            )
            FilterChipType.BARRIO -> activeFilters.copy(
                barrioId = null,
                barrioLabel = null,
            )
            FilterChipType.MODALITY -> activeFilters.copy(modalidadAtencion = null)
            FilterChipType.DATE -> activeFilters.copy(fechaDeseada = null)
            FilterChipType.QUERY -> activeFilters.copy(query = null)
        }
        activeFilters.persistTo(savedStateHandle)
        refresh()
    }

    private fun buildActiveFilterChips(filters: WorkerSearchFilters): List<FilterChip> = buildList {
        filters.categoryLabel?.let { add(FilterChip(FilterChipType.CATEGORY, it)) }
        filters.rubroLabel?.let { add(FilterChip(FilterChipType.RUBRO, it)) }
        filters.subRubroLabel?.let { add(FilterChip(FilterChipType.SUBRUBRO, it)) }
        filters.barrioLabel?.let { add(FilterChip(FilterChipType.BARRIO, it)) }
        filters.modalidadAtencion?.let {
            add(FilterChip(FilterChipType.MODALITY, it.name.lowercase().replaceFirstChar { c -> c.titlecase() }))
        }
        filters.fechaDeseada?.let { add(FilterChip(FilterChipType.DATE, it)) }
        filters.query?.takeIf { it.isNotBlank() }?.let { add(FilterChip(FilterChipType.QUERY, it)) }
    }

    private fun maybeTrackSuccessfulSearch() {
        if (!pendingTrackOnSuccess) return
        pendingTrackOnSuccess = false
        val entry = recentEntryForActiveFilters() ?: return
        val key = listOf(
            entry.type.name,
            entry.id.toString(),
            entry.label,
        ).joinToString("|")
        if (key == trackedKey) return
        trackedKey = key
        viewModelScope.launch { trackRecentSearchUseCase(entry) }
    }

    private fun recentEntryForActiveFilters(): RecentSearchEntry? {
        val query = activeFilters.query?.trim().orEmpty()
        val rubroId = activeFilters.rubroId
        val rubroLabel = activeFilters.rubroLabel?.trim().orEmpty()
        val subRubroId = activeFilters.subRubroId
        val subRubroLabel = activeFilters.subRubroLabel?.trim().orEmpty()
        return when {
            subRubroId != null && subRubroLabel.isNotEmpty() -> RecentSearchEntry(
                type = RecentSearchType.SUBRUBRO,
                id = subRubroId,
                label = subRubroLabel,
                categoryId = activeFilters.categoryId,
                rubroId = rubroId,
            )
            rubroId != null && rubroLabel.isNotEmpty() -> RecentSearchEntry(
                type = RecentSearchType.RUBRO,
                id = rubroId,
                label = rubroLabel,
                categoryId = activeFilters.categoryId,
                rubroId = rubroId,
            )
            query.isNotEmpty() -> RecentSearchEntry(
                type = RecentSearchType.QUERY,
                id = query.hashCode().toLong(),
                label = query,
            )
            else -> null
        }
    }

    private fun emitEffect(effect: SearchResultsEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

sealed interface SearchResultsUiState {
    data object Loading : SearchResultsUiState
    data class Success(
        val workers: List<WorkerSummary>,
        val activeFilters: WorkerSearchFilters,
        val filterChips: List<FilterChip>,
        val isLoadingPage: Boolean,
        val endReached: Boolean,
        val isRefreshing: Boolean = false,
    ) : SearchResultsUiState
    data class Empty(
        val activeFilters: WorkerSearchFilters,
        val filterChips: List<FilterChip>,
        val isRefreshing: Boolean = false,
    ) : SearchResultsUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : SearchResultsUiState
}

data class FilterSheetUiState(
    val visible: Boolean = false,
    val categories: List<CatalogOption> = emptyList(),
    val rubros: List<CatalogOption> = emptyList(),
    val subRubros: List<CatalogOption> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedCategoryLabel: String? = null,
    val selectedRubroId: Long? = null,
    val selectedRubroLabel: String? = null,
    val selectedSubRubroId: Long? = null,
    val selectedSubRubroLabel: String? = null,
    val barrioQuery: String = "",
    val barrioSuggestions: List<CatalogOption> = emptyList(),
    val selectedBarrioId: Long? = null,
    val selectedBarrioLabel: String? = null,
    val selectedModality: com.cocido.oficios.domain.model.AttentionModality? = null,
    val selectedDate: String? = null,
)

internal fun hasRemovableFilterChips(chips: List<FilterChip>): Boolean =
    chips.any { it.type != FilterChipType.QUERY }

enum class FilterChipType {
    CATEGORY,
    RUBRO,
    SUBRUBRO,
    BARRIO,
    MODALITY,
    DATE,
    QUERY,
}

data class FilterChip(
    val type: FilterChipType,
    val label: String,
)

sealed interface SearchResultsEvent {
    data object Retry : SearchResultsEvent
    data object Refresh : SearchResultsEvent
    data object Back : SearchResultsEvent
    data object LoadNextPage : SearchResultsEvent
    data object OpenFilters : SearchResultsEvent
    data object DismissFilters : SearchResultsEvent
    data object ApplyFilters : SearchResultsEvent
    data object ClearFilters : SearchResultsEvent
    data class QueryChanged(val query: String) : SearchResultsEvent
    data object QuerySubmitted : SearchResultsEvent
    data object VoiceSearch : SearchResultsEvent
    data class WorkerViewProfile(val worker: WorkerSummary) : SearchResultsEvent
    data class WorkerContact(val worker: WorkerSummary) : SearchResultsEvent
    data class FilterCategorySelected(val category: CatalogOption) : SearchResultsEvent
    data class FilterRubroSelected(val rubro: CatalogOption) : SearchResultsEvent
    data class FilterSubRubroSelected(val subRubro: CatalogOption) : SearchResultsEvent
    data class FilterBarrioQueryChanged(val query: String) : SearchResultsEvent
    data class FilterBarrioSelected(val barrio: CatalogOption) : SearchResultsEvent
    data class FilterModalitySelected(
        val modality: com.cocido.oficios.domain.model.AttentionModality?,
    ) : SearchResultsEvent
    data class FilterDateChanged(val date: String?) : SearchResultsEvent
    data class RemoveFilterChip(val chip: FilterChip) : SearchResultsEvent
}

sealed interface SearchResultsEffect {
    data object NavigateBack : SearchResultsEffect
    data class NavigateToWorker(val workerId: Long) : SearchResultsEffect
    data class NavigateToHire(val workerId: Long) : SearchResultsEffect
    data object ShowComingSoon : SearchResultsEffect
    data class ShowMessage(val message: String) : SearchResultsEffect
}
