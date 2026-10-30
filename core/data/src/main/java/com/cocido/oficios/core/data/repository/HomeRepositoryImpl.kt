package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.mapper.toHomeCategoryItems
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.domain.model.ProviderHomeTiles
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.model.resolveProviderHomeTiles
import com.cocido.oficios.domain.model.nowInArgentina
import com.cocido.oficios.domain.model.recentClientWorkers
import com.cocido.oficios.domain.model.upcomingAgendaAppointments
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.HomeRepository
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RecentSearchRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.withContext

class HomeRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository,
    private val recentSearchRepository: RecentSearchRepository,
    private val onboardingRepository: OnboardingRepository,
    private val providerProfileRepository: ProviderProfileRepository,
    private val accountRepository: AccountRepository,
    private val orderRepository: OrderRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val clock: Clock,
) : HomeRepository {

    override suspend fun getClientHome(): AppResult<ClientHomeContent> {
        return withContext(dispatcherProvider.io) {
            when (val userResult = authRepository.getCurrentUser()) {
                is AppResult.Error -> userResult
                is AppResult.Success -> {
                    when (val categoriesResult = catalogRepository.getCategories()) {
                        is AppResult.Error -> categoriesResult
                        is AppResult.Success -> {
                            val categories = categoriesResult.data.toHomeCategoryItems()
                            AppResult.Success(
                                userResult.data.toClientHomeContent(
                                    categories = categories,
                                    recentWorkers = loadRecentClientWorkers(),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    override suspend fun getProviderHome(): AppResult<ProviderHomeContent> {
        return withContext(dispatcherProvider.io) {
            when (val userResult = authRepository.getCurrentUser()) {
                is AppResult.Error -> userResult
                is AppResult.Success -> {
                    when (val statusResult = onboardingRepository.getProviderProfileStatus()) {
                        is AppResult.Error -> statusResult
                        is AppResult.Success -> {
                            val status = statusResult.data
                            val missingItems = onboardingRepository
                                .getProviderRequiredSteps(status)
                                .map { it.title }
                            val accountProfile = loadAccountProfile()
                            val tiles = resolveProfileTiles(
                                status = status,
                                missingItems = missingItems,
                                profile = accountProfile,
                            )
                            val primarySpecialtyLabel = loadPrimarySpecialtyLabel()
                            val upcomingOrders = loadUpcomingWorkerOrders()
                            AppResult.Success(
                                ProviderHomeContent(
                                    userDisplayName = listOf(userResult.data.firstName, userResult.data.lastName)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" "),
                                    isVerified = status.isIdentityVerified,
                                    primarySpecialtyLabel = primarySpecialtyLabel,
                                    profileCompletionPercent = if (status.hasCompletedRequiredProfile) {
                                        100
                                    } else {
                                        tiles.completionPercent
                                    },
                                    missingProfileItems = missingItems,
                                    benefits = emptyList(),
                                    hasActiveBooking = upcomingOrders.any {
                                        it.bookingStatus == com.cocido.oficios.domain.model.BookingStatus.IN_PROGRESS
                                    },
                                    upcomingOrders = upcomingOrders,
                                    profilePhotoUrl = userResult.data.profilePhotoUrl,
                                    hasAgendaConfigured = status.isConfigurationComplete,
                                    hasGalleryPhotos = tiles.gallery,
                                    hasCommercialLocal = tiles.local,
                                    hasCertificate = tiles.certificate,
                                    isRequiredProfileComplete = status.hasCompletedRequiredProfile,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadPrimarySpecialtyLabel(): String? {
        return when (val syncResult = providerProfileRepository.loadProviderProfileForSync()) {
            is AppResult.Success -> syncResult.data.tradeDraft?.let { "${it.categoryName} · ${it.rubroName}" }
            is AppResult.Error -> null
        }
    }

    private suspend fun loadUpcomingWorkerOrders(): List<com.cocido.oficios.domain.model.ServiceOrderListItem> {
        return when (val result = orderRepository.getWorkerOrders()) {
            is AppResult.Success -> upcomingAgendaAppointments(
                orders = result.data,
                now = nowInArgentina(clock),
            )
            is AppResult.Error -> {
                val cached = orderRepository.workerOrders.value ?: return emptyList()
                upcomingAgendaAppointments(orders = cached, now = nowInArgentina(clock))
            }
        }
    }

    private suspend fun loadAccountProfile(): AccountProfile? {
        return when (val result = accountRepository.getProfile()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }
    }

    /**
     * Galería y certificados salen de `GET perfil` (listas reales). Local comercial:
     * `lugarLocal` en subrubros o `domicilios` con calle. Si el perfil no carga, se
     * cae a `faltantes` de `estado-perfil` (no hay % numérico ni flags dedicados).
     */
    private fun resolveProfileTiles(
        status: ProviderProfileStatus,
        missingItems: List<String>,
        profile: AccountProfile?,
    ): ProviderHomeTiles = resolveProviderHomeTiles(
        details = profile?.providerDetails,
        missingSteps = status.missingSteps + missingItems,
        isProfileComplete = status.isProfileComplete,
    )

    override suspend fun getCategories(): AppResult<List<HomeCategoryItem>> {
        return withContext(dispatcherProvider.io) {
            when (val result = catalogRepository.getCategories()) {
                is AppResult.Error -> result
                is AppResult.Success -> AppResult.Success(result.data.toHomeCategoryItems())
            }
        }
    }

    override suspend fun trackRecentSearch(entry: RecentSearchEntry) {
        withContext(dispatcherProvider.io) {
            recentSearchRepository.track(entry)
        }
    }

    private suspend fun loadRecentClientWorkers(): List<com.cocido.oficios.domain.model.WorkerSummary> {
        return when (val result = orderRepository.getClientOrders()) {
            is AppResult.Success -> result.data.recentClientWorkers(RECENT_WORKERS_LIMIT)
            is AppResult.Error -> emptyList()
        }
    }

    private fun User.toClientHomeContent(
        categories: List<HomeCategoryItem>,
        recentWorkers: List<com.cocido.oficios.domain.model.WorkerSummary>,
    ): ClientHomeContent =
        ClientHomeContent(
            userDisplayName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
            emailVerified = emailVerified,
            profilePhotoUrl = profilePhotoUrl,
            recentSearches = emptyList(),
            categories = categories,
            featuredWorkers = recentWorkers,
        )

    private companion object {
        const val RECENT_WORKERS_LIMIT = 8
    }
}
