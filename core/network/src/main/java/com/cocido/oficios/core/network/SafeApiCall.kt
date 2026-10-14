package com.cocido.oficios.core.network

import android.util.Log
import com.cocido.oficios.core.common.result.AppResult
import java.util.concurrent.CancellationException
import kotlinx.serialization.SerializationException

suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    if (throwable is SerializationException) {
        Log.e("OficiosApi", "No se pudo interpretar la respuesta", throwable)
    }
    AppResult.Error(throwable.toNetworkAppError())
}
