package com.cocido.oficios.core.ui.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

/**
 * Recodifica SIEMPRE a JPEG ≤ [ImageUploadContract.MAX_EDGE_PX] y
 * [ImageUploadContract.TARGET_MAX_BYTES]. Un copy crudo de JPG/PNG/WEBP
 * (el camino anterior) era lo que disparaba el 413 de nginx.
 *
 * API 28+: [ImageDecoder] con ALLOCATOR_SOFTWARE y setTargetSize.
 * API 27: [BitmapFactory] abriendo el stream aparte del resultado de decode
 * (inJustDecodeBounds siempre devuelve null; no se puede usar `?: return`).
 * PNG/WEBP con alfa se aplanan sobre blanco. Se respeta orientación EXIF.
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
    require(file.exists() && file.length() > 0L) { ImageUploadContract.IMAGE_READ_FAILED_MESSAGE }
    val bitmap = decodeScaledBitmap(file)
        ?: throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    writeCompressedJpeg(context, bitmap, filePrefix)
}.getOrNull()

/**
 * JPEG listo para multipart **sin recortar**: escala ≤ [ImageUploadContract.MAX_EDGE_PX]
 * y comprime ≤ [ImageUploadContract.TARGET_MAX_BYTES], respetando EXIF.
 * Usado por selfie / foto de rostro para no abrir el cropper.
 */
fun prepareImageForUpload(
    context: Context,
    uri: Uri?,
    path: String?,
    filePrefix: String,
): String {
    return when {
        uri != null -> compressUriToJpegFile(context, uri, filePrefix)
        !path.isNullOrBlank() && File(path).exists() -> {
            compressImageFilePath(context, path, filePrefix)
                ?: throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
        }
        else -> throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    }
}

internal fun compressUriToJpegFile(
    context: Context,
    uri: Uri,
    filePrefix: String,
): String {
    val bitmap = decodeScaledBitmap(context, uri)
        ?: throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    return writeCompressedJpeg(context, bitmap, filePrefix)
}

internal fun decodeScaledBitmap(
    context: Context,
    uri: Uri,
    maxEdge: Int = ImageUploadContract.MAX_EDGE_PX,
): Bitmap? {
    val mime = context.contentResolver.openInputStream(uri)?.use { stream ->
        val header = ByteArray(16)
        val read = stream.read(header)
        if (read <= 0) null else ImageUploadContract.mimeFromMagicBytes(header.copyOf(read))
    }
    val allowDecoder = mime != "image/png" && mime != "image/webp"
    val fromDecoder = takeUsableDecoderBitmap(
        if (allowDecoder && Build.VERSION.SDK_INT >= 28) {
            decodeWithImageDecoder(context, uri, maxEdge)
        } else {
            null
        },
    )
    val decoded = if (fromDecoder != null) {
        applyExifIfDecoderDidNotRotate(context, uri, fromDecoder)
    } else {
        decodeWithBitmapFactory(context, uri, maxEdge)
    } ?: return null
    return flattenOnWhite(decoded)
}

internal fun decodeScaledBitmap(
    file: File,
    maxEdge: Int = ImageUploadContract.MAX_EDGE_PX,
): Bitmap? {
    val header = file.inputStream().use { stream ->
        val buffer = ByteArray(16)
        val read = stream.read(buffer)
        if (read <= 0) ByteArray(0) else buffer.copyOf(read)
    }
    val mime = ImageUploadContract.mimeFromMagicBytes(header)
    val allowDecoder = mime != "image/png" && mime != "image/webp"
    val fromDecoder = takeUsableDecoderBitmap(
        if (allowDecoder && Build.VERSION.SDK_INT >= 28) decodeFileWithImageDecoder(file, maxEdge) else null,
    )
    val decoded = if (fromDecoder != null) {
        file.inputStream().use { stream -> applyExifIfDecoderDidNotRotate(fromDecoder, stream, file) }
    } else {
        decodeFileWithBitmapFactory(file, maxEdge)
    } ?: return null
    return flattenOnWhite(decoded)
}

private fun takeUsableDecoderBitmap(decoded: Bitmap?): Bitmap? {
    if (decoded == null) return null
    if (!isVisuallyBlank(decoded)) return decoded
    if (!decoded.isRecycled) decoded.recycle()
    return null
}

private fun isVisuallyBlank(bitmap: Bitmap): Boolean {
    if (bitmap.width <= 0 || bitmap.height <= 0) return true
    val samples = intArrayOf(
        bitmap.getPixel(0, 0),
        bitmap.getPixel(bitmap.width - 1, 0),
        bitmap.getPixel(0, bitmap.height - 1),
        bitmap.getPixel(bitmap.width - 1, bitmap.height - 1),
        bitmap.getPixel(bitmap.width / 2, bitmap.height / 2),
    )
    return samples.all { pixel ->
        Color.red(pixel) == 0 && Color.green(pixel) == 0 && Color.blue(pixel) == 0
    }
}

@RequiresApi(28)
private fun decodeWithImageDecoder(context: Context, uri: Uri, maxEdge: Int): Bitmap? {
    if (Build.VERSION.SDK_INT < 28) return null
    return runCatching {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        decodeImageDecoderSource(source, maxEdge)
    }.getOrNull()
}

@RequiresApi(28)
private fun decodeFileWithImageDecoder(file: File, maxEdge: Int): Bitmap? {
    if (Build.VERSION.SDK_INT < 28) return null
    return runCatching {
        decodeImageDecoderSource(ImageDecoder.createSource(file), maxEdge)
    }.getOrNull()
}

