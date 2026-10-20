package com.cocido.oficios.core.common.result

sealed interface AppError {
    val message: String
    val backendCode: String? get() = null

    data class Network(
        override val message: String,
        val code: Int? = null,
        override val backendCode: String? = null,
    ) : AppError

    data class Validation(
        override val message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
        override val backendCode: String? = null,
    ) : AppError

    data class Unauthorized(
        override val message: String,
        override val backendCode: String? = null,
    ) : AppError

    data class Forbidden(
        override val message: String,
        override val backendCode: String? = null,
    ) : AppError

    data class NotFound(
        override val message: String,
        override val backendCode: String? = null,
    ) : AppError

    data class Conflict(
        override val message: String,
        override val backendCode: String? = null,
    ) : AppError

    data class Unknown(
        override val message: String = "Unexpected error",
        override val backendCode: String? = null,
    ) : AppError
}
