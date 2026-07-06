package com.cocido.oficios.core.common.result

sealed interface AppError {
    val message: String

    data class Network(
        override val message: String,
        val code: Int? = null,
    ) : AppError

    data class Validation(
        override val message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : AppError

    data class Unauthorized(
        override val message: String,
    ) : AppError

    data class Forbidden(
        override val message: String,
    ) : AppError

    data class NotFound(
        override val message: String,
    ) : AppError

    data class Conflict(
        override val message: String,
    ) : AppError

    data class Unknown(
        override val message: String = "Unexpected error",
    ) : AppError
}