@RequiresApi(28)
private fun decodeImageDecoderSource(source: ImageDecoder.Source, maxEdge: Int): Bitmap {
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val longest = maxOf(info.size.width, info.size.height).coerceAtLeast(1)
        if (longest > maxEdge) {
            val scale = maxEdge.toFloat() / longest
            decoder.setTargetSize(
                (info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1),
            )
        }
    }
}

private fun decodeWithBitmapFactory(context: Context, uri: Uri, maxEdge: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = context.contentResolver.openInputStream(uri) ?: return null
    boundsStream.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val decode = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxEdge)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val pixels = context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, decode)
    } ?: return null
    val oriented = context.contentResolver.openInputStream(uri)?.use { input ->
        applyExifOrientation(pixels, input)
    } ?: pixels
    return scaleDown(oriented, maxEdge)
}

private fun decodeFileWithBitmapFactory(file: File, maxEdge: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val decode = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxEdge)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val pixels = BitmapFactory.decodeFile(file.absolutePath, decode) ?: return null
    val oriented = file.inputStream().use { applyExifOrientation(pixels, it) }
    return scaleDown(oriented, maxEdge)
}

internal fun sampleSizeFor(width: Int, height: Int, maxEdge: Int = ImageUploadContract.MAX_EDGE_PX): Int {
    val longest = maxOf(width, height).coerceAtLeast(1)
    if (longest <= maxEdge) return 1
    var sample = 1
    while (longest / sample > maxEdge * 2) {
        sample *= 2
    }
    return sample
}

internal fun flattenOnWhite(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    for (index in pixels.indices) {
        val pixel = pixels[index]
        val alpha = Color.alpha(pixel)
        pixels[index] = when {
            alpha == 0 -> Color.WHITE
            alpha == 255 -> Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
            else -> Color.rgb(
                (Color.red(pixel) * alpha + 255 * (255 - alpha)) / 255,
                (Color.green(pixel) * alpha + 255 * (255 - alpha)) / 255,
                (Color.blue(pixel) * alpha + 255 * (255 - alpha)) / 255,
            )
        }
    }
    val flat = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    flat.setPixels(pixels, 0, width, 0, 0, width, height)
    if (flat !== source && !source.isRecycled) source.recycle()
    return flat
}

private fun applyExifIfDecoderDidNotRotate(context: Context, uri: Uri, decoded: Bitmap): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        applyExifIfDecoderDidNotRotate(decoded, stream, bounds.outWidth, bounds.outHeight)
    } ?: decoded
}

private fun applyExifIfDecoderDidNotRotate(decoded: Bitmap, stream: InputStream, file: File): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    return applyExifIfDecoderDidNotRotate(decoded, stream, bounds.outWidth, bounds.outHeight)
}

private fun applyExifIfDecoderDidNotRotate(
    decoded: Bitmap,
    stream: InputStream,
    storedWidth: Int,
    storedHeight: Int,
): Bitmap {
    val exif = ExifInterface(stream)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
    )
    val needsSwap = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
        orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
        orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
        orientation == ExifInterface.ORIENTATION_TRANSVERSE
    if (!needsSwap) return decoded
    val storedLandscape = storedWidth >= storedHeight
    val decodedLandscape = decoded.width >= decoded.height
    if (storedLandscape != decodedLandscape) return decoded
    val rotated = applyExifOrientation(decoded, orientation)
    return rotated
}

internal fun applyExifOrientation(bitmap: Bitmap, stream: InputStream): Bitmap {
    val exif = ExifInterface(stream)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
    )
    return applyExifOrientation(bitmap, orientation)
}

private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
    val rotated = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateSoftware(bitmap, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateSoftware(bitmap, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateSoftware(bitmap, 270)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipSoftware(bitmap, horizontal = true)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipSoftware(bitmap, horizontal = false)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            val mid = rotateSoftware(bitmap, 90)
            val flipped = flipSoftware(mid, horizontal = true)
            if (mid !== flipped && mid !== bitmap && !mid.isRecycled) mid.recycle()
            flipped
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            val mid = rotateSoftware(bitmap, 270)
            val flipped = flipSoftware(mid, horizontal = true)
            if (mid !== flipped && mid !== bitmap && !mid.isRecycled) mid.recycle()
            flipped
        }
        else -> return bitmap
    }
    if (rotated !== bitmap && !bitmap.isRecycled) bitmap.recycle()
    return rotated
}

internal fun rotateSoftware(source: Bitmap, degrees: Int): Bitmap {
    val turn = ((degrees % 360) + 360) % 360
    if (turn == 0) return source
    val width = source.width
    val height = source.height
    val (outWidth, outHeight) = if (turn == 180) width to height else height to width
    val out = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixel = source.getPixel(x, y)
            when (turn) {
                90 -> out.setPixel(height - 1 - y, x, pixel)
                180 -> out.setPixel(width - 1 - x, height - 1 - y, pixel)
                270 -> out.setPixel(y, width - 1 - x, pixel)
            }
        }
    }
    return out
}

private fun flipSoftware(source: Bitmap, horizontal: Boolean): Bitmap {
    val width = source.width
    val height = source.height
    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            val destX = if (horizontal) width - 1 - x else x
            val destY = if (horizontal) y else height - 1 - y
            out.setPixel(destX, destY, source.getPixel(x, y))
        }
    }
    return out
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

internal fun writeCompressedJpeg(context: Context, bitmap: Bitmap, filePrefix: String): String {
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
                throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
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
