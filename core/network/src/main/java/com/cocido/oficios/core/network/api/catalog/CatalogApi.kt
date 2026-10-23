package com.cocido.oficios.core.network.api.catalog

import com.cocido.oficios.core.network.api.catalog.dto.BarrioDto
import com.cocido.oficios.core.network.api.catalog.dto.CategoriaDto
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogApi {

    @GET("categorias/search")
    suspend fun searchCategories(
        @Query("pageSize") pageSize: Int = 100,
        @Query("pageNumber") pageNumber: Int = 1,
    ): PaginatedResponseDto<CategoriaDto>

    @GET("rubros/search")
    suspend fun searchRubros(
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("pageSize") pageSize: Int = 100,
        @Query("pageNumber") pageNumber: Int = 1,
    ): PaginatedResponseDto<RubroDto>

    @GET("sub-rubros/search")
    suspend fun searchSubRubros(
        @Query("rubroId") rubroId: Long? = null,
        @Query("pageSize") pageSize: Int = 100,
        @Query("pageNumber") pageNumber: Int = 1,
    ): PaginatedResponseDto<SubRubroDto>

    @GET("barrios/search")
    suspend fun searchBarrios(
        @Query("q") query: String? = null,
        @Query("nombre") nombre: String? = null,
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNumber") pageNumber: Int = 1,
    ): PaginatedResponseDto<BarrioDto>
}
