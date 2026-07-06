package com.cocido.oficios.domain.usecase

import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.repository.HomeRepository
import javax.inject.Inject

class TrackRecentCategoryUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(categoryId: Long, categoryName: String) {
        homeRepository.trackRecentSearch(
            RecentSearchEntry(
                type = RecentSearchType.CATEGORY,
                id = categoryId,
                label = categoryName,
                categoryId = categoryId,
            ),
        )
    }
}
