package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.LoginResponseDto
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenResponseDto
import com.cocido.oficios.core.network.api.auth.dto.RolUsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.SignupResponseDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import kotlinx.serialization.json.JsonPrimitive

fun LoginResponseDto.toDomainSession(): UserSession =
    UserSession(tokens = toTokens(), user = usuario.toDomainUser())

fun SignupResponseDto.toDomainSession(): UserSession =
    UserSession(tokens = toTokens(), user = usuario.toDomainUser())

fun LoginResponseDto.toTokens(): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)

fun SignupResponseDto.toTokens(): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)

fun RefreshTokenResponseDto.toDomainTokens(): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)

fun UsuarioDto.toDomainUser(): User = User(
    id = id,
    email = email,
    firstName = persona?.nombres.orEmpty(),
    lastName = persona?.apellido.orEmpty(),
    role = UserRole.fromRoleName(rolUsuario?.nombre),
    emailVerified = emailVerificado,
    personaId = persona?.id,
    profilePhotoUrl = fotoPerfil?.let { element ->
        (element as? JsonPrimitive)?.content
    },
)

fun RolUsuarioDto.toDomainRoleDefinition(): UserRoleDefinition = UserRoleDefinition(
    id = id,
    name = nombre,
    role = UserRole.fromRoleName(nombre),
)
