package com.cocido.oficios.feature.home.rubros

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RubrosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val trackRecentSearchUseCase: TrackRecentSearchUseCase,
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L
    private val categoryName: String = savedStateHandle.get<String>("categoryName").orEmpty()

    private val _uiState = MutableStateFlow<RubrosUiState>(RubrosUiState.Loading)
    val uiState: StateFlow<RubrosUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RubrosEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadRubros()
    }

    fun onEvent(event: RubrosEvent) {
        when (event) {
            RubrosEvent.Retry -> loadRubros()
            RubrosEvent.Refresh -> loadRubros(refreshing = true)
            RubrosEvent.Back -> emitEffect(RubrosEffect.NavigateBack)
            is RubrosEvent.RubroSelected -> onRubroSelected(event.rubro)
            is RubrosEvent.SearchSubmitted -> onSearchSubmitted(event.query)
            RubrosEvent.VoiceSearch -> emitEffect(RubrosEffect.ShowComingSoon)
        }
    }

    private fun onSearchSubmitted(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        emitEffect(
            RubrosEffect.NavigateToSearchResults(
                rubroId = -1L,
                rubroLabel = "",
                categoryId = categoryId,
                categoryLabel = categoryName,
                query = trimmed,
            ),
        )
    }

    private fun onRubroSelected(rubro: CatalogOption) {
        viewModelScope.launch {
            trackRecentSearchUseCase(
                RecentSearchEntry(
                    type = RecentSearchType.RUBRO,
                    id = rubro.id,
                    label = rubro.label,
                    categoryId = categoryId,
                    rubroId = rubro.id,
                ),
            )
            emitEffect(
                RubrosEffect.NavigateToSearchResults(
                    rubroId = rubro.id,
                    rubroLabel = rubro.label,
                    categoryId = categoryId,
                    categoryLabel = categoryName,
                ),
            )
        }
    }

    private fun loadRubros(refreshing: Boolean = false) {
        viewModelScope.launch {
            val previous = _uiState.value
            if (refreshing && previous is RubrosUiState.Success) {
                _uiState.update { previous.copy(isRefreshing = true) }
            } else if (refreshing && previous is RubrosUiState.Error) {
                _uiState.update { previous.copy(isRefreshing = true) }
            } else {
                _uiState.update { RubrosUiState.Loading }
            }
            when (val result = getRubrosByCategoryUseCase(categoryId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        RubrosUiState.Success(
                            categoryName = categoryName,
                            rubros = result.data,
                        )
                    }
                }
                is AppResult.Error -> {
                    if (refreshing && previous is RubrosUiState.Success) {
                        _uiState.update { previous.copy(isRefreshing = false) }
                    } else {
                        _uiState.update { RubrosUiState.Error(result.error.userMessage()) }
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: RubrosEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

sealed interface RubrosUiState {
    data object Loading : RubrosUiState
    data class Success(
        val categoryName: String,
        val rubros: List<CatalogOption>,
        val isRefreshing: Boolean = false,
    ) : RubrosUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : RubrosUiState
}

sealed interface RubrosEvent {
    data object Retry : RubrosEvent
    data object Refresh : RubrosEvent
    data object Back : RubrosEvent
    data class RubroSelected(val rubro: CatalogOption) : RubrosEvent
    data class SearchSubmitted(val query: String) : RubrosEvent
    data object VoiceSearch : RubrosEvent
}

sealed interface RubrosEffect {
    data object NavigateBack : RubrosEffect
    data object ShowComingSoon : RubrosEffect
    data class NavigateToSearchResults(
        val rubroId: Long,
        val rubroLabel: String,
        val categoryId: Long,
        val categoryLabel: String,
        val query: String? = null,
    ) : RubrosEffect
}
