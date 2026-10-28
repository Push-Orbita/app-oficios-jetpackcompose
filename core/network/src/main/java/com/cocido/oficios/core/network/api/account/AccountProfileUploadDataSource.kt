package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class AccountProfileUploadDataSource @Inject constructor(
    private val api: AccountApi,
) {
    suspend fun updateUser(
        userId: Long,
        firstName: String,
        lastName: String,
        email: String,
        currentPhotoUrl: String?,
        photoFilePath: String?,
        removePhoto: Boolean,
    ): UsuarioDto {
        val photoPart = photoFilePath?.let { path ->
            val file = File(path)
            require(file.exists() && file.length() > 0L) { "La foto seleccionada no está disponible." }
            createProfilePhotoPart(file)
        }
        val photoUrlPart = when {
            photoPart != null -> null
            removePhoto -> "".toRequestBody(TEXT_MEDIA_TYPE)
            !currentPhotoUrl.isNullOrBlank() -> currentPhotoUrl.toRequestBody(TEXT_MEDIA_TYPE)
            else -> null
        }
        return api.updateUser(
            userId = userId,
            firstName = firstName.toRequestBody(TEXT_MEDIA_TYPE),
            lastName = lastName.toRequestBody(TEXT_MEDIA_TYPE),
            email = email.toRequestBody(TEXT_MEDIA_TYPE),
            profilePhoto = photoPart,
            currentPhotoUrl = photoUrlPart,
        )
    }

    private companion object {
        val TEXT_MEDIA_TYPE = "text/plain".toMediaTypeOrNull()
    }
}
