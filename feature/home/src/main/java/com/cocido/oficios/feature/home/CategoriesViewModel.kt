package com.cocido.oficios.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.usecase.GetHomeCategoriesUseCase
import com.cocido.oficios.domain.usecase.TrackRecentCategoryUseCase
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
class CategoriesViewModel @Inject constructor(
    private val getHomeCategoriesUseCase: GetHomeCategoriesUseCase,
    private val trackRecentCategoryUseCase: TrackRecentCategoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CategoriesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadCategories()
    }

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            CategoriesEvent.Retry -> loadCategories()
            CategoriesEvent.Back -> emitEffect(CategoriesEffect.NavigateBack)
            CategoriesEvent.OpenNotifications -> emitEffect(CategoriesEffect.ShowComingSoon)
            is CategoriesEvent.CategorySelected -> onCategorySelected(event.category)
            is CategoriesEvent.SearchSubmitted -> onSearchSubmitted(event.query)
            CategoriesEvent.VoiceSearch -> emitEffect(CategoriesEffect.ShowComingSoon)
        }
    }

    private fun onSearchSubmitted(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        emitEffect(CategoriesEffect.NavigateToSearchResults(trimmed))
    }

    private fun onCategorySelected(category: HomeCategoryItem) {
        if (category.id < 0) return
        viewModelScope.launch {
            trackRecentCategoryUseCase(category.id, category.name)
            emitEffect(CategoriesEffect.NavigateToRubros(category.id, category.name))
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { CategoriesUiState.Loading }
            when (val result = getHomeCategoriesUseCase()) {
                is AppResult.Success -> {
                    _uiState.update { CategoriesUiState.Success(result.data) }
                }
                is AppResult.Error -> {
                    _uiState.update { CategoriesUiState.Error(result.error.userMessage()) }
                }
            }
        }
    }

    private fun emitEffect(effect: CategoriesEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
