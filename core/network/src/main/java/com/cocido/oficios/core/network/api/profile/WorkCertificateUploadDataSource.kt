package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class WorkCertificateUploadDataSource @Inject constructor(
    private val workCertificateApi: WorkCertificateApi,
) {
    suspend fun uploadCertificate(name: String, filePath: String): CertificadoTrabajoDto {
        val file = File(filePath)
        require(file.exists()) { "No se encontró el archivo del certificado" }
        val mediaType = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            else -> "image/*"
        }.toMediaTypeOrNull()
        val part = MultipartBody.Part.createFormData(
            "certificadoArchivo",
            file.name,
            file.asRequestBody(mediaType),
        )
        return workCertificateApi.uploadCertificate(
            nombre = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            certificadoArchivo = part,
        )
    }

    suspend fun updateCertificate(
        id: Long,
        name: String,
        currentUrl: String?,
        filePath: String?,
    ): CertificadoTrabajoDto {
        val filePart = filePath?.let { path ->
            val file = File(path)
            require(file.exists() && file.length() > 0L) { "No se encontró el archivo del certificado" }
            MultipartBody.Part.createFormData(
                "certificadoArchivoNuevo",
                file.name,
                file.asRequestBody(certificateMediaType(file)),
            )
        }
        return workCertificateApi.updateCertificate(
            id = id,
            nombre = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            certificadoArchivoNuevo = filePart,
            urlArchivoActual = if (filePart == null) {
                require(!currentUrl.isNullOrBlank()) { "Falta la URL actual del certificado" }
                currentUrl.toRequestBody("text/plain".toMediaTypeOrNull())
            } else {
                null
            },
        )
    }

    private fun certificateMediaType(file: File) = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        else -> "image/*"
    }.toMediaTypeOrNull()
}
