package com.cocido.oficios.core.ui.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Recodifica SIEMPRE a JPEG ≤ [ImageUploadContract.MAX_EDGE_PX] y
 * [ImageUploadContract.TARGET_MAX_BYTES]. Un copy crudo de JPG/PNG/WEBP
 * (el camino anterior) era lo que disparaba el 413 de nginx.
 */
fun resolveUriToFilePath(
    context: Context,
    uri: Uri,
    filePrefix: String = "imagen",
): String? = runCatching {
    compressUriToJpegFile(context, uri, filePrefix)
}.getOrNull()

fun compressImageFilePath(
    context: Context,
    path: String,
    filePrefix: String = "imagen",
): String? = runCatching {
    val file = File(path)
    require(file.exists() && file.length() > 0L) { "empty image" }
    val bitmap = decodeScaledBitmap(file) ?: error("Cannot decode image")
    writeCompressedJpeg(context, bitmap, filePrefix)
}.getOrNull()

internal fun compressUriToJpegFile(
    context: Context,
    uri: Uri,
    filePrefix: String,
): String {
    val bitmap = decodeScaledBitmap(context, uri) ?: error("Cannot decode image")
    return writeCompressedJpeg(context, bitmap, filePrefix)
}

private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    } ?: return null
    val decode = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
    }
    return context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, decode)
    }?.let(::scaleDown)
}

private fun decodeScaledBitmap(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    val decode = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
    }
    return BitmapFactory.decodeFile(file.absolutePath, decode)?.let(::scaleDown)
}

private fun sampleSizeFor(width: Int, height: Int): Int {
    val longest = maxOf(width, height).coerceAtLeast(1)
    if (longest <= ImageUploadContract.MAX_EDGE_PX) return 1
    var sample = 1
    while (longest / sample > ImageUploadContract.MAX_EDGE_PX * 2) {
        sample *= 2
    }
    return sample
}

private fun scaleDown(source: Bitmap, maxEdge: Int = ImageUploadContract.MAX_EDGE_PX): Bitmap {
    val longest = maxOf(source.width, source.height)
    if (longest <= maxEdge) return source
    val scale = maxEdge.toFloat() / longest
    val scaled = Bitmap.createScaledBitmap(
        source,
        (source.width * scale).toInt().coerceAtLeast(1),
        (source.height * scale).toInt().coerceAtLeast(1),
        true,
    )
    if (scaled !== source) source.recycle()
    return scaled
}

private fun writeCompressedJpeg(context: Context, bitmap: Bitmap, filePrefix: String): String {
    val bytes = try {
        encodeUnderTarget(bitmap)
    } finally {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
    val tempFile = File.createTempFile("${filePrefix}_", ".jpg", context.cacheDir)
    tempFile.outputStream().use { it.write(bytes) }
    return tempFile.absolutePath
}

internal fun encodeUnderTarget(bitmap: Bitmap): ByteArray {
    var current = bitmap
    var produced: ByteArray? = null
    while (true) {
        for (quality in ImageUploadContract.jpegQualityLadder()) {
            val out = ByteArrayOutputStream()
            if (!current.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                error("Cannot compress image")
            }
            produced = out.toByteArray()
            if (!ImageUploadContract.exceedsImageTarget(produced.size.toLong())) {
                recycleIfNotOriginal(current, bitmap)
                return produced
            }
        }
        val longest = maxOf(current.width, current.height)
        if (longest <= 320) break
        val next = scaleDown(current, (longest * 3 / 4).coerceAtLeast(320))
        if (next === current) break
        recycleIfNotOriginal(current, bitmap)
        current = next
    }
    val last = produced
        ?: ByteArrayOutputStream().also { stream ->
            current.compress(Bitmap.CompressFormat.JPEG, ImageUploadContract.MIN_JPEG_QUALITY, stream)
        }.toByteArray()
    recycleIfNotOriginal(current, bitmap)
    return last
}

private fun recycleIfNotOriginal(current: Bitmap, original: Bitmap) {
    if (current !== original && !current.isRecycled) current.recycle()
}
