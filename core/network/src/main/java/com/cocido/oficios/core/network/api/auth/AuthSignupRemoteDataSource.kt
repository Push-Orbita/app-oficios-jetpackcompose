package com.cocido.oficios.core.network.api.auth

import com.cocido.oficios.core.network.api.auth.dto.SignupResponseDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSignupRemoteDataSource @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        roleId: Long,
        document: String?,
    ): SignupResponseDto {
        val parts = SignupRequestFactory.create(firstName, lastName, email, password, roleId, document)
        return authApi.signup(
            firstName = parts.firstName,
            lastName = parts.lastName,
            email = parts.email,
            password = parts.password,
            roleId = parts.roleId,
            document = parts.document,
        )
    }
}
