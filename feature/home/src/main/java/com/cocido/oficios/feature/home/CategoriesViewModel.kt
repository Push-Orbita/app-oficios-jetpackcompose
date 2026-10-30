package com.cocido.oficios.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.usecase.GetHomeCategoriesUseCase
import com.cocido.oficios.domain.usecase.TrackRecentCategoryUseCase
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.feature.home.navigation.HomeDestination
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
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _effects = Channel<CategoriesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadCategories()
        refreshUnreadCount()
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            when (val result = getUnreadNotificationCountUseCase()) {
                is AppResult.Success -> _unreadNotificationCount.update { result.data }
                is AppResult.Error -> Unit
            }
        }
    }

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            CategoriesEvent.Retry -> loadCategories()
            CategoriesEvent.Refresh -> loadCategories(refreshing = true)
            CategoriesEvent.Back -> emitEffect(CategoriesEffect.NavigateBack)
            CategoriesEvent.OpenNotifications -> emitEffect(CategoriesEffect.NavigateToNotifications)
            is CategoriesEvent.CategorySelected -> onCategorySelected(event.category)
            is CategoriesEvent.QueryChanged -> onQueryChanged(event.query)
            is CategoriesEvent.SearchSubmitted -> onQueryChanged(event.query)
            CategoriesEvent.SeeAll -> emitEffect(
                CategoriesEffect.NavigateToRubros(HomeDestination.allRubrosCategoryId, ""),
            )
        }
    }

    private fun onQueryChanged(query: String) {
        _uiState.update { state ->
            if (state is CategoriesUiState.Success) state.copy(query = query) else state
        }
    }

    private fun onCategorySelected(category: HomeCategoryItem) {
        if (category.id < 0) return
        viewModelScope.launch {
            trackRecentCategoryUseCase(category.id, category.name)
            emitEffect(CategoriesEffect.NavigateToRubros(category.id, category.name))
        }
    }

    private fun loadCategories(refreshing: Boolean = false) {
        viewModelScope.launch {
            val previous = _uiState.value
            val previousQuery = (previous as? CategoriesUiState.Success)?.query.orEmpty()
            try {
                if (refreshing && previous is CategoriesUiState.Success) {
                    _uiState.update { previous.copy(isRefreshing = true) }
                } else if (refreshing && previous is CategoriesUiState.Error) {
                    _uiState.update { previous.copy(isRefreshing = true) }
                } else {
                    _uiState.update { CategoriesUiState.Loading }
                }
                when (val result = getHomeCategoriesUseCase()) {
                    is AppResult.Success -> {
                        _uiState.update {
                            CategoriesUiState.Success(categories = result.data, query = previousQuery)
                        }
                    }
                    is AppResult.Error -> {
                        val message = result.error.userMessage()
                        if (previous is CategoriesUiState.Success) {
                            _uiState.update { previous.copy(isRefreshing = false) }
                            emitEffect(CategoriesEffect.ShowMessage(message))
                        } else {
                            _uiState.update { CategoriesUiState.Error(message) }
                            if (refreshing) emitEffect(CategoriesEffect.ShowMessage(message))
                        }
                    }
                }
            } finally {
                _uiState.update { state ->
                    when (state) {
                        is CategoriesUiState.Success -> state.copy(isRefreshing = false)
                        is CategoriesUiState.Error -> state.copy(isRefreshing = false)
                        CategoriesUiState.Loading -> state
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: CategoriesEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
