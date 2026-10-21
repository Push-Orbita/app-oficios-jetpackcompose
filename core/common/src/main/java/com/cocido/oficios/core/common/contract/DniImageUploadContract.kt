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

    val ACCEPTED_EXTENSIONS = ImageUploadContract.ACCEPTED_EXTENSIONS
    val ACCEPTED_MIME_TYPES = ImageUploadContract.ACCEPTED_MIME_TYPES
    val ACCEPTED_FORMATS_LABEL = ImageUploadContract.ACCEPTED_FORMATS_LABEL

    fun isAcceptedExtension(extension: String): Boolean =
        ImageUploadContract.isAcceptedExtension(extension)

    fun normalizeExtension(extension: String): String =
        ImageUploadContract.normalizeExtension(extension)

    fun mimeTypeForExtension(extension: String): String =
        ImageUploadContract.mimeTypeForExtension(extension)
}
