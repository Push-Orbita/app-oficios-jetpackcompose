package com.cocido.oficios.feature.home.worker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.orComputedFrom
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.FavoritesRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.social.GetFavoriteStatusUseCase
import com.cocido.oficios.domain.usecase.social.ToggleFavoriteUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val REVIEWS_PAGE_SIZE = 10

@HiltViewModel
class WorkerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase,
    private val getWorkerReviewsUseCase: GetWorkerReviewsUseCase,
    private val getFavoriteStatusUseCase: GetFavoriteStatusUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val orderUseCases: OrderUseCases,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository,
    private val accountUseCases: AccountUseCases,
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L
    private var isSelf: Boolean = false

    private val _uiState = MutableStateFlow<WorkerProfileUiState>(WorkerProfileUiState.Loading)
    val uiState: StateFlow<WorkerProfileUiState> = _uiState.asStateFlow()

    private val _effects = Channel<WorkerProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentReviewsPage = 0
    private var hasMoreReviews = false
    private var skipNextResume = true

    init {
        if (workerId > 0) {
            loadInitial()
            viewModelScope.launch {
                favoritesRepository.favoritesChanged.collect { refreshFavoriteFlag() }
            }
        } else {
            _uiState.value = WorkerProfileUiState.Error("ID de trabajador inválido")
        }
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        refresh()
        refreshFavoriteFlag()
    }

    fun onEvent(event: WorkerProfileEvent) {
        when (event) {
            WorkerProfileEvent.Retry -> loadInitial()
            WorkerProfileEvent.Refresh -> refresh()
            WorkerProfileEvent.Back -> emitEffect(WorkerProfileEffect.NavigateBack)
            WorkerProfileEvent.HireClicked -> {
                if (isSelfProfile()) return
                emitEffect(WorkerProfileEffect.NavigateToHire(workerId))
            }
            WorkerProfileEvent.RateClicked -> {
                if (isSelfProfile()) return
                val state = uiState.value as? WorkerProfileUiState.Success ?: return
                val bookingId = state.rateBookingId
                if (bookingId == null) {
                    emitEffect(WorkerProfileEffect.ShowRateUnavailable)
                    return
                }
                emitEffect(WorkerProfileEffect.NavigateToRate(bookingId, state.profile.displayName))
            }
            WorkerProfileEvent.ReportProfileClicked -> {
                val profile = (uiState.value as? WorkerProfileUiState.Success)?.profile ?: return
                emitEffect(WorkerProfileEffect.NavigateToReport(workerId, profile.displayName))
            }
            WorkerProfileEvent.FavoriteClicked -> {
                if (isSelfProfile()) return
                toggleFavorite()
            }
            WorkerProfileEvent.LoadNextReviews -> loadNextReviews()
        }
    }

    private fun refresh() {
        val current = _uiState.value as? WorkerProfileUiState.Success
        if (current == null) {
            loadInitial()
            return
        }
        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    if (state is WorkerProfileUiState.Success) state.copy(isRefreshing = true) else state
                }
                when (val profileResult = getWorkerProfileUseCase(workerId)) {
                    is AppResult.Success -> loadReviewsAndSetSuccess(
                        profile = profileResult.data,
                        pageNumber = 1,
                        canFavorite = current.canFavorite,
                        isFavorite = current.isFavorite,
                    )
                    is AppResult.Error -> {
                        emitEffect(WorkerProfileEffect.ShowMessage(profileResult.error.userMessage()))
                    }
                }
            } finally {
                _uiState.update { state ->
                    if (state is WorkerProfileUiState.Success) state.copy(isRefreshing = false) else state
                }
            }
        }
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { WorkerProfileUiState.Loading }
            coroutineScope {
                val identityDeferred = async { resolveIsSelf() }
                val profileDeferred = async { getWorkerProfileUseCase(workerId) }
                val favoriteDeferred = async { getFavoriteStatusUseCase(workerId) }
                isSelf = identityDeferred.await()
                when (val profileResult = profileDeferred.await()) {
                    is AppResult.Success -> {
                        val favoriteFlags = favoriteDeferred.await().toFavoriteFlags()
                        loadReviewsAndSetSuccess(
                            profile = profileResult.data,
                            pageNumber = 1,
                            canFavorite = favoriteFlags.canFavorite,
                            isFavorite = favoriteFlags.isFavorite,
                        )
                    }
                    is AppResult.Error -> {
                        _uiState.update { WorkerProfileUiState.Error(profileResult.error.userMessage()) }
                    }
                }
            }
        }
    }

    private suspend fun loadReviewsAndSetSuccess(
        profile: WorkerProfile,
        pageNumber: Int,
        canFavorite: Boolean,
        isFavorite: Boolean,
    ) {
        when (val reviewsResult = getWorkerReviewsUseCase(workerId, pageNumber, REVIEWS_PAGE_SIZE)) {
            is AppResult.Success -> {
                val paged = reviewsResult.data
                currentReviewsPage = paged.pageNumber
                hasMoreReviews = paged.hasMore
                val rateBookingId = if (isSelf) {
                    null
                } else {
                    pendingRatingBookingId(
                        workerId = workerId,
                        orders = clientOrders(),
                        ratedBookingIds = paged.items.mapNotNull { review -> review.bookingId }.toSet(),
                    )
                }
                _uiState.update {
                    WorkerProfileUiState.Success(
                        profile = profile.copy(reputation = profile.reputation.orComputedFrom(paged.items)),
                        reviews = paged.items,
                        isLoadingReviews = false,
                        hasMoreReviews = paged.hasMore,
                        reviewsPageNumber = paged.pageNumber,
                        totalReviewsPages = paged.totalPages,
                        canFavorite = canFavorite && !isSelf,
                        isFavorite = isFavorite && !isSelf,
                        rateBookingId = rateBookingId,
                        isSelf = isSelf,
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
                        canFavorite = canFavorite && !isSelf,
                        isFavorite = isFavorite && !isSelf,
                        isSelf = isSelf,
                    )
                }
            }
        }
    }

    private fun toggleFavorite() {
        val state = uiState.value as? WorkerProfileUiState.Success ?: return
        if (!state.canFavorite || state.isTogglingFavorite) return
        val previous = state.isFavorite
        _uiState.update { current ->
            if (current is WorkerProfileUiState.Success) {
                current.copy(isFavorite = !previous, isTogglingFavorite = true)
            } else {
                current
            }
        }
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(workerId, currentlyFavorite = previous)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        if (current is WorkerProfileUiState.Success) {
                            current.copy(isTogglingFavorite = false)
                        } else {
                            current
                        }
                    }
                    emitEffect(WorkerProfileEffect.ShowFavoriteResult(added = !previous))
                }
                is AppResult.Error -> {
                    _uiState.update { current ->
                        if (current is WorkerProfileUiState.Success) {
                            current.copy(isFavorite = previous, isTogglingFavorite = false)
                        } else {
                            current
                        }
                    }
                    emitEffect(WorkerProfileEffect.ShowMessage(result.error.userMessage()))
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

    private fun refreshFavoriteFlag() {
        if (isSelfProfile()) return
        viewModelScope.launch {
            val flags = getFavoriteStatusUseCase(workerId).toFavoriteFlags()
            _uiState.update { current ->
                if (current is WorkerProfileUiState.Success && !current.isSelf) {
                    current.copy(canFavorite = flags.canFavorite, isFavorite = flags.isFavorite)
                } else {
                    current
                }
            }
        }
    }

    private suspend fun resolveIsSelf(): Boolean {
        val user = (authRepository.getCurrentUser() as? AppResult.Success)?.data
        val cachedWorkerId = accountUseCases.providerDetails.value?.workerId?.takeIf { it > 0L }
        val providerWorkerId = cachedWorkerId ?: if (user?.role == UserRole.PROVIDER) {
            (accountUseCases.getProfile() as? AppResult.Success)?.data?.providerDetails?.workerId
                ?.takeIf { it > 0L }
        } else {
            null
        }
        return isOwnPublicWorkerProfile(
            publicWorkerId = workerId,
            currentUserId = user?.id,
            currentProviderWorkerId = providerWorkerId,
        )
    }

    private fun isSelfProfile(): Boolean {
        val state = _uiState.value as? WorkerProfileUiState.Success
        return isSelf || state?.isSelf == true
    }

    private suspend fun clientOrders() =
        (orderUseCases.getClientOrders() as? AppResult.Success)?.data.orEmpty()

    private fun emitEffect(effect: WorkerProfileEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

private data class FavoriteFlags(
    val canFavorite: Boolean,
    val isFavorite: Boolean,
)

private fun AppResult<com.cocido.oficios.domain.model.FavoriteStatus>.toFavoriteFlags(): FavoriteFlags = when (this) {
    is AppResult.Success -> FavoriteFlags(canFavorite = true, isFavorite = data.isFavorite)
    is AppResult.Error -> FavoriteFlags(
        canFavorite = error !is AppError.Forbidden,
        isFavorite = false,
    )
}
