package com.cocido.oficios.core.ui.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cocido.oficios.core.common.contract.DniImageUploadContract
import java.io.File

fun resolveUriToFilePath(context: Context, uri: Uri): String? = runCatching {
    val mimeType = context.contentResolver.getType(uri)?.lowercase()
    val extension = mimeType?.let(::mimeTypeToExtension)

    if (extension != null && DniImageUploadContract.isAcceptedExtension(extension)) {
        copyUriToCacheFile(context, uri, DniImageUploadContract.normalizeExtension(extension))
    } else {
        convertUriToJpegFile(context, uri)
    }
}.getOrNull()

private fun copyUriToCacheFile(context: Context, uri: Uri, extension: String): String {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Cannot open uri")
    val tempFile = File.createTempFile("dni_", ".$extension", context.cacheDir)
    tempFile.outputStream().use { output -> inputStream.use { it.copyTo(output) } }
    return tempFile.absolutePath
}

private fun convertUriToJpegFile(context: Context, uri: Uri): String {
    val bitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        ?: error("Cannot decode image")
    val tempFile = File.createTempFile("dni_", ".jpg", context.cacheDir)
    tempFile.outputStream().use { output ->
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
            error("Cannot compress image")
        }
    }
    return tempFile.absolutePath
}

private fun mimeTypeToExtension(mimeType: String): String? = when (mimeType) {
    in DniImageUploadContract.ACCEPTED_MIME_TYPES -> when (mimeType) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> null
    }
    else -> null
}
