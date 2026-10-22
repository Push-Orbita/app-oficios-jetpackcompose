package com.cocido.oficios.feature.home.worker

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.FavoriteStatus
import com.cocido.oficios.domain.model.PagedWorkerReviews
import com.cocido.oficios.domain.model.ProviderAccountDetails
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import com.cocido.oficios.domain.repository.WorkerProfileRepository
import com.cocido.oficios.domain.model.BookingTracking
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.account.AccountUseCases
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
import org.junit.Assert.assertFalse
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

        val viewModel = viewModel(workerId, repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerProfileUiState.Success)
        state as WorkerProfileUiState.Success

        assertEquals(workerId, state.profile.id)
        assertEquals(1, state.reviews.size)
        assertTrue(state.hasMoreReviews)
        assertFalse(state.isSelf)
        assertTrue(state.canFavorite)
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

        val viewModel = viewModel(workerId, repository)

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
        val viewModel = viewModel(workerId, repository)
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

        val viewModel = viewModel(workerId, repository)

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
        val viewModel = viewModel(workerId, repository, favorites = favorites)
        advanceUntilIdle()

        viewModel.onEvent(WorkerProfileEvent.FavoriteClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value as WorkerProfileUiState.Success
        assertTrue(state.isFavorite)
        assertTrue(state.canFavorite)
        assertTrue(state.rateBookingId == null)
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
        val viewModel = viewModel(workerId, repository, favorites = favorites)
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
        val viewModel = viewModel(
            workerId,
            FakeWorkerProfileRepository(AppResult.Success(profile), emptyMap()),
        )
        advanceUntilIdle()
        val effects = mutableListOf<WorkerProfileEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(WorkerProfileEvent.RateClicked)
        advanceUntilIdle()
        job.cancel()
        assertTrue(effects.none { it is WorkerProfileEffect.NavigateToRate })
        assertTrue(effects.any { it is WorkerProfileEffect.ShowRateUnavailable })
        assertTrue((viewModel.uiState.value as WorkerProfileUiState.Success).rateBookingId == null)
    }

    @Test
    fun `own public profile hides contact request rate and favorite`() = runTest(testDispatcher) {
        val workerId = 42L
        val profile = WorkerProfile(
            id = workerId,
            displayName = "Matias",
            avatarUrl = null,
            isVerified = true,
        )
        val viewer = User(
            id = 7L,
            email = "trabajador@oficios.test",
            firstName = "Matias",
            lastName = "Perez",
            role = UserRole.PROVIDER,
            emailVerified = true,
        )
        val viewModel = viewModel(
            workerId = workerId,
            profileRepo = FakeWorkerProfileRepository(AppResult.Success(profile), emptyMap()),
            favorites = FakeFavoritesStatusRepository(isFavorite = true),
            currentUser = viewer,
            currentProviderWorkerId = workerId,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as WorkerProfileUiState.Success
        assertTrue(state.isSelf)
        assertFalse(state.canFavorite)
        assertFalse(state.isFavorite)
        assertTrue(state.rateBookingId == null)

        val effects = mutableListOf<WorkerProfileEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(WorkerProfileEvent.HireClicked)
        viewModel.onEvent(WorkerProfileEvent.RateClicked)
        viewModel.onEvent(WorkerProfileEvent.FavoriteClicked)
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.none { it is WorkerProfileEffect.NavigateToHire })
        assertTrue(effects.none { it is WorkerProfileEffect.NavigateToRate })
        assertTrue(effects.none { it is WorkerProfileEffect.ShowRateUnavailable })
        assertTrue(effects.none { it is WorkerProfileEffect.ShowFavoriteResult })
        assertFalse((viewModel.uiState.value as WorkerProfileUiState.Success).isFavorite)
    }
}

private fun viewModel(
    workerId: Long,
    profileRepo: WorkerProfileRepository,
    favorites: FavoritesRepository = FakeFavoritesStatusRepository(),
    orders: OrderRepository = IdleOrderRepository(),
    currentUser: User = User(
        id = 1L,
        email = "cliente@oficios.test",
        firstName = "Ana",
        lastName = "Perez",
        role = UserRole.CLIENT,
        emailVerified = true,
    ),
    currentProviderWorkerId: Long? = null,
) = WorkerProfileViewModel(
    savedStateHandle = SavedStateHandle(mapOf("workerId" to workerId)),
    getWorkerProfileUseCase = GetWorkerProfileUseCase(profileRepo),
    getWorkerReviewsUseCase = GetWorkerReviewsUseCase(profileRepo),
    getFavoriteStatusUseCase = GetFavoriteStatusUseCase(favorites),
    toggleFavoriteUseCase = ToggleFavoriteUseCase(favorites),
    orderUseCases = OrderUseCases(orders),
    favoritesRepository = favorites,
    authRepository = FakeWorkerAuthRepository(currentUser),
    accountUseCases = AccountUseCases(
        FakeWorkerAccountRepository(currentUser, currentProviderWorkerId),
    ),
)

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

