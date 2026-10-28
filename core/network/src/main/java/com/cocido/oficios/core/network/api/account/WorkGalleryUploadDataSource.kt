package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkGalleryUploadDataSource @Inject constructor(
    private val api: WorkGalleryApi,
) {
    suspend fun upload(filePaths: List<String>): List<WorkGalleryDto> {
        require(filePaths.size in 1..5) { "Seleccione entre 1 y 5 fotos por carga." }
        val parts = filePaths.mapIndexed { index, path ->
            val file = File(path)
            require(file.exists() && file.length() > 0L) { "Una de las fotos ya no está disponible." }
            createGalleryPhotoPart(file, index)
        }
        return api.upload(parts)
    }
}
