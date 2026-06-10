package com.cocido.oficios.domain.model

data class User(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val emailVerified: Boolean,
    val profilePhotoUrl: String? = null,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: String? = null,
)

data class UserSession(
    val tokens: AuthTokens,
    val user: User,
)
