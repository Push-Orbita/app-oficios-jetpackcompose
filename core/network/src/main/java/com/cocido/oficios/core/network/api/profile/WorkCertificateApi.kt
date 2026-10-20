package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import kotlinx.serialization.json.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

interface WorkCertificateApi {

    @Multipart
    @POST("certificado-trabajo")
    suspend fun uploadCertificate(
        @Part("nombre") nombre: RequestBody,
        @Part certificadoArchivo: MultipartBody.Part,
    ): CertificadoTrabajoDto

    @GET("certificado-trabajo/search")
    suspend fun searchCertificates(
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageNumber") pageNumber: Int = 1,
    ): JsonElement

    @Multipart
    @PATCH("certificado-trabajo/{id}")
    suspend fun updateCertificate(
        @Path("id") id: Long,
        @Part("nombre") nombre: RequestBody? = null,
        @Part certificadoArchivoNuevo: MultipartBody.Part? = null,
        @Part("urlArchivoActual") urlArchivoActual: RequestBody? = null,
    ): CertificadoTrabajoDto

    @DELETE("certificado-trabajo/{id}")
    suspend fun deleteCertificate(@Path("id") id: Long): JsonElement
}
