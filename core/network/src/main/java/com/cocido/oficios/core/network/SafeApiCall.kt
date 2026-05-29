package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.toAppError
import retrofit2.HttpException

suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (exception: HttpException) {
    AppResult.Error(
        AppError.Network(
            message = exception.message() ?: "HTTP error",
            code = exception.code(),
        ),
    )
} catch (throwable: Throwable) {
    AppResult.Error(throwable.toAppError())
}
