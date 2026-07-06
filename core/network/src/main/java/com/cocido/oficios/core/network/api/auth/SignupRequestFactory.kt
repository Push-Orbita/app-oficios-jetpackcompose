package com.cocido.oficios.core.network.api.auth

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object SignupRequestFactory {
    private val textType = "text/plain".toMediaType()

    fun create(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        roleId: Long,
        document: String?,
    ): SignupMultipartParts = SignupMultipartParts(
        firstName = firstName.toRequestBody(textType),
        lastName = lastName.toRequestBody(textType),
        email = email.toRequestBody(textType),
        password = password.toRequestBody(textType),
        roleId = roleId.toString().toRequestBody(textType),
        document = document?.toRequestBody(textType),
    )
}

data class SignupMultipartParts(
    val firstName: RequestBody,
    val lastName: RequestBody,
    val email: RequestBody,
    val password: RequestBody,
    val roleId: RequestBody,
    val document: RequestBody?,
)
