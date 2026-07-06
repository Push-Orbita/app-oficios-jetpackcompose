package com.cocido.oficios.core.common.contract

/**
 * Contrato del endpoint `POST img-documento/subir-documento-dni`.
 * Mantener alineado con la validación del backend (VA01).
 */
object DniImageUploadContract {
    const val ENDPOINT_PATH = "img-documento/subir-documento-dni"
    const val PART_FRONT_FIELD = "frenteArchivo"
    const val PART_BACK_FIELD = "dorsoArchivo"
    const val FRONT_FILE_PREFIX = "dni_frente"
    const val BACK_FILE_PREFIX = "dni_dorso"

    val ACCEPTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")

    val ACCEPTED_MIME_TYPES = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
    )

    val ACCEPTED_FORMATS_LABEL = "JPG, PNG, WEBP, GIF"

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
}
