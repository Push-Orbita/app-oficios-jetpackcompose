package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Estado visible de una foto de identidad (frente, dorso o selfie).
 *
 * La foto elegida en el dispositivo ([ReadyToSend]) siempre gana sobre lo que
 * ya está en el servidor: el usuario puede reemplazarla todas las veces que
 * quiera y nada se sube hasta que toca el CTA principal.
 */
enum class DocumentSlotStatus {
    /** Sin foto local ni remota. */
    Empty,

    /** Foto elegida en el dispositivo, todavía no enviada. */
    ReadyToSend,

    /** Enviada; el equipo la está revisando. Se puede reemplazar. */
    InReview,

    /** Rechazada por el equipo; hay que cargar otra. */
    Rejected,

    /** Aprobada; el backend no permite reemplazarla. */
    Approved,
    ;

    val hasImage: Boolean get() = this != Empty
    val canReplace: Boolean get() = this != Approved
}

/**
 * Una foto de identidad lista para dibujar: estado + lo que se muestra como
 * miniatura (ruta local si hay una elegida, si no la URL remota).
 */
data class DocumentSlot(
    val status: DocumentSlotStatus,
    val previewModel: String?,
)

internal fun documentSlotOf(
    localPath: String?,
    remoteLoaded: Boolean,
    remoteUrl: String?,
    remoteStatus: String?,
): DocumentSlot {
    val status = when {
        localPath != null -> DocumentSlotStatus.ReadyToSend
        !remoteLoaded -> DocumentSlotStatus.Empty
        remoteStatus.isDocumentStatus(DOCUMENT_STATUS_APPROVED) -> DocumentSlotStatus.Approved
        remoteStatus.isDocumentStatus(DOCUMENT_STATUS_REJECTED) -> DocumentSlotStatus.Rejected
        else -> DocumentSlotStatus.InReview
    }
    return DocumentSlot(
        status = status,
        previewModel = localPath ?: remoteUrl?.takeIf { it.isNotBlank() },
    )
}

internal const val DOCUMENT_STATUS_UPLOADED = "SUBIDO"
internal const val DOCUMENT_STATUS_APPROVED = "APROBADO"
internal const val DOCUMENT_STATUS_REJECTED = "RECHAZADO"

internal fun String?.isDocumentStatus(expected: String): Boolean =
    this?.equals(expected, ignoreCase = true) == true

/**
 * Tope de un envío de documentación completo (número + fotos). OkHttp tiene
 * timeouts por request pero no por cadena de requests; sin este tope un
 * PATCH + DELETE + POST multipart con red mala deja el CTA girando minutos.
 */
internal const val DOCUMENT_SUBMIT_TIMEOUT_MS = 90_000L

/**
 * Corre un envío y garantiza que termine en un [AppResult]: timeout o
 * excepción inesperada se convierten en error para que la UI salga del
 * estado de carga y el usuario pueda reintentar.
 */
internal suspend fun <T> guardedSubmit(
    timeoutMs: Long = DOCUMENT_SUBMIT_TIMEOUT_MS,
    block: suspend () -> AppResult<T>,
): AppResult<T> = try {
    withTimeout(timeoutMs) { block() }
} catch (timeout: TimeoutCancellationException) {
    AppResult.Error(AppError.Network(message = SUBMIT_TIMEOUT_MESSAGE))
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    AppResult.Error(error.toAppError())
}

internal const val SUBMIT_TIMEOUT_MESSAGE =
    "El envío está tardando demasiado. Revise su conexión y vuelva a intentarlo."