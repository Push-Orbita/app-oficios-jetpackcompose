package com.cocido.oficios.domain.model

data class RegisterClientRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val document: String,
)

data class RegisterProviderRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val document: String,
)
