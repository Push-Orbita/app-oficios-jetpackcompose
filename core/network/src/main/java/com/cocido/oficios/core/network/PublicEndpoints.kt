package com.cocido.oficios.core.network

object PublicEndpoints {
    val paths = setOf(
        "auth/login",
        "auth/signup",
        "auth/refresh",
        "auth/password-reset/request-code",
        "auth/password-reset/verify-code",
    )

    fun isPublic(path: String): Boolean = paths.any { path.contains(it) }
}
