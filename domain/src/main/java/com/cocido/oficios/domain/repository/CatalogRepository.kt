package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption

interface CatalogRepository {
    suspend fun getCategories(): AppResult<List<CatalogOption>>
    suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>>
    suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>>
    suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>>
}
