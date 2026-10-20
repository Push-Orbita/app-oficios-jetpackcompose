package com.cocido.oficios.core.ui.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

private const val MAX_DOCUMENT_BYTES = 10L * 1024L * 1024L
private val ACCEPTED_CERTIFICATE_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png", "webp")

/** Copies a user-selected certificate into app cache after validating type and size. */
fun resolveCertificateUriToFilePath(context: Context, uri: Uri): String? = runCatching {
    val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    val mime = context.contentResolver.getType(uri).orEmpty().lowercase()
    val extension = displayName?.substringAfterLast('.', "")?.lowercase().orEmpty().ifBlank {
        when (mime) {
            "application/pdf" -> "pdf"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> ""
        }
    }
    require(extension in ACCEPTED_CERTIFICATE_EXTENSIONS) {
        "El certificado debe ser PDF, JPG, PNG o WEBP."
    }
    val output = File.createTempFile("certificate_", ".$extension", context.cacheDir)
    context.contentResolver.openInputStream(uri)?.use { input ->
        output.outputStream().use { stream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= MAX_DOCUMENT_BYTES) { "El certificado supera el máximo de 10 MB." }
                stream.write(buffer, 0, read)
            }
        }
    } ?: error("No se pudo abrir el archivo")
    output.absolutePath
}.getOrNull()
