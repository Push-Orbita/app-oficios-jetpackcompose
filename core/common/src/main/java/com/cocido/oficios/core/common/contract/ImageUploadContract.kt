package com.cocido.oficios.core.common.contract

import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File

/**
 * Formatos de imagen que el backend acepta en galería, DNI, selfie, certificado y foto de perfil.
 * El MIME genérico image/asterisco no es válido: hay que mandar image/jpeg, png, webp o gif.
 */
object ImageUploadContract {
    const val ACCEPTED_FORMATS_LABEL = "JPG, PNG, WEBP, GIF"
    const val GENERIC_IMAGE_MIME = "image/*"

    val ACCEPTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")

    val ACCEPTED_MIME_TYPES = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
    )

    fun isAcceptedExtension(extension: String): Boolean =
        extension.lowercase() in ACCEPTED_EXTENSIONS

    fun normalizeExtension(extension: String): String = when (extension.lowercase()) {
        "jpeg" -> "jpg"
        else -> extension.lowercase()
    }

    fun mimeTypeForExtension(extension: String): String = when (normalizeExtension(extension)) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "image/jpeg"
    }

    fun extensionForMime(mime: String): String? = when (normalizeMime(mime)) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> null
    }

    fun normalizeMime(raw: String?): String? {
        val value = raw?.substringBefore(';')?.trim()?.lowercase().orEmpty()
        if (value.isBlank() || value == GENERIC_IMAGE_MIME) return null
        return when (value) {
            "image/jpg", "image/pjpeg" -> "image/jpeg"
            else -> value.takeIf { it in ACCEPTED_MIME_TYPES }
        }
    }

    fun mimeFromMagicBytes(bytes: ByteArray): String? {
        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
        ) {
            return "image/jpeg"
        }
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte()
        ) {
            return "image/png"
        }
        if (bytes.size >= 6 &&
            bytes[0] == 'G'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte()
        ) {
            return "image/gif"
        }
        if (bytes.size >= 12 &&
            bytes.copyOfRange(0, 4).contentEquals("RIFF".toByteArray()) &&
            bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray())
        ) {
            return "image/webp"
        }
        return null
    }

    fun detectMime(file: File, declaredMime: String? = null): String? {
        val header = readFileHeader(file)
        val fromMagic = mimeFromMagicBytes(header)
        if (fromMagic != null) return fromMagic
        val fromDeclared = normalizeMime(declaredMime)
        if (fromDeclared != null) return fromDeclared
        if (header.isNotEmpty()) return null
        return file.extension.takeIf { isAcceptedExtension(it) }?.let(::mimeTypeForExtension)
    }

    fun requireAcceptedMime(file: File, declaredMime: String? = null): String {
        if (!file.exists() || file.length() == 0L) {
            throw ClientValidationException("La foto seleccionada no está disponible. Vuelva a elegirla.")
        }
        return detectMime(file, declaredMime)
            ?: throw ClientValidationException(
                "Formato no válido. Aceptados: $ACCEPTED_FORMATS_LABEL",
            )
    }

    fun galleryFileName(mime: String, index: Int = 0): String =
        "galeria_${System.currentTimeMillis()}_$index.${extensionForMime(mime) ?: "jpg"}"

    fun profileFileName(mime: String): String =
        "perfil_${System.currentTimeMillis()}.${extensionForMime(mime) ?: "jpg"}"

    fun certificateFileName(mime: String, originalName: String): String {
        val ext = if (mime == "application/pdf") {
            "pdf"
        } else {
            extensionForMime(mime) ?: originalName.substringAfterLast('.', "jpg")
        }
        return "certificado_${System.currentTimeMillis()}.$ext"
    }

    /** [InputStream.readNBytes] es API 33; minSdk 27 lee el encabezado a mano. */
    private fun readFileHeader(file: File, size: Int = 16): ByteArray {
        if (!file.exists() || file.length() == 0L) return ByteArray(0)
        return file.inputStream().use { stream ->
            val buffer = ByteArray(size)
            val read = stream.read(buffer)
            if (read <= 0) ByteArray(0) else buffer.copyOf(read)
        }
    }
}
