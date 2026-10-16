package com.cocido.oficios.core.network.api.social

import com.cocido.oficios.core.network.api.social.dto.FavoritoItemDto
import com.cocido.oficios.core.network.api.social.dto.FavoritoStatusDto
import com.cocido.oficios.core.network.api.social.dto.FavoritosPageDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoritesApi {

    @GET("favoritos")
    suspend fun getFavorites(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): FavoritosPageDto

    @GET("favoritos/trabajador-oficio/{trabajadorOficioId}")
    suspend fun getStatus(
        @Path("trabajadorOficioId") trabajadorOficioId: Long,
    ): FavoritoStatusDto

    @PUT("favoritos/trabajador-oficio/{trabajadorOficioId}")
    suspend fun add(
        @Path("trabajadorOficioId") trabajadorOficioId: Long,
        @Header("Idempotency-Key") idempotencyKey: String,
    ): FavoritoItemDto

    @DELETE("favoritos/trabajador-oficio/{trabajadorOficioId}")
    suspend fun remove(
        @Path("trabajadorOficioId") trabajadorOficioId: Long,
    )
}
