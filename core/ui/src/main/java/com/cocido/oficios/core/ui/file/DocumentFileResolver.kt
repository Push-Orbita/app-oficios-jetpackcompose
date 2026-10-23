package com.cocido.oficios.core.ui.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.File

private val ACCEPTED_CERTIFICATE_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png", "webp")

sealed interface CertificateFilePick {
    data class Ready(
        val path: String,
        val displayName: String,
        val sizeBytes: Long,
        val isPdf: Boolean,
    ) : CertificateFilePick

    data class Rejected(val message: String) : CertificateFilePick
}

/**
 * Imagen: JPEG comprimido bajo [ImageUploadContract.TARGET_MAX_BYTES].
 * PDF: se copia tal cual; si supera ~1 MB se rechaza con aviso claro.
 */
fun pickCertificateFile(context: Context, uri: Uri): CertificateFilePick = runCatching {
    val displayName = queryDisplayName(context, uri) ?: "certificado"
    val mime = context.contentResolver.getType(uri).orEmpty().lowercase()
    val extension = displayName.substringAfterLast('.', "").lowercase().ifBlank {
        when {
            mime == "application/pdf" -> "pdf"
            mime == "image/jpeg" -> "jpg"
            mime == "image/png" -> "png"
            mime == "image/webp" -> "webp"
            else -> ""
        }
    }
    val header = readHeader(context, uri)
    val magicMime = ImageUploadContract.mimeFromMagicBytes(header)
    val declaredPdf = mime == "application/pdf" || extension == "pdf"
    val declaredImage = ImageUploadContract.normalizeMime(mime) != null ||
        extension in setOf("jpg", "jpeg", "png", "webp")

    when {
        ImageUploadContract.isPdfMagic(header) || (declaredPdf && magicMime == null) ->
            copyPdfUnderLimit(context, uri, displayName)
        magicMime != null || declaredImage -> {
            val path = compressUriToJpegFile(context, uri, "certificate")
            val file = File(path)
            CertificateFilePick.Ready(
                path = path,
                displayName = displayName.substringBeforeLast('.', displayName) + ".jpg",
                sizeBytes = file.length(),
                isPdf = false,
            )
        }
        extension in ACCEPTED_CERTIFICATE_EXTENSIONS -> error("No se pudo leer el archivo.")
        else -> CertificateFilePick.Rejected("El certificado debe ser PDF, JPG, PNG o WEBP.")
    }
}.getOrElse { throwable ->
    CertificateFilePick.Rejected(ImageUploadContract.userFacingFileFailure(throwable))
}

/** @deprecated Prefer [pickCertificateFile] para mostrar el motivo del rechazo. */
fun resolveCertificateUriToFilePath(context: Context, uri: Uri): String? =
    (pickCertificateFile(context, uri) as? CertificateFilePick.Ready)?.path

private fun copyPdfUnderLimit(
    context: Context,
    uri: Uri,
    displayName: String,
): CertificateFilePick {
    val declaredSize = querySize(context, uri)
    if (declaredSize != null && ImageUploadContract.exceedsPdfLimit(declaredSize)) {
        return CertificateFilePick.Rejected(ImageUploadContract.PDF_TOO_LARGE_MESSAGE)
    }
    val output = File.createTempFile("certificate_", ".pdf", context.cacheDir)
    context.contentResolver.openInputStream(uri)?.use { input ->
        output.outputStream().use { stream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (ImageUploadContract.exceedsPdfLimit(total)) {
                    output.delete()
                    return CertificateFilePick.Rejected(ImageUploadContract.PDF_TOO_LARGE_MESSAGE)
                }
                stream.write(buffer, 0, read)
            }
            if (total <= 0L) {
                output.delete()
                return CertificateFilePick.Rejected("El PDF está vacío. Elija otro archivo.")
            }
        }
    } ?: return CertificateFilePick.Rejected("No se pudo abrir el archivo")
    return CertificateFilePick.Ready(
        path = output.absolutePath,
        displayName = displayName,
        sizeBytes = output.length(),
        isPdf = true,
    )
}

private fun queryDisplayName(context: Context, uri: Uri): String? =
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

private fun querySize(context: Context, uri: Uri): Long? =
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
        }

private fun readHeader(context: Context, uri: Uri, size: Int = 16): ByteArray {
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        val buffer = ByteArray(size)
        val read = stream.read(buffer)
        if (read <= 0) ByteArray(0) else buffer.copyOf(read)
    } ?: ByteArray(0)
}
