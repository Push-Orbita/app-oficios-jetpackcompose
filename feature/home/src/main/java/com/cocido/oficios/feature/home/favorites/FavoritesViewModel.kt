package com.cocido.oficios.feature.home.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.repository.FavoritesRepository
import com.cocido.oficios.domain.usecase.social.GetFavoritesUseCase
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
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<FavoritesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var skipNextResume = true

    init {
        load(pageNumber = 1, append = false)
        viewModelScope.launch {
            favoritesRepository.favoritesChanged.collect {
                load(pageNumber = 1, append = false, silent = _uiState.value.items.isNotEmpty())
            }
        }
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        load(pageNumber = 1, append = false, silent = _uiState.value.items.isNotEmpty())
    }

    fun onEvent(event: FavoritesEvent) {
        when (event) {
            FavoritesEvent.Retry -> load(pageNumber = 1, append = false)
            FavoritesEvent.LoadMore -> {
                val state = _uiState.value
                if (!state.hasMore || state.isLoadingMore) return
                load(pageNumber = state.pageNumber + 1, append = true)
            }
            is FavoritesEvent.HireClicked -> emitEffect(FavoritesEffect.NavigateToHire(event.workerId))
            is FavoritesEvent.ProfileClicked -> emitEffect(FavoritesEffect.NavigateToWorker(event.workerId))
            FavoritesEvent.SearchClicked -> emitEffect(FavoritesEffect.NavigateToSearch)
        }
    }

    private fun load(pageNumber: Int, append: Boolean, silent: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !append && !silent,
                    isLoadingMore = append,
                    errorMessage = if (silent) it.errorMessage else null,
                )
            }
            when (val result = getFavoritesUseCase(pageNumber = pageNumber)) {
                is AppResult.Success -> {
                    val page = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = if (append) it.items + page.items else page.items,
                            pageNumber = page.pageNumber,
                            hasMore = page.hasMore,
                            errorMessage = null,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = result.error.userMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: FavoritesEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
