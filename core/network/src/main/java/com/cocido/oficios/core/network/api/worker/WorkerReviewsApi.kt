package com.cocido.oficios.core.network.api.worker

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.worker.dto.CalificacionDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WorkerReviewsApi {

    @GET("calificaciones/search")
    suspend fun searchReviews(
        @Query("trabajadorOficioId") trabajadorOficioId: Long,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int = 10,
    ): PaginatedResponseDto<CalificacionDto>
}

