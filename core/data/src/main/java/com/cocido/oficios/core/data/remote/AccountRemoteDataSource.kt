package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.account.AccountApi
import com.cocido.oficios.core.network.api.account.AccountProfileUploadDataSource
import com.cocido.oficios.core.network.api.account.dto.ChangePasswordRequestDto
import com.cocido.oficios.core.network.safeApiCall
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRemoteDataSource @Inject constructor(
    private val api: AccountApi,
    private val profileUploadDataSource: AccountProfileUploadDataSource,
) {
    suspend fun getCurrentUser() = safeApiCall { api.getCurrentUser() }

    suspend fun updateProfile(request: UpdateAccountProfileRequest) = safeApiCall {
        profileUploadDataSource.updateUser(
            userId = request.userId,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            currentPhotoUrl = request.currentPhotoUrl,
            photoFilePath = request.photoFilePath,
            removePhoto = request.removePhoto,
        )
    }

    suspend fun changePassword(email: String, password: String, confirmation: String) = safeApiCall {
        api.changePassword(
            ChangePasswordRequestDto(
                email = email,
                contrasena = password,
                confirmarContrasena = confirmation,
            ),
        )
    }
}
