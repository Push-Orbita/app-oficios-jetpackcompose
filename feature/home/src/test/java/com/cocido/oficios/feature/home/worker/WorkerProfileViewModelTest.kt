package com.cocido.oficios.feature.home.worker

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FavoriteStatus
import com.cocido.oficios.domain.model.PagedWorkerReviews
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import com.cocido.oficios.domain.repository.WorkerProfileRepository
import com.cocido.oficios.domain.usecase.social.GetFavoriteStatusUseCase
import com.cocido.oficios.domain.usecase.social.ToggleFavoriteUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load emits success state with profile and first page reviews`() = runTest(testDispatcher) {
        val workerId = 42L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Matias",
            avatarUrl = null,
            isVerified = true,
        )

        val reviewsPage1 = PagedWorkerReviews(
            items = listOf(
                WorkerReview(
                    id = 1L,
                    qualityScore = 9,
                    punctualityScore = 4,
                    comment = "Excelente",
                    clientLabel = "Maria",
                ),
            ),
            pageNumber = 1,
            totalPages = 2,
            hasMore = true,
        )

        val repository = FakeWorkerProfileRepository(
            profile = AppResult.Success(profile),
            reviewsPages = mapOf(1 to reviewsPage1),
        )

        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(repository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(repository),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(FakeFavoritesStatusRepository()),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(FakeFavoritesStatusRepository()),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerProfileUiState.Success)
        state as WorkerProfileUiState.Success

        assertEquals(workerId, state.profile.id)
        assertEquals(1, state.reviews.size)
        assertTrue(state.hasMoreReviews)
    }

    @Test
    fun `hire clicked emits NavigateToHire effect`() = runTest(testDispatcher) {
        val workerId = 10L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Cliente",
            avatarUrl = null,
            isVerified = false,
        )

        val repository = FakeWorkerProfileRepository(
            profile = AppResult.Success(profile),
            reviewsPages = emptyMap(),
        )

        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(repository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(repository),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(FakeFavoritesStatusRepository()),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(FakeFavoritesStatusRepository()),
        )

        val effects = mutableListOf<WorkerProfileEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(WorkerProfileEvent.HireClicked)
        advanceUntilIdle()

        job.cancel()

        assertTrue(
            effects.any { it is WorkerProfileEffect.NavigateToHire && it.workerId == workerId },
        )
    }

    @Test
    fun `report clicked emits NavigateToReport with worker name`() = runTest(testDispatcher) {
        val workerId = 10L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Matias",
            avatarUrl = null,
            isVerified = false,
        )
        val repository = FakeWorkerProfileRepository(
            profile = AppResult.Success(profile),
            reviewsPages = emptyMap(),
        )
        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(repository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(repository),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(FakeFavoritesStatusRepository()),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(FakeFavoritesStatusRepository()),
        )
        advanceUntilIdle()

        val effects = mutableListOf<WorkerProfileEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(WorkerProfileEvent.ReportProfileClicked)
        advanceUntilIdle()
        job.cancel()

        assertTrue(
            effects.any {
                it is WorkerProfileEffect.NavigateToReport &&
                    it.workerId == workerId &&
                    it.workerName == "Matias"
            },
        )
    }

    @Test
    fun `load next reviews appends items when hasMoreReviews`() = runTest(testDispatcher) {
        val workerId = 7L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Trabajador",
            avatarUrl = null,
            isVerified = true,
        )

        val reviewsPage1 = PagedWorkerReviews(
            items = listOf(WorkerReview(id = 1L, qualityScore = 8, punctualityScore = 4, comment = null, clientLabel = null)),
            pageNumber = 1,
            totalPages = 2,
            hasMore = true,
        )

        val reviewsPage2 = PagedWorkerReviews(
            items = listOf(WorkerReview(id = 2L, qualityScore = 10, punctualityScore = 5, comment = null, clientLabel = null)),
            pageNumber = 2,
            totalPages = 2,
            hasMore = false,
        )

        val repository = FakeWorkerProfileRepository(
            profile = AppResult.Success(profile),
            reviewsPages = mapOf(
                1 to reviewsPage1,
                2 to reviewsPage2,
            ),
        )

        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(repository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(repository),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(FakeFavoritesStatusRepository()),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(FakeFavoritesStatusRepository()),
        )

        advanceUntilIdle()

        viewModel.onEvent(WorkerProfileEvent.LoadNextReviews)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerProfileUiState.Success)
        state as WorkerProfileUiState.Success

        assertEquals(2, state.reviews.size)
        assertEquals(2, state.reviews.last().id)
        assertTrue(state.hasMoreReviews.not())
    }

    @Test
    fun `favorite click toggles optimistically and keeps favorite on success`() = runTest(testDispatcher) {
        val workerId = 3L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Matias",
            avatarUrl = null,
            isVerified = true,
        )
        val repository = FakeWorkerProfileRepository(
            profile = AppResult.Success(profile),
            reviewsPages = emptyMap(),
        )
        val favorites = FakeFavoritesStatusRepository(isFavorite = false)
        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(repository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(repository),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(favorites),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favorites),
        )
        advanceUntilIdle()

        viewModel.onEvent(WorkerProfileEvent.FavoriteClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value as WorkerProfileUiState.Success
        assertTrue(state.isFavorite)
        assertTrue(state.canFavorite)
        assertTrue(state.canRate.not())
    }

    @Test
    fun `favorite click rolls back when toggle fails`() = runTest(testDispatcher) {
        val workerId = 3L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Matias",
            avatarUrl = null,
            isVerified = true,
        )
        val repository = FakeWorkerProfileRepository(
            profile = AppResult.Success(profile),
            reviewsPages = emptyMap(),
        )
        val favorites = FakeFavoritesStatusRepository(isFavorite = true, failToggle = true)
        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(repository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(repository),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(favorites),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favorites),
        )
        advanceUntilIdle()
        val effects = mutableListOf<WorkerProfileEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(WorkerProfileEvent.FavoriteClicked)
        advanceUntilIdle()
        job.cancel()

        val state = viewModel.uiState.value as WorkerProfileUiState.Success
        assertTrue(state.isFavorite)
        assertTrue(state.isTogglingFavorite.not())
        assertTrue(effects.any { it is WorkerProfileEffect.ShowMessage })
    }

    @Test
    fun `rate stays unavailable without a finished booking of this client`() = runTest(testDispatcher) {
        val workerId = 8L
        val profile = WorkerProfile(id = workerId, displayName = "Ana", avatarUrl = null, isVerified = true)
        val viewModel = WorkerProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
            getWorkerProfileUseCase = GetWorkerProfileUseCase(
                FakeWorkerProfileRepository(AppResult.Success(profile), emptyMap()),
            ),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(
                FakeWorkerProfileRepository(AppResult.Success(profile), emptyMap()),
            ),
            getFavoriteStatusUseCase = GetFavoriteStatusUseCase(FakeFavoritesStatusRepository()),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(FakeFavoritesStatusRepository()),
        )
        advanceUntilIdle()
        val effects = mutableListOf<WorkerProfileEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(WorkerProfileEvent.RateClicked)
        advanceUntilIdle()
        job.cancel()
        assertTrue(effects.any { it is WorkerProfileEffect.ShowRatingUnavailable })
        assertTrue((viewModel.uiState.value as WorkerProfileUiState.Success).canRate.not())
    }
}

private class FakeWorkerProfileRepository(
    private val profile: AppResult<WorkerProfile>,
    private val reviewsPages: Map<Int, PagedWorkerReviews>,
) : WorkerProfileRepository {
    override suspend fun getWorkerProfile(workerId: Long): AppResult<WorkerProfile> = profile

    override suspend fun getWorkerReviews(
        workerId: Long,
        pageNumber: Int,
        pageSize: Int,
    ): AppResult<PagedWorkerReviews> {
        val page = reviewsPages[pageNumber]
        return if (page != null) {
            AppResult.Success(page)
        } else {
            AppResult.Success(
                PagedWorkerReviews(
                    items = emptyList(),
                    pageNumber = pageNumber,
                    totalPages = null,
                    hasMore = false,
                ),
            )
        }
    }
}

private class FakeFavoritesStatusRepository(
    private var isFavorite: Boolean = false,
    private val failToggle: Boolean = false,
) : FavoritesRepository {
    override val favoritesChanged: SharedFlow<Unit> = MutableSharedFlow()
    override suspend fun getFavorites(pageNumber: Int, pageSize: Int) = error("unused")

    override suspend fun getStatus(workerId: Long) =
        AppResult.Success(FavoriteStatus(isFavorite = isFavorite))

    override suspend fun add(workerId: Long, idempotencyKey: String): AppResult<Unit> {
        if (failToggle) return AppResult.Error(AppError.Network("falló"))
        isFavorite = true
        return AppResult.Success(Unit)
    }

    override suspend fun remove(workerId: Long): AppResult<Unit> {
        if (failToggle) return AppResult.Error(AppError.Network("falló"))
        isFavorite = false
        return AppResult.Success(Unit)
    }
}

