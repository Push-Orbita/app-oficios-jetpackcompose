package com.cocido.oficios.core.network.api.search

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.search.dto.WorkerSearchItemDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WorkerSearchApi {

    @GET("trabajador-oficio/search")
    suspend fun searchWorkers(
        @Query("rubroId") rubroId: Long? = null,
        @Query("subRubroId") subRubroId: Long? = null,
        @Query("modalidadAtencion") modalidadAtencion: String? = null,
        @Query("localidadId") localidadId: Long? = null,
        @Query("barrioId") barrioId: Long? = null,
        @Query("fechaDeseada") fechaDeseada: String? = null,
        @Query("q") query: String? = null,
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): PaginatedResponseDto<WorkerSearchItemDto>
}
