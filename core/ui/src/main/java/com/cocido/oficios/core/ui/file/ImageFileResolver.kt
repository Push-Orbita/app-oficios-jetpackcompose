package com.cocido.oficios.core.ui.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.File

private const val MAX_EDGE_PX = 2048
private const val JPEG_QUALITY = 85

fun resolveUriToFilePath(
    context: Context,
    uri: Uri,
    filePrefix: String = "imagen",
): String? = runCatching {
    val declaredMime = context.contentResolver.getType(uri)
    val header = context.contentResolver.openInputStream(uri)?.use { stream ->
        val buffer = ByteArray(16)
        val read = stream.read(buffer)
        if (read <= 0) ByteArray(0) else buffer.copyOf(read)
    } ?: ByteArray(0)
    val mime = ImageUploadContract.mimeFromMagicBytes(header)
        ?: ImageUploadContract.normalizeMime(declaredMime)
    val extension = mime?.let(ImageUploadContract::extensionForMime)

    if (extension != null) {
        copyUriToCacheFile(context, uri, filePrefix, extension)
    } else {
        convertUriToJpegFile(context, uri, filePrefix)
    }
}.getOrNull()

private fun copyUriToCacheFile(
    context: Context,
    uri: Uri,
    filePrefix: String,
    extension: String,
): String {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Cannot open uri")
    val tempFile = File.createTempFile("${filePrefix}_", ".$extension", context.cacheDir)
    tempFile.outputStream().use { output -> inputStream.use { it.copyTo(output) } }
    return tempFile.absolutePath
}

private fun convertUriToJpegFile(context: Context, uri: Uri, filePrefix: String): String {
    val original = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inJustDecodeBounds = false })
    } ?: error("Cannot decode image")
    val bitmap = scaleDown(original)
    val tempFile = File.createTempFile("${filePrefix}_", ".jpg", context.cacheDir)
    tempFile.outputStream().use { output ->
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
            error("Cannot compress image")
        }
    }
    if (bitmap !== original) original.recycle()
    return tempFile.absolutePath
}

private fun scaleDown(source: Bitmap): Bitmap {
    val longest = maxOf(source.width, source.height)
    if (longest <= MAX_EDGE_PX) return source
    val scale = MAX_EDGE_PX.toFloat() / longest
    return Bitmap.createScaledBitmap(
        source,
        (source.width * scale).toInt().coerceAtLeast(1),
        (source.height * scale).toInt().coerceAtLeast(1),
        true,
    )
}
