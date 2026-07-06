package com.cocido.oficios.feature.home

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.repository.HomeRepository

class FakeHomeRepository(
    private var homeResult: AppResult<ClientHomeContent> = AppResult.Success(
        ClientHomeContent(
            userDisplayName = "Nicolas Ayala",
            emailVerified = true,
            profilePhotoUrl = null,
            recentSearches = listOf(
                RecentSearchEntry(
                    type = RecentSearchType.RUBRO,
                    id = 3,
                    label = "Plomería",
                    rubroId = 3,
                ),
                RecentSearchEntry(
                    type = RecentSearchType.CATEGORY,
                    id = 1,
                    label = "Construcción",
                    categoryId = 1,
                ),
            ),
            featuredWorkers = listOf(
                WorkerSummary(
                    id = 12L,
                    displayName = "Carlos Rodríguez",
                    specialtyLabel = "Plomería",
                    rating = 4.8f,
                    avatarUrl = null,
                    isVerified = true,
                    primaryRubroId = 3L,
                ),
            ),
        ),
    ),
    private var categoriesResult: AppResult<List<HomeCategoryItem>> = AppResult.Success(
        listOf(
            HomeCategoryItem(1, "Construcción"),
            HomeCategoryItem(2, "Mantenimiento"),
        ),
    ),
) : HomeRepository {

    override suspend fun getClientHome(): AppResult<ClientHomeContent> = homeResult

    override suspend fun getCategories(): AppResult<List<HomeCategoryItem>> = categoriesResult

    override suspend fun trackRecentSearch(entry: RecentSearchEntry) = Unit
}