private class FakeWorkerAuthRepository(
    private val currentUser: User,
) : AuthRepository {
    override suspend fun getCurrentUser() = AppResult.Success(currentUser)
    override suspend fun login(email: String, password: String): AppResult<UserSession> = unused()
    override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> = unused()
    override suspend fun registerProvider(request: RegisterProviderRequest): AppResult<UserSession> = unused()
    override suspend fun refreshToken(refreshToken: String): AppResult<AuthTokens> = unused()
    override suspend fun logout(): AppResult<Unit> = unused()
    override suspend fun requestPasswordReset(email: String): AppResult<Unit> = unused()
    override suspend fun verifyPasswordResetCode(code: String): AppResult<String> = unused()
    override suspend fun resetPassword(
        resetToken: String,
        password: String,
        confirmPassword: String,
    ): AppResult<Unit> = unused()
    override suspend fun sendVerificationEmail(): AppResult<String> = unused()
    override suspend fun verifyEmail(code: String): AppResult<Unit> = unused()
    override suspend fun getUserRoles(): AppResult<List<UserRoleDefinition>> = unused()

    private fun unused(): Nothing = error("unused")
}

private class FakeWorkerAccountRepository(
    private val user: User,
    providerWorkerId: Long?,
) : AccountRepository {
    private val unused = AppResult.Error(AppError.Unknown("unused"))
    private val details = providerWorkerId?.takeIf { it > 0L }?.let { id ->
        ProviderAccountDetails(
            workerId = id,
            status = "VALIDADO",
            document = null,
            description = null,
            experience = null,
            paymentMethods = emptyList(),
            services = emptyList(),
            gallery = emptyList(),
            certificates = emptyList(),
        )
    }
    override val providerDetails: StateFlow<ProviderAccountDetails?> = MutableStateFlow(details)

    override suspend fun getProfile() = AppResult.Success(
        AccountProfile(user = user, providerDetails = details),
    )

    override suspend fun updateProfile(request: UpdateAccountProfileRequest) = unused
    override suspend fun changePassword(email: String, password: String, confirmation: String) = unused
    override suspend fun getClientDomicilios() = unused
    override suspend fun createClientDomicilio(request: ClientDomicilioRequest) = unused
    override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest) = unused
    override suspend fun deleteClientDomicilio(id: Long) = unused
    override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) = unused
    override suspend fun getClientContactos() = unused
    override suspend fun createClientContacto(request: ClientContactoRequest) = unused
    override suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest) = unused
    override suspend fun deleteClientContacto(id: Long) = unused
    override suspend fun setFavoriteContacto(id: Long, favorite: Boolean) = unused
    override suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest) = unused
    override suspend fun addProviderService(request: ProviderRubroFullRequest) = unused
    override suspend fun deleteProviderService(id: Long) = unused
    override suspend fun replaceProviderService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ) = unused
    override suspend fun getCertificates() = unused
    override suspend fun uploadCertificate(name: String, filePath: String) = unused
    override suspend fun updateCertificate(id: Long, name: String, currentUrl: String?, filePath: String?) = unused
    override suspend fun deleteCertificate(id: Long) = unused
    override suspend fun getGallery() = unused
    override suspend fun uploadGallery(filePaths: List<String>) = unused
    override suspend fun deleteGalleryItem(id: Long) = unused
    override suspend fun getCurrentDeletionRequest() = unused
    override suspend fun requestAccountDeletion(params: RequestAccountDeletionParams) = unused
    override suspend fun cancelAccountDeletion() = unused
}

private class IdleOrderRepository : OrderRepository {
    override val ordersChanged: SharedFlow<Unit> = MutableSharedFlow()
    override suspend fun getClientOrders(): AppResult<List<ServiceOrderListItem>> = AppResult.Success(emptyList())
    override suspend fun createServiceRequest(params: CreateServiceRequestParams): AppResult<Long> = unused()
    override suspend fun getWorkerOrders(): AppResult<List<ServiceOrderListItem>> = unused()
    override suspend fun getOrderDetail(orderId: Long): AppResult<ServiceOrder> = unused()
    override suspend fun acceptOrder(orderId: Long): AppResult<Unit> = unused()
    override suspend fun rejectOrder(orderId: Long, reason: String): AppResult<Unit> = unused()
    override suspend fun sendCounterOffer(orderId: Long, newStartIso: String, newEndIso: String, newPrice: Double?): AppResult<Unit> = unused()
    override suspend fun acceptCounterOffer(counterOfferId: Long): AppResult<Unit> = unused()
    override suspend fun rejectCounterOffer(counterOfferId: Long): AppResult<Unit> = unused()
    override suspend fun cancelOrderBeforeAcceptance(orderId: Long): AppResult<Unit> = unused()
    override suspend fun cancelConfirmedOrder(orderId: Long, reason: String?): AppResult<Unit> = unused()
    override suspend fun getBookingTracking(bookingId: Long): AppResult<BookingTracking?> = unused()
    override suspend fun findOrderIdForBooking(bookingId: Long): AppResult<Long?> = unused()
    override suspend fun markEnRoute(bookingId: Long, location: GeoPoint): AppResult<Unit> = unused()
    override suspend fun markArrived(bookingId: Long, location: GeoPoint): AppResult<Unit> = unused()
    override suspend fun getCompletionCode(bookingId: Long): AppResult<String?> = unused()
    override suspend fun resendCompletionCode(bookingId: Long): AppResult<Unit> = unused()
    override suspend fun finalizeBooking(bookingId: Long, completionCode: String): AppResult<Unit> = unused()
    override suspend fun submitRating(bookingId: Long, quality: Int, punctuality: Int, comment: String?): AppResult<Unit> = unused()

    private fun unused(): Nothing = error("unused")
}

