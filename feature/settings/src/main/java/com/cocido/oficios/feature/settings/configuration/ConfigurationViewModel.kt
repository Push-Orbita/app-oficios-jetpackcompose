package com.cocido.oficios.feature.settings.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.domain.usecase.ObserveThemeModeUseCase
import com.cocido.oficios.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigurationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeThemeModeUseCase().collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun onEvent(event: ConfigurationEvent) {
        when (event) {
            is ConfigurationEvent.SetThemeMode -> viewModelScope.launch {
                setThemeModeUseCase(event.mode)
            }
            ConfigurationEvent.NavigateBack -> Unit
        }
    }
}
