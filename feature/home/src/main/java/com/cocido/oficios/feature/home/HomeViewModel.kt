package com.cocido.oficios.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.GetHomeContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContentUseCase: GetHomeContentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var forceError = false

    init {
        loadContent()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadContent,
            HomeEvent.Retry,
            -> loadContent()
            HomeEvent.SimulateError -> {
                forceError = true
                loadContent()
            }
            HomeEvent.NavigateToSettings -> Unit
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.update { HomeUiState.Loading }
            when (val result = getHomeContentUseCase(forceError = forceError)) {
                is AppResult.Success -> {
                    forceError = false
                    _uiState.update { HomeUiState.Success(result.data) }
                }
                is AppResult.Error -> {
                    _uiState.update { HomeUiState.Error(result.error.userMessage()) }
                }
            }
        }
    }
}
