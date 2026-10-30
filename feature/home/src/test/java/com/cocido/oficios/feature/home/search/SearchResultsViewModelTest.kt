package com.cocido.oficios.feature.home.search

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.catalog.GetAllRubrosUseCase
import com.cocido.oficios.domain.usecase.catalog.GetAllSubRubrosUseCase
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.core.ui.intent.dispatchSpokenSearch
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.usecase.search.GetRecentClientWorkersUseCase
import com.cocido.oficios.domain.usecase.search.ObserveRecentSearchesUseCase
import com.cocido.oficios.domain.usecase.search.RemoveRecentSearchUseCase
import com.cocido.oficios.domain.model.NotificationPage
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import com.cocido.oficios.domain.usecase.search.SearchWorkersUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import com.cocido.oficios.feature.home.orders.NoWorkerProfileRepository
import kotlinx.coroutines.test.advanceTimeBy
import com.cocido.oficios.feature.home.FakeRecentSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SearchResultsViewModelTest {

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
    fun `empty search opens idle instead of querying the backend`() = runTest(testDispatcher) {
        var searches = 0
        val viewModel = createViewModel(
            savedState = SavedStateHandle(),
            onSearch = { searches++ },
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Idle)
        assertEquals(0, searches)
    }

    @Test
    fun `typed query waits 300ms before searching`() = runTest(testDispatcher) {
        var searches = 0
        val viewModel = createViewModel(
            savedState = SavedStateHandle(),
            onSearch = { searches++ },
        )
        advanceUntilIdle()
        viewModel.onEvent(SearchResultsEvent.QueryChanged("plomero"))
        advanceTimeBy(299)
        assertEquals(0, searches)
        advanceTimeBy(1)
        advanceUntilIdle()
        assertEquals(1, searches)
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Success)
    }

    @Test
    fun `load emits success state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Success)
    }

    @Test
    fun `query submit refreshes results`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SearchResultsEvent.QueryChanged("plomero"))
        viewModel.onEvent(SearchResultsEvent.QuerySubmitted)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is SearchResultsUiState.Success)
        assertEquals("plomero", viewModel.searchQuery.value)
    }

    @Test
    fun `spoken query fills field and submits search`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val spoken = "  electricista "
        assertTrue(
            dispatchSpokenSearch(
                spoken = spoken,
                onQueryChange = { viewModel.onEvent(SearchResultsEvent.QueryChanged(it)) },
                onSearch = { viewModel.onEvent(SearchResultsEvent.QuerySubmitted) },
            ),
        )
        advanceUntilIdle()

        assertEquals("electricista", viewModel.searchQuery.value)
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Success)
    }

    @Test
    fun `hasRemovableFilterChips ignores query chip only`() {
        assertTrue(
            hasRemovableFilterChips(
                listOf(
                    FilterChip(FilterChipType.QUERY, "plomero"),
                    FilterChip(FilterChipType.RUBRO, "Gas"),
                ),
            ),
        )
        assertEquals(
            false,
            hasRemovableFilterChips(listOf(FilterChip(FilterChipType.QUERY, "plomero"))),
        )
    }

    @Test
    fun `clearFilters keeps search query`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(SearchResultsEvent.QueryChanged("electricista"))
        viewModel.onEvent(SearchResultsEvent.QuerySubmitted)
        advanceUntilIdle()

        viewModel.onEvent(SearchResultsEvent.ClearFilters)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchResultsUiState.Success || state is SearchResultsUiState.Empty)
        val filters = when (state) {
            is SearchResultsUiState.Success -> state.activeFilters
            is SearchResultsUiState.Empty -> state.activeFilters
            else -> error("unexpected state")
        }
        assertEquals("electricista", filters.query)
        val chips = when (state) {
            is SearchResultsUiState.Success -> state.filterChips
            is SearchResultsUiState.Empty -> state.filterChips
            else -> error("unexpected state")
        }
        assertEquals(listOf(FilterChip(FilterChipType.QUERY, "electricista")), chips)
    }

    @Test
    fun `error emits error state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            searchResult = AppResult.Error(AppError.Unknown()),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Error)
    }

    @Test
    fun `non empty first page tracks rubro`() = runTest(testDispatcher) {
        val recents = FakeRecentSearchRepository()
        createViewModel(recents = recents)
        advanceUntilIdle()
        assertEquals(1, recents.tracked.size)
        assertEquals("Plomería", recents.tracked.single().label)
    }

    @Test
    fun `empty first page does not track`() = runTest(testDispatcher) {
        val recents = FakeRecentSearchRepository()
        createViewModel(
            searchResult = AppResult.Success(
                PagedWorkers(items = emptyList(), pageNumber = 1, totalPages = 1, hasMore = false),
            ),
            recents = recents,
        )
        advanceUntilIdle()
        assertTrue(recents.tracked.isEmpty())
    }

    private fun createViewModel(
        searchResult: AppResult<PagedWorkers> = AppResult.Success(
            PagedWorkers(
                items = listOf(
                    WorkerSummary(
                        id = 1L,
                        displayName = "Test Worker",
                        specialtyLabel = "Plomería",
                        rating = 4.5f,
                        avatarUrl = null,
                        isVerified = true,
                        primaryRubroId = 3L,
                    ),
                ),
                pageNumber = 1,
                totalPages = 1,
                hasMore = false,
            ),
        ),
        recents: FakeRecentSearchRepository = FakeRecentSearchRepository(),
        savedState: SavedStateHandle = SavedStateHandle(
            mapOf(
                "rubroId" to 3L,
                "rubroLabel" to "Plomería",
            ),
        ),
        onSearch: () -> Unit = {},
    ): SearchResultsViewModel {
        val repository = object : WorkerSearchRepository {
            override suspend fun searchWorkers(
                filters: WorkerSearchFilters,
                pageNumber: Int,
                pageSize: Int,
            ): AppResult<PagedWorkers> {
                onSearch()
                return searchResult
            }
        }
        val catalogRepository = object : CatalogRepository {
            override suspend fun getCategories(): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun getAllRubros(): AppResult<List<CatalogOption>> =
                AppResult.Success(listOf(CatalogOption(3, "Plomería"), CatalogOption(4, "Electricidad")))
            override suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
        }
        return SearchResultsViewModel(
            savedStateHandle = savedState,
            searchWorkersUseCase = SearchWorkersUseCase(repository, catalogRepository),
            trackRecentSearchUseCase = TrackRecentSearchUseCase(recents),
            observeRecentSearchesUseCase = ObserveRecentSearchesUseCase(recents),
            removeRecentSearchUseCase = RemoveRecentSearchUseCase(recents),
            getRecentClientWorkersUseCase = GetRecentClientWorkersUseCase(SearchTestOrderRepository()),
            getAllRubrosUseCase = GetAllRubrosUseCase(catalogRepository),
            getAllSubRubrosUseCase = GetAllSubRubrosUseCase(catalogRepository),
            getCategoriesUseCase = GetCategoriesUseCase(catalogRepository),
            getRubrosByCategoryUseCase = GetRubrosByCategoryUseCase(catalogRepository),
            getSubRubrosByRubroUseCase = GetSubRubrosByRubroUseCase(catalogRepository),
            searchBarriosUseCase = SearchBarriosUseCase(catalogRepository),
            getWorkerReviewsUseCase = GetWorkerReviewsUseCase(NoWorkerProfileRepository),
            getUnreadNotificationCountUseCase = GetUnreadNotificationCountUseCase(SearchTestNotificationsRepository()),
        )
    }
}

