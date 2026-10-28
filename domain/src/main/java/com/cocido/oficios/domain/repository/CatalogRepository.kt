package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption

interface CatalogRepository {
    suspend fun getCategories(): AppResult<List<CatalogOption>>
    suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>>
    suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>>
    suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>>

    suspend fun getAllRubros(): AppResult<List<CatalogOption>> {
        val categories = when (val result = getCategories()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val rubros = mutableListOf<CatalogOption>()
        for (category in categories) {
            when (val result = getRubrosByCategory(category.id)) {
                is AppResult.Success -> rubros += result.data
                is AppResult.Error -> return result
            }
        }
        return AppResult.Success(rubros)
    }

    suspend fun getAllSubRubros(): AppResult<List<CatalogOption>> {
        val rubros = when (val result = getAllRubros()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val subRubros = mutableListOf<CatalogOption>()
        for (rubro in rubros) {
            when (val result = getSubRubrosByRubro(rubro.id)) {
                is AppResult.Success -> subRubros += result.data
                is AppResult.Error -> return result
            }
        }
        return AppResult.Success(subRubros)
    }
}
