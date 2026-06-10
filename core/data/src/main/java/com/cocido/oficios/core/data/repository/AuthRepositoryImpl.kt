package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.mapper.toDomainRoleDefinition
import com.cocido.oficios.core.data.mapper.toDomainSession
import com.cocido.oficios.core.data.mapper.toDomainTokens
import com.cocido.oficios.core.data.mapper.toDomainUser
import com.cocido.oficios.core.data.remote.AuthRemoteDataSource
import com.cocido.oficios.core.data.session.SessionManager
import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionManager: SessionManager,
    private val tokenDataStore: TokenDataStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<UserSession> =
        when (val result = remoteDataSource.login(email, password)) {
            is AppResult.Success -> {
                val session = result.data.toDomainSession()
                sessionManager.saveSession(session.tokens, session.user)
                AppResult.Success(session)
            }
            is AppResult.Error -> result
        }

    override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> {
        val roleId = resolveRoleId(UserRole.CLIENT) ?: return roleNotFoundError()
        return register(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = request.password,
            roleId = roleId,
            document = request.document,
        )
    }

    override suspend fun registerProvider(request: RegisterProviderRequest): AppResult<UserSession> {
        val roleId = resolveRoleId(UserRole.PROVIDER) ?: return roleNotFoundError()
        return register(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = request.password,
            roleId = roleId,
            document = request.document,
        )
    }

    override suspend fun refreshToken(refreshToken: String): AppResult<AuthTokens> =
        when (val result = remoteDataSource.refreshToken(refreshToken)) {
            is AppResult.Success -> {
                val tokens = result.data.toDomainTokens()
                sessionManager.updateTokens(tokens)
                AppResult.Success(tokens)
            }
            is AppResult.Error -> result
        }

    override suspend fun getCurrentUser(): AppResult<User> =
        when (val result = remoteDataSource.getCurrentUser()) {
            is AppResult.Success -> AppResult.Success(result.data.toDomainUser())
            is AppResult.Error -> result
        }

    override suspend fun logout(): AppResult<Unit> {
        val result = remoteDataSource.logout()
        sessionManager.clearSession()
        return when (result) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> {
                sessionManager.clearSession()
                AppResult.Success(Unit)
            }
        }
    }

    override suspend fun requestPasswordReset(email: String): AppResult<Unit> =
        when (val result = remoteDataSource.requestPasswordReset(email)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> result
        }

    override suspend fun verifyPasswordResetCode(code: String): AppResult<String> =
        remoteDataSource.verifyPasswordResetCode(code)

    override suspend fun resetPassword(
        resetToken: String,
        password: String,
        confirmPassword: String,
    ): AppResult<Unit> = when (
        val result = remoteDataSource.resetPassword(resetToken, password, confirmPassword)
    ) {
        is AppResult.Success -> AppResult.Success(Unit)
        is AppResult.Error -> result
    }

    override suspend fun getUserRoles(): AppResult<List<UserRoleDefinition>> =
        when (val result = remoteDataSource.getUserRoles()) {
            is AppResult.Success -> {
                val roles = result.data.map { it.toDomainRoleDefinition() }
                cacheRoleIds(roles)
                AppResult.Success(roles)
            }
            is AppResult.Error -> result
        }

    private suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        roleId: Long,
        document: String?,
    ): AppResult<UserSession> = when (
        val result = remoteDataSource.signup(firstName, lastName, email, password, roleId, document)
    ) {
        is AppResult.Success -> {
            val session = result.data.toDomainSession()
            sessionManager.saveSession(session.tokens, session.user)
            AppResult.Success(session)
        }
        is AppResult.Error -> result
    }

    private suspend fun resolveRoleId(role: UserRole): Long? {
        tokenDataStore.getClientRoleId()?.takeIf { role == UserRole.CLIENT }?.let { return it }
        tokenDataStore.getProviderRoleId()?.takeIf { role == UserRole.PROVIDER }?.let { return it }

        return when (val rolesResult = getUserRoles()) {
            is AppResult.Success -> rolesResult.data.firstOrNull { it.role == role }?.id
            is AppResult.Error -> null
        }
    }

    private fun cacheRoleIds(roles: List<UserRoleDefinition>) {
        tokenDataStore.saveRoleIds(
            clientRoleId = roles.firstOrNull { it.role == UserRole.CLIENT }?.id,
            providerRoleId = roles.firstOrNull { it.role == UserRole.PROVIDER }?.id,
        )
    }

    private fun roleNotFoundError(): AppResult.Error =
        AppResult.Error(
            com.cocido.oficios.core.common.result.AppError.Unknown(
                "No se pudo determinar el rol de usuario. Intentá más tarde.",
            ),
        )
}
