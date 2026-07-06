package com.cocido.oficios.domain.usecase.catalog

import com.cocido.oficios.domain.repository.CatalogRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke() = catalogRepository.getCategories()
}

class GetRubrosByCategoryUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(categoryId: Long) = catalogRepository.getRubrosByCategory(categoryId)
}

class GetSubRubrosByRubroUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(rubroId: Long) = catalogRepository.getSubRubrosByRubro(rubroId)
}

class SearchBarriosUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(query: String) = catalogRepository.searchBarrios(query)
}
