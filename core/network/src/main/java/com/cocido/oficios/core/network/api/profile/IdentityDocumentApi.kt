package com.cocido.oficios.core.network.api.profile

import kotlinx.serialization.json.JsonElement
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface IdentityDocumentApi {

    @GET("img-documento/search")
    suspend fun searchDocuments(
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("tipoDocumento") tipoDocumento: String? = null,
    ): JsonElement

    @Multipart
    @POST("img-documento/subir-documento-dni")
    suspend fun uploadDniDocuments(
        @Part frenteArchivo: MultipartBody.Part,
        @Part dorsoArchivo: MultipartBody.Part,
    ): JsonElement

    @Multipart
    @POST("img-documento/subir-documento-selfie")
    suspend fun uploadSelfie(
        @Part selfieArchivo: MultipartBody.Part,
    ): JsonElement

    @Multipart
    @PATCH("img-documento/{id}/reemplazar-selfie")
    suspend fun replaceSelfie(
        @Path("id") documentId: Long,
        @Part selfieArchivo: MultipartBody.Part,
    ): JsonElement
}
