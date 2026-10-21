package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class WorkGalleryUploadDataSource @Inject constructor(
    private val api: WorkGalleryApi,
) {
    suspend fun upload(filePaths: List<String>): List<WorkGalleryDto> {
        require(filePaths.size in 1..5) { "Seleccione entre 1 y 5 fotos por carga." }
        val parts = filePaths.map { path ->
            val file = File(path)
            require(file.exists() && file.length() > 0L) { "Una de las fotos ya no está disponible." }
            MultipartBody.Part.createFormData(
                "fotos",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull()),
            )
        }
        return api.upload(parts)
    }
}
