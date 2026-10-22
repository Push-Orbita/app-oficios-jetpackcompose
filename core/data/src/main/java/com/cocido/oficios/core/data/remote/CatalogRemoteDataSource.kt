package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.network.api.catalog.CatalogApi
import com.cocido.oficios.core.network.api.catalog.dto.BarrioDto
import com.cocido.oficios.core.network.api.catalog.dto.CategoriaDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRemoteDataSource @Inject constructor(
    private val catalogApi: CatalogApi,
) {
    suspend fun fetchCategories(): AppResult<List<CategoriaDto>> = safeApiCall {
        catalogApi.searchCategories().data
    }

    suspend fun fetchRubros(categoryId: Long): AppResult<List<RubroDto>> = safeApiCall {
        catalogApi.searchRubros(categoriaId = categoryId).data
    }

    suspend fun fetchAllRubros(): AppResult<List<RubroDto>> = safeApiCall {
        catalogApi.searchRubros(pageSize = 100).data
    }

    suspend fun fetchSubRubros(rubroId: Long): AppResult<List<SubRubroDto>> = safeApiCall {
        catalogApi.searchSubRubros(rubroId = rubroId).data
    }

    suspend fun fetchAllSubRubros(): AppResult<List<SubRubroDto>> = safeApiCall {
        catalogApi.searchSubRubros(pageSize = 100).data
    }

    suspend fun fetchBarrios(
        query: String?,
        nombre: String? = query,
        pageSize: Int = 20,
    ): AppResult<List<BarrioDto>> = safeApiCall {
        catalogApi.searchBarrios(query = query, nombre = nombre, pageSize = pageSize).data
    }
}
