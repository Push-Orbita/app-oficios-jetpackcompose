package com.cocido.oficios.core.common.result

sealed interface AppError {
    val message: String

    data class Network(
        override val message: String,
        val code: Int? = null,
    ) : AppError

    data class Validation(
        override val message: String,
    ) : AppError

    data class Unknown(
        override val message: String = "Unexpected error",
    ) : AppError
}
