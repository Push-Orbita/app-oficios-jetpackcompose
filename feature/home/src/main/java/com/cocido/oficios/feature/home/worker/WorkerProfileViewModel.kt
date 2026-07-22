package com.cocido.oficios.feature.home.worker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

private const val REVIEWS_PAGE_SIZE = 10

@HiltViewModel
class WorkerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase,
    private val getWorkerReviewsUseCase: GetWorkerReviewsUseCase,
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L

    private val _uiState = MutableStateFlow<WorkerProfileUiState>(WorkerProfileUiState.Loading)
    val uiState: StateFlow<WorkerProfileUiState> = _uiState.asStateFlow()

    private val _effects = Channel<WorkerProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentReviewsPage = 0
    private var hasMoreReviews = false

    init {
        if (workerId > 0) {
            loadInitial()
        } else {
            _uiState.value = WorkerProfileUiState.Error("ID de trabajador inválido")
        }
    }

    fun onEvent(event: WorkerProfileEvent) {
        when (event) {
            WorkerProfileEvent.Retry -> loadInitial()
            WorkerProfileEvent.Back -> emitEffect(WorkerProfileEffect.NavigateBack)
            WorkerProfileEvent.HireClicked -> emitEffect(WorkerProfileEffect.NavigateToHire(workerId))
            WorkerProfileEvent.LoadNextReviews -> loadNextReviews()
        }
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { WorkerProfileUiState.Loading }

            when (val profileResult = getWorkerProfileUseCase(workerId)) {
                is AppResult.Success -> {
                    val profile = profileResult.data
                    loadReviewsAndSetSuccess(profile, pageNumber = 1)
                }

                is AppResult.Error -> {
                    _uiState.update { WorkerProfileUiState.Error(profileResult.error.userMessage()) }
                }
            }
        }
    }

    private suspend fun loadReviewsAndSetSuccess(profile: com.cocido.oficios.domain.model.WorkerProfile, pageNumber: Int) {
        when (val reviewsResult = getWorkerReviewsUseCase(workerId, pageNumber, REVIEWS_PAGE_SIZE)) {
            is AppResult.Success -> {
                val paged = reviewsResult.data
                currentReviewsPage = paged.pageNumber
                hasMoreReviews = paged.hasMore
                _uiState.update {
                    WorkerProfileUiState.Success(
                        profile = profile,
                        reviews = paged.items,
                        isLoadingReviews = false,
                        hasMoreReviews = paged.hasMore,
                        reviewsPageNumber = paged.pageNumber,
                        totalReviewsPages = paged.totalPages,
                    )
                }
            }

            is AppResult.Error -> {
                _uiState.update {
                    WorkerProfileUiState.Success(
                        profile = profile,
                        reviews = emptyList(),
                        isLoadingReviews = false,
                        hasMoreReviews = false,
                    )
                }
            }
        }
    }

    private fun loadNextReviews() {
        val state = uiState.value
        if (state !is WorkerProfileUiState.Success) return
        if (!state.hasMoreReviews || state.isLoadingReviews) return
        if (hasMoreReviews.not()) return

        viewModelScope.launch {
            _uiState.update { current ->
                if (current is WorkerProfileUiState.Success) {
                    current.copy(isLoadingReviews = true)
                } else current
            }

            val nextPage = currentReviewsPage + 1
            when (val reviewsResult = getWorkerReviewsUseCase(workerId, nextPage, REVIEWS_PAGE_SIZE)) {
                is AppResult.Success -> {
                    val paged = reviewsResult.data
                    currentReviewsPage = paged.pageNumber
                    hasMoreReviews = paged.hasMore
                    _uiState.update { current ->
                        if (current is WorkerProfileUiState.Success) {
                            current.copy(
                                reviews = current.reviews + paged.items,
                                isLoadingReviews = false,
                                hasMoreReviews = paged.hasMore,
                                reviewsPageNumber = paged.pageNumber,
                                totalReviewsPages = paged.totalPages,
                            )
                        } else current
                    }
                }

                is AppResult.Error -> {
                    _uiState.update { current ->
                        if (current is WorkerProfileUiState.Success) current.copy(isLoadingReviews = false) else current
                    }
                    emitEffect(WorkerProfileEffect.ShowMessage(reviewsResult.error.userMessage()))
                }
            }
        }
    }

    private fun emitEffect(effect: WorkerProfileEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