private class SearchTestNotificationsRepository : NotificationsRepository {
    override suspend fun registerDeviceToken(fcmToken: String, deviceId: String?) = AppResult.Success(Unit)
    override suspend fun markAsRead(notificationId: Long) = AppResult.Success(Unit)
    override suspend fun getNotifications(pageNumber: Int, pageSize: Int, soloNoLeidas: Boolean) =
        AppResult.Success(NotificationPage(emptyList(), 1, 1, false))
}

private class SearchTestOrderRepository : OrderRepository {
    override val ordersChanged = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    override suspend fun getClientOrders() = AppResult.Success(
        listOf(
            ServiceOrderListItem(
                id = 1,
                counterpartName = "Carlos Rodríguez",
                subRubroLabel = "Cañerías",
                status = ServiceRequestStatus.ACCEPTED,
                bookingStatus = null,
                scheduledLabel = null,
                addressLabel = "Av. 25 de Mayo 450",
                rubroLabel = "Plomería",
                isVerified = true,
                workerId = 12,
            ),
        ),
    )
    override suspend fun createServiceRequest(params: com.cocido.oficios.domain.model.CreateServiceRequestParams) = unused()
    override suspend fun getWorkerOrders() = unused()
    override suspend fun getOrderDetail(orderId: Long) = unused()
    override suspend fun acceptOrder(orderId: Long) = unused()
    override suspend fun rejectOrder(orderId: Long, reason: String) = unused()
    override suspend fun sendCounterOffer(orderId: Long, newStartIso: String, newEndIso: String, newPrice: Double?) = unused()
    override suspend fun acceptCounterOffer(counterOfferId: Long) = unused()
    override suspend fun rejectCounterOffer(counterOfferId: Long) = unused()
    override suspend fun cancelOrderBeforeAcceptance(orderId: Long) = unused()
    override suspend fun cancelConfirmedOrder(orderId: Long, reason: String?) = unused()
    override suspend fun getBookingTracking(bookingId: Long) = unused()
    override suspend fun findOrderIdForBooking(bookingId: Long) = unused()
    override suspend fun markEnRoute(bookingId: Long, location: com.cocido.oficios.domain.model.GeoPoint) = unused()
    override suspend fun markArrived(bookingId: Long, location: com.cocido.oficios.domain.model.GeoPoint) = unused()
    override suspend fun getCompletionCode(bookingId: Long) = unused()
    override suspend fun resendCompletionCode(bookingId: Long) = unused()
    override suspend fun finalizeBooking(bookingId: Long, completionCode: String) = unused()
    override suspend fun submitRating(bookingId: Long, quality: Int, punctuality: Int, comment: String?) = unused()
    private fun unused(): Nothing = error("unused")
}
