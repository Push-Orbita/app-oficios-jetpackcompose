package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.account.createCertificateFilePart
import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class WorkCertificateUploadDataSource @Inject constructor(
    private val workCertificateApi: WorkCertificateApi,
) {
    suspend fun uploadCertificate(name: String, filePath: String): CertificadoTrabajoDto {
        val file = File(filePath)
        require(file.exists()) { "No se encontró el archivo del certificado" }
        return workCertificateApi.uploadCertificate(
            nombre = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            certificadoArchivo = createCertificateFilePart("certificadoArchivo", file),
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
            createCertificateFilePart("certificadoArchivoNuevo", file)
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
}
