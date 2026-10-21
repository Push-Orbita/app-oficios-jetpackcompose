package com.cocido.oficios.core.ui.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.common.result.ClientValidationException
import com.cocido.oficios.core.ui.file.decodeScaledBitmap
import com.cocido.oficios.core.ui.file.rotateSoftware
import com.cocido.oficios.core.ui.file.writeCompressedJpeg
import java.io.File

fun prepareImageForCrop(
    context: Context,
    uri: Uri,
    filePrefix: String = "recorte",
): String {
    val bitmap = decodeScaledBitmap(context, uri)
        ?: throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    return writeWorkingJpeg(context, bitmap, filePrefix)
}

fun prepareImageForCropFromPath(
    context: Context,
    path: String,
    filePrefix: String = "recorte",
): String {
    val file = File(path)
    val bitmap = decodeScaledBitmap(file)
        ?: throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    return writeWorkingJpeg(context, bitmap, filePrefix)
}

fun applyImageCrop(
    context: Context,
    sourcePath: String,
    transform: ImageCropTransform,
    viewportWidth: Float,
    viewportHeight: Float,
    filePrefix: String,
): String {
    val original = BitmapFactory.decodeFile(sourcePath)
        ?: throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    val crop = bitmapCropRect(original.width, original.height, transform, viewportWidth, viewportHeight)
    val rotated = rotateSoftware(original, transform.rotationDegrees)
    if (rotated !== original && !original.isRecycled) original.recycle()
    val safe = PixelRect(
        left = crop.left.coerceIn(0, rotated.width - 1),
        top = crop.top.coerceIn(0, rotated.height - 1),
        right = crop.right.coerceIn(1, rotated.width),
        bottom = crop.bottom.coerceIn(1, rotated.height),
    )
    val cropped = Bitmap.createBitmap(rotated, safe.left, safe.top, safe.width, safe.height)
    if (cropped !== rotated && !rotated.isRecycled) rotated.recycle()
    return writeCompressedJpeg(context, cropped, filePrefix)
}

private fun writeWorkingJpeg(context: Context, bitmap: Bitmap, filePrefix: String): String {
    val file = File.createTempFile("${filePrefix}_edit_", ".jpg", context.cacheDir)
    file.outputStream().use { stream ->
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)) {
            bitmap.recycle()
            throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
        }
    }
    if (!bitmap.isRecycled) bitmap.recycle()
    return file.absolutePath
}
