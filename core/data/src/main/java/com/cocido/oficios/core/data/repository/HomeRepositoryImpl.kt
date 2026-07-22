package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.mapper.toHomeCategoryItems
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.ProviderBenefit
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.HomeRepository
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RecentSearchRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext

class HomeRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository,
    private val recentSearchRepository: RecentSearchRepository,
    private val workerSearchRepository: WorkerSearchRepository,
    private val onboardingRepository: OnboardingRepository,
    private val providerProfileRepository: ProviderProfileRepository,
    private val dispatcherProvider: DispatcherProvider,
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
                            val recent = resolveRecentSearches(categories)
                            val featuredWorkers = loadFeaturedWorkers()
                            AppResult.Success(
                                userResult.data.toClientHomeContent(
                                    recentSearches = recent,
                                    featuredWorkers = featuredWorkers,
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
                            val primarySpecialtyLabel = loadPrimarySpecialtyLabel()
                            AppResult.Success(
                                ProviderHomeContent(
                                    userDisplayName = listOf(userResult.data.firstName, userResult.data.lastName)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" "),
                                    isVerified = status.isIdentityVerified,
                                    primarySpecialtyLabel = primarySpecialtyLabel,
                                    profileCompletionPercent = status.toCompletionPercent(),
                                    missingProfileItems = missingItems,
                                    benefits = staticProviderBenefits(),
                                    hasActiveBooking = false,
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

    /**
     * El backend no expone un porcentaje numérico de completitud (solo flags booleanos),
     * así que se deriva un valor aproximado en incrementos de 25% a partir de los 4 hitos
     * reales que sí devuelve `trabajador-oficio/me/estado-perfil`.
     */
    private fun ProviderProfileStatus.toCompletionPercent(): Int {
        val milestones = listOf(isIdentityVerified, isProfileComplete, isConfigurationComplete, isOperational)
        val completed = milestones.count { it }
        return (completed * 100) / milestones.size
    }

    /**
     * Contenido estático: el swagger real (apioficios.pushsoftware.com.ar/api-json) no tiene
     * ningún tag de "beneficios" — se deja este placeholder local hasta que el backend lo exponga.
     */
    private fun staticProviderBenefits(): List<ProviderBenefit> = listOf(
        ProviderBenefit(
            title = "Ferretería El Clavo",
            description = "Descuento en materiales de construcción para trabajadores registrados.",
            discountLabel = "10% OFF",
        ),
        ProviderBenefit(
            title = "Seguro de responsabilidad civil",
            description = "Cobertura preferencial para trabajadores de oficio activos.",
        ),
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

    private suspend fun resolveRecentSearches(
        categories: List<HomeCategoryItem>,
    ): List<RecentSearchEntry> {
        val stored = recentSearchRepository.getRecents()
        if (stored.isNotEmpty()) return stored.take(3)

        return categories.take(3).map { category ->
            RecentSearchEntry(
                type = RecentSearchType.CATEGORY,
                id = category.id,
                label = category.name,
                categoryId = category.id,
            )
        }
    }

    private suspend fun loadFeaturedWorkers(): List<WorkerSummary> {
        return when (
            val result = workerSearchRepository.searchWorkers(
                filters = WorkerSearchFilters(),
                pageNumber = 1,
                pageSize = FEATURED_WORKERS_PAGE_SIZE,
            )
        ) {
            is AppResult.Success -> result.data.items
            is AppResult.Error -> emptyList()
        }
    }

    private fun User.toClientHomeContent(
        recentSearches: List<RecentSearchEntry>,
        featuredWorkers: List<WorkerSummary>,
    ): ClientHomeContent =
        ClientHomeContent(
            userDisplayName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
            emailVerified = emailVerified,
            profilePhotoUrl = profilePhotoUrl,
            recentSearches = recentSearches,
            featuredWorkers = featuredWorkers,
        )

    private companion object {
        const val FEATURED_WORKERS_PAGE_SIZE = 5
    }
}
