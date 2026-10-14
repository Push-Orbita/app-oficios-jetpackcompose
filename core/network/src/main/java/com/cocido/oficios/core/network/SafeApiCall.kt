package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.result.AppResult
import java.util.concurrent.CancellationException

suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    AppResult.Error(throwable.toNetworkAppError())
}
