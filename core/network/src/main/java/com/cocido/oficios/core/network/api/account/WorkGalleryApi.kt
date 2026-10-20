package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import kotlinx.serialization.json.JsonElement
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Path

interface WorkGalleryApi {
    @GET("galeria-trabajo/search")
    suspend fun search(
        @Query("trabajadorOficioId") workerId: Long,
        @Query("pageSize") pageSize: Int = 100,
        @Query("pageNumber") pageNumber: Int = 1,
    ): JsonElement

    @Multipart
    @POST("galeria-trabajo/subir-fotos")
    suspend fun upload(@Part photos: List<MultipartBody.Part>): List<WorkGalleryDto>

    @DELETE("galeria-trabajo/{id}")
    suspend fun delete(@Path("id") id: Long): JsonElement
}
