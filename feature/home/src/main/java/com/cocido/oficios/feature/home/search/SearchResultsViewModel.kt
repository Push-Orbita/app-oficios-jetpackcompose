package com.cocido.oficios.feature.home.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.model.averageStarRating
import com.cocido.oficios.domain.model.catalogSuggestions
import com.cocido.oficios.domain.model.hasSearchCriteria
import com.cocido.oficios.domain.model.normalizeSearchText
import com.cocido.oficios.domain.usecase.catalog.GetAllRubrosUseCase
import com.cocido.oficios.domain.usecase.catalog.GetAllSubRubrosUseCase
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.usecase.search.GetRecentClientWorkersUseCase
import com.cocido.oficios.domain.usecase.search.ObserveRecentSearchesUseCase
import com.cocido.oficios.domain.usecase.search.RemoveRecentSearchUseCase
import com.cocido.oficios.domain.usecase.search.SearchWorkersUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
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

private const val QUERY_DEBOUNCE_MS = 300L

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchWorkersUseCase: SearchWorkersUseCase,
    private val trackRecentSearchUseCase: TrackRecentSearchUseCase,
    private val observeRecentSearchesUseCase: ObserveRecentSearchesUseCase,
    private val removeRecentSearchUseCase: RemoveRecentSearchUseCase,
    private val getRecentClientWorkersUseCase: GetRecentClientWorkersUseCase,
    private val getAllRubrosUseCase: GetAllRubrosUseCase,
    private val getAllSubRubrosUseCase: GetAllSubRubrosUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val getSubRubrosByRubroUseCase: GetSubRubrosByRubroUseCase,
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val getWorkerReviewsUseCase: GetWorkerReviewsUseCase,
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
    private var idleRecents: List<RecentSearchEntry> = emptyList()
    private var idleWorkers: List<WorkerSummary> = emptyList()
    private var catalogRubros: List<CatalogOption> = emptyList()
    private var catalogSubRubros: List<CatalogOption> = emptyList()
    private val ratingsCache = mutableMapOf<Long, Pair<Float?, Int?>>()

    init {
        observeRecents()
        loadIdleCatalog()
        loadInitial()
    }

    fun onEvent(event: SearchResultsEvent) {
        when (event) {
            SearchResultsEvent.Retry -> refresh()
            SearchResultsEvent.Refresh -> pullToRefresh()
            SearchResultsEvent.Back -> emitEffect(SearchResultsEffect.NavigateBack)
            SearchResultsEvent.OpenNotifications -> emitEffect(SearchResultsEffect.NavigateToNotifications)
            SearchResultsEvent.LoadNextPage -> loadNextPage()
            SearchResultsEvent.OpenFilters -> openFilters()
            SearchResultsEvent.DismissFilters -> _filterSheetState.update { it.copy(visible = false) }
            SearchResultsEvent.ApplyFilters -> applyFilters()
            SearchResultsEvent.ClearFilters -> clearFilters()
            is SearchResultsEvent.QueryChanged -> onQueryChanged(event.query)
            SearchResultsEvent.QuerySubmitted -> onQuerySubmitted()
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
            SearchResultsEvent.FilterBarrioFocused ->
                onBarrioQueryChanged(_filterSheetState.value.barrioQuery, preserveSelection = true)
            is SearchResultsEvent.FilterBarrioSelected -> onBarrioSelected(event.barrio)
            is SearchResultsEvent.FilterModalitySelected -> {
                _filterSheetState.update { it.copy(selectedModality = event.modality) }
            }
            is SearchResultsEvent.FilterDateChanged -> {
                _filterSheetState.update { it.copy(selectedDate = event.date) }
            }
            is SearchResultsEvent.RemoveFilterChip -> removeFilterChip(event.chip)
            is SearchResultsEvent.SuggestionSelected -> onSuggestionSelected(event.suggestion)
            is SearchResultsEvent.RecentSearchClicked -> onRecentSearchClicked(event.entry)
            is SearchResultsEvent.RemoveRecentSearch -> removeRecent(event.entry)
            is SearchResultsEvent.RubroChipSelected -> onRubroChipSelected(event.rubro)
            is SearchResultsEvent.RecentWorkerClicked -> {
                emitEffect(SearchResultsEffect.NavigateToWorker(event.worker.id))
            }
        }
    }

    private fun onQueryChanged(query: String) {
        _searchQuery.value = query
        activeFilters = activeFilters.copy(query = query.takeIf { it.isNotBlank() })
        activeFilters.persistTo(savedStateHandle)
        pendingTrackOnSuccess = false
        if (!activeFilters.hasSearchCriteria()) {
            queryDebounceJob?.cancel()
            publishIdle(suggestions = emptyList())
            return
        }
        val typingWithoutFilters = !activeFilters.copy(query = null).hasSearchCriteria()
        if (typingWithoutFilters) {
            publishIdle(suggestions = suggestionsFor(query))
        }
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
        if (!activeFilters.hasSearchCriteria()) {
            publishIdle()
            return
        }
        currentPage = 0
        endReached = false
        loadPage(page = 1, append = false)
    }

    private fun refresh() {
        if (!activeFilters.hasSearchCriteria()) {
            loadJob?.cancel()
            publishIdle(suggestions = suggestionsFor(_searchQuery.value))
            return
        }
        currentPage = 0
        endReached = false
        loadPage(page = 1, append = false)
    }

    private fun pullToRefresh() {
        if (!activeFilters.hasSearchCriteria()) {
            loadIdleCatalog()
            return
        }
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
                            is SearchResultsUiState.Idle -> previous.copy(isRefreshing = true)
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
                        val newItems = applyRatingsCache(result.data.items)
                        val merged = (current?.workers.orEmpty()) + newItems
                        _uiState.update {
                            SearchResultsUiState.Success(
                                workers = merged,
                                activeFilters = activeFilters,
                                filterChips = chips,
                                isLoadingPage = false,
                                endReached = endReached,
                            )
                        }
                        fetchMissingRatings(newItems)
                    } else {
                        if (result.data.items.isEmpty()) {
                            pendingTrackOnSuccess = false
                            _uiState.update {
                                SearchResultsUiState.Empty(activeFilters, chips)
                            }
                        } else {
                            maybeTrackSuccessfulSearch()
                            val workersWithCache = applyRatingsCache(result.data.items)
                            _uiState.update {
                                SearchResultsUiState.Success(
                                    workers = workersWithCache,
                                    activeFilters = activeFilters,
                                    filterChips = chips,
                                    isLoadingPage = false,
                                    endReached = endReached,
                                )
                            }
                            fetchMissingRatings(workersWithCache)
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
                    } else if (previous is SearchResultsUiState.Success || previous is SearchResultsUiState.Empty || previous is SearchResultsUiState.Idle) {
                        _uiState.update {
                            when (previous) {
                                is SearchResultsUiState.Success -> previous.copy(isRefreshing = false)
                                is SearchResultsUiState.Empty -> previous.copy(isRefreshing = false)
                                is SearchResultsUiState.Idle -> previous.copy(isRefreshing = false)
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
                            is SearchResultsUiState.Idle -> state.copy(isRefreshing = false)
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

    private fun onBarrioQueryChanged(query: String, preserveSelection: Boolean = false) {
        _filterSheetState.update {
            it.copy(
                barrioQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                selectedBarrioLabel = if (preserveSelection) it.selectedBarrioLabel else null,
                isSearchingBarrios = true,
            )
        }
        barrioSearchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = barrioSearchJob,
            query = query,
            searchBarrios = searchBarriosUseCase,
            onResult = { options ->
                _filterSheetState.update {
                    it.copy(isSearchingBarrios = false, barrioSuggestions = options)
                }
            },
        )
    }

    private fun onBarrioSelected(barrio: CatalogOption) {
        barrioSearchJob?.cancel()
        _filterSheetState.update {
            it.copy(
                barrioQuery = barrio.label,
                selectedBarrioId = barrio.id,
                selectedBarrioLabel = barrio.label,
                barrioSuggestions = emptyList(),
                isSearchingBarrios = false,
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
        if (chip.type == FilterChipType.QUERY) {
            _searchQuery.value = ""
        }
        activeFilters.persistTo(savedStateHandle)
        refresh()
    }

    private fun observeRecents() {
        viewModelScope.launch {
            observeRecentSearchesUseCase().collect { recents ->
                idleRecents = recents
                if (!activeFilters.hasSearchCriteria()) {
                    publishIdle(suggestions = suggestionsFor(_searchQuery.value))
                }
            }
        }
    }

    private fun loadIdleCatalog() {
        viewModelScope.launch {
            catalogRubros = when (val result = getAllRubrosUseCase()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> catalogRubros
            }
            catalogSubRubros = when (val result = getAllSubRubrosUseCase()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> catalogSubRubros
            }
            idleWorkers = getRecentClientWorkersUseCase()
            if (!activeFilters.hasSearchCriteria()) {
                publishIdle(suggestions = suggestionsFor(_searchQuery.value))
            }
        }
    }

    private fun publishIdle(suggestions: List<SearchSuggestion> = emptyList()) {
        _uiState.update {
            SearchResultsUiState.Idle(
                recentSearches = idleRecents,
                recentWorkers = idleWorkers,
                rubroChips = catalogRubros,
                suggestions = suggestions,
            )
        }
    }

    private fun suggestionsFor(query: String): List<SearchSuggestion> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val needle = normalizeSearchText(trimmed)
        val trades = buildList {
            addAll(
                catalogSuggestions(trimmed, catalogSubRubros).map { option ->
                    SearchSuggestion(
                        id = "sub-${option.id}",
                        label = option.label,
                        subtitle = null,
                        kind = SearchSuggestionKind.SUBRUBRO,
                        trade = option,
                    )
                },
            )
            addAll(
                catalogSuggestions(trimmed, catalogRubros).map { option ->
                    SearchSuggestion(
                        id = "rubro-${option.id}",
                        label = option.label,
                        subtitle = null,
                        kind = SearchSuggestionKind.RUBRO,
                        trade = option,
                    )
                },
            )
        }.distinctBy { normalizeSearchText(it.label) }
        val workers = idleWorkers
            .filter { normalizeSearchText(it.displayName).contains(needle) }
            .map { worker ->
                SearchSuggestion(
                    id = "worker-${worker.id}",
                    label = worker.displayName,
                    subtitle = worker.specialtyLabel,
                    kind = SearchSuggestionKind.WORKER,
                    worker = worker,
                )
            }
        return (trades + workers).take(8)
    }

    private fun onSuggestionSelected(suggestion: SearchSuggestion) {
        when (suggestion.kind) {
            SearchSuggestionKind.WORKER -> suggestion.worker?.let { worker ->
                emitEffect(SearchResultsEffect.NavigateToWorker(worker.id))
            }
            SearchSuggestionKind.RUBRO -> suggestion.trade?.let { onRubroChipSelected(it) }
            SearchSuggestionKind.SUBRUBRO -> suggestion.trade?.let { subRubro ->
                queryDebounceJob?.cancel()
                _searchQuery.value = ""
                activeFilters = activeFilters.copy(
                    query = null,
                    subRubroId = subRubro.id,
                    subRubroLabel = subRubro.label,
                )
                activeFilters.persistTo(savedStateHandle)
                pendingTrackOnSuccess = true
                refresh()
            }
        }
    }

    private fun onRecentSearchClicked(entry: RecentSearchEntry) {
        viewModelScope.launch { trackRecentSearchUseCase(entry) }
        when (entry.type) {
            RecentSearchType.CATEGORY -> {
                _searchQuery.value = entry.label
                activeFilters = activeFilters.copy(query = entry.label)
                activeFilters.persistTo(savedStateHandle)
                pendingTrackOnSuccess = true
                refresh()
            }
            RecentSearchType.RUBRO -> {
                _searchQuery.value = ""
                activeFilters = activeFilters.copy(
                    query = null,
                    rubroId = entry.rubroId ?: entry.id,
                    rubroLabel = entry.label,
                    categoryId = entry.categoryId,
                )
                activeFilters.persistTo(savedStateHandle)
                pendingTrackOnSuccess = true
                refresh()
            }
            RecentSearchType.SUBRUBRO -> {
                _searchQuery.value = ""
                activeFilters = activeFilters.copy(
                    query = null,
                    rubroId = entry.rubroId,
                    subRubroId = entry.id,
                    subRubroLabel = entry.label,
                    categoryId = entry.categoryId,
                )
                activeFilters.persistTo(savedStateHandle)
                pendingTrackOnSuccess = true
                refresh()
            }
            RecentSearchType.QUERY -> {
                _searchQuery.value = entry.label
                activeFilters = activeFilters.copy(query = entry.label)
                activeFilters.persistTo(savedStateHandle)
                pendingTrackOnSuccess = true
                refresh()
            }
        }
    }

    private fun removeRecent(entry: RecentSearchEntry) {
        viewModelScope.launch {
            removeRecentSearchUseCase(entry)
            idleRecents = idleRecents.filterNot { it.type == entry.type && it.id == entry.id }
            if (!activeFilters.hasSearchCriteria()) {
                publishIdle(suggestions = suggestionsFor(_searchQuery.value))
            }
        }
    }

    private fun onRubroChipSelected(rubro: CatalogOption) {
        queryDebounceJob?.cancel()
        _searchQuery.value = ""
        activeFilters = activeFilters.copy(
            query = null,
            rubroId = rubro.id,
            rubroLabel = rubro.label,
            subRubroId = null,
            subRubroLabel = null,
        )
        activeFilters.persistTo(savedStateHandle)
        pendingTrackOnSuccess = true
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

    private fun applyRatingsCache(workers: List<WorkerSummary>): List<WorkerSummary> {
        return workers.map { worker ->
            if (worker.rating == null) {
                val cached = ratingsCache[worker.id]
                if (cached != null) {
                    worker.copy(rating = cached.first, reviewCount = cached.second)
                } else {
                    worker
                }
            } else {
                worker
            }
        }
    }

    private fun fetchMissingRatings(workers: List<WorkerSummary>) {
        viewModelScope.launch {
            val workersToFetch = workers.filter { worker ->
                worker.rating == null && !ratingsCache.containsKey(worker.id)
            }
            
            if (workersToFetch.isEmpty()) return@launch
            
            workersToFetch.forEach { worker ->
                launch {
                    val reviewsResult = getWorkerReviewsUseCase(
                        workerId = worker.id,
                        pageNumber = 1,
                        pageSize = 20
                    )
                    
                    if (reviewsResult is AppResult.Success) {
                        val reviews = reviewsResult.data.items
                        val rating = reviews.averageStarRating()
                        val count = reviews.size.takeIf { it > 0 }
                        
                        ratingsCache[worker.id] = Pair(rating, count)
                        
                        _uiState.update { state ->
                            if (state is SearchResultsUiState.Success) {
                                val enrichedWorkers = state.workers.map { w ->
                                    if (w.id == worker.id && w.rating == null) {
                                        w.copy(rating = rating, reviewCount = count)
                                    } else {
                                        w
                                    }
                                }
                                state.copy(workers = enrichedWorkers)
                            } else {
                                state
                            }
                        }
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: SearchResultsEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

sealed interface SearchResultsUiState {
    data class Idle(
        val recentSearches: List<RecentSearchEntry> = emptyList(),
        val recentWorkers: List<WorkerSummary> = emptyList(),
        val rubroChips: List<CatalogOption> = emptyList(),
        val suggestions: List<SearchSuggestion> = emptyList(),
        val isRefreshing: Boolean = false,
    ) : SearchResultsUiState
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
    val isSearchingBarrios: Boolean = false,
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
    data object OpenNotifications : SearchResultsEvent
    data object LoadNextPage : SearchResultsEvent
    data object OpenFilters : SearchResultsEvent
    data object DismissFilters : SearchResultsEvent
    data object ApplyFilters : SearchResultsEvent
    data object ClearFilters : SearchResultsEvent
    data class QueryChanged(val query: String) : SearchResultsEvent
    data object QuerySubmitted : SearchResultsEvent
    data class WorkerViewProfile(val worker: WorkerSummary) : SearchResultsEvent
    data class WorkerContact(val worker: WorkerSummary) : SearchResultsEvent
    data class FilterCategorySelected(val category: CatalogOption) : SearchResultsEvent
    data class FilterRubroSelected(val rubro: CatalogOption) : SearchResultsEvent
    data class FilterSubRubroSelected(val subRubro: CatalogOption) : SearchResultsEvent
    data class FilterBarrioQueryChanged(val query: String) : SearchResultsEvent
    data object FilterBarrioFocused : SearchResultsEvent
    data class FilterBarrioSelected(val barrio: CatalogOption) : SearchResultsEvent
    data class FilterModalitySelected(
        val modality: com.cocido.oficios.domain.model.AttentionModality?,
    ) : SearchResultsEvent
    data class FilterDateChanged(val date: String?) : SearchResultsEvent
    data class RemoveFilterChip(val chip: FilterChip) : SearchResultsEvent
    data class SuggestionSelected(val suggestion: SearchSuggestion) : SearchResultsEvent
    data class RecentSearchClicked(val entry: RecentSearchEntry) : SearchResultsEvent
    data class RemoveRecentSearch(val entry: RecentSearchEntry) : SearchResultsEvent
    data class RubroChipSelected(val rubro: CatalogOption) : SearchResultsEvent
    data class RecentWorkerClicked(val worker: WorkerSummary) : SearchResultsEvent
}

enum class SearchSuggestionKind {
    RUBRO,
    SUBRUBRO,
    WORKER,
}

data class SearchSuggestion(
    val id: String,
    val label: String,
    val subtitle: String?,
    val kind: SearchSuggestionKind,
    val trade: CatalogOption? = null,
    val worker: WorkerSummary? = null,
)

sealed interface SearchResultsEffect {
    data object NavigateBack : SearchResultsEffect
    data object NavigateToNotifications : SearchResultsEffect
    data class NavigateToWorker(val workerId: Long) : SearchResultsEffect
    data class NavigateToHire(val workerId: Long) : SearchResultsEffect
    data class ShowMessage(val message: String) : SearchResultsEffect
}
