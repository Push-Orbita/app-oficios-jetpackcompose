package com.cocido.oficios.core.ui.crop

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Cálculo puro de recorte/rotación. El bitmap se rota primero y el recorte
 * vive en el espacio ya rotado, cubriendo la ventana con pan/pinza.
 */
enum class ImageCropAspect {
    Square,
    Gallery43,
    Free,
}

enum class ImageCropMode {
    Profile,
    Gallery,
    Certificate,
    Document,
}

fun ImageCropMode.defaultAspect(): ImageCropAspect = when (this) {
    ImageCropMode.Profile -> ImageCropAspect.Square
    ImageCropMode.Gallery -> ImageCropAspect.Gallery43
    ImageCropMode.Certificate, ImageCropMode.Document -> ImageCropAspect.Free
}

fun ImageCropMode.circularMask(): Boolean = this == ImageCropMode.Profile

fun ImageCropMode.allowsFreeAspect(): Boolean = this == ImageCropMode.Gallery

data class PixelRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int get() = (right - left).coerceAtLeast(1)
    val height: Int get() = (bottom - top).coerceAtLeast(1)
}

data class ImageCropTransform(
    val rotationDegrees: Int = 0,
    val scale: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val aspect: ImageCropAspect = ImageCropAspect.Free,
)

fun rotateClockwise(degrees: Int): Int = (degrees + 90) % 360

fun rotatedSize(width: Int, height: Int, rotationDegrees: Int): Pair<Int, Int> {
    return if (rotationDegrees % 180 == 0) width to height else height to width
}

fun aspectRatio(aspect: ImageCropAspect, viewportWidth: Float, viewportHeight: Float): Float {
    return when (aspect) {
        ImageCropAspect.Square -> 1f
        ImageCropAspect.Gallery43 -> 4f / 3f
        ImageCropAspect.Free -> (viewportWidth / viewportHeight.coerceAtLeast(1f)).coerceAtLeast(0.05f)
    }
}

fun cropWindow(
    viewportWidth: Float,
    viewportHeight: Float,
    aspect: ImageCropAspect,
    insetFraction: Float = 0.08f,
): PixelRect {
    val availableW = viewportWidth * (1f - insetFraction * 2f)
    val availableH = viewportHeight * (1f - insetFraction * 2f)
    val ratio = aspectRatio(aspect, availableW, availableH)
    val windowH: Float
    val windowW: Float
    if (availableW / availableH > ratio) {
        windowH = availableH
        windowW = windowH * ratio
    } else {
        windowW = availableW
        windowH = windowW / ratio
    }
    val left = ((viewportWidth - windowW) / 2f).roundToInt()
    val top = ((viewportHeight - windowH) / 2f).roundToInt()
    return PixelRect(
        left = left,
        top = top,
        right = left + windowW.roundToInt().coerceAtLeast(1),
        bottom = top + windowH.roundToInt().coerceAtLeast(1),
    )
}

fun coverScale(
    rotatedWidth: Int,
    rotatedHeight: Int,
    cropWidth: Float,
    cropHeight: Float,
): Float {
    val widthScale = cropWidth / rotatedWidth.coerceAtLeast(1).toFloat()
    val heightScale = cropHeight / rotatedHeight.coerceAtLeast(1).toFloat()
    return maxOf(widthScale, heightScale)
}

fun clampScale(scale: Float): Float = scale.coerceIn(1f, 8f)

fun clampPan(
    panX: Float,
    panY: Float,
    rotatedWidth: Int,
    rotatedHeight: Int,
    crop: PixelRect,
    scale: Float,
): Pair<Float, Float> {
    val drawScale = coverScale(rotatedWidth, rotatedHeight, crop.width.toFloat(), crop.height.toFloat()) *
        clampScale(scale)
    val displayedW = rotatedWidth * drawScale
    val displayedH = rotatedHeight * drawScale
    val cropCx = (crop.left + crop.right) / 2f
    val cropCy = (crop.top + crop.bottom) / 2f
    val minLeft = crop.right - displayedW
    val maxLeft = crop.left.toFloat()
    val minTop = crop.bottom - displayedH
    val maxTop = crop.top.toFloat()
    val unclampedLeft = cropCx - displayedW / 2f + panX
    val unclampedTop = cropCy - displayedH / 2f + panY
    val left = unclampedLeft.coerceIn(min(minLeft, maxLeft), max(minLeft, maxLeft))
    val top = unclampedTop.coerceIn(min(minTop, maxTop), max(minTop, maxTop))
    return (left - (cropCx - displayedW / 2f)) to (top - (cropCy - displayedH / 2f))
}

fun bitmapCropRect(
    imageWidth: Int,
    imageHeight: Int,
    transform: ImageCropTransform,
    viewportWidth: Float,
    viewportHeight: Float,
): PixelRect {
    val (rotatedW, rotatedH) = rotatedSize(imageWidth, imageHeight, transform.rotationDegrees)
    val crop = cropWindow(viewportWidth, viewportHeight, transform.aspect)
    val (panX, panY) = clampPan(
        transform.panX,
        transform.panY,
        rotatedW,
        rotatedH,
        crop,
        transform.scale,
    )
    val drawScale = coverScale(rotatedW, rotatedH, crop.width.toFloat(), crop.height.toFloat()) *
        clampScale(transform.scale)
    val displayedW = rotatedW * drawScale
    val displayedH = rotatedH * drawScale
    val cropCx = (crop.left + crop.right) / 2f
    val cropCy = (crop.top + crop.bottom) / 2f
    val imgLeft = cropCx - displayedW / 2f + panX
    val imgTop = cropCy - displayedH / 2f + panY
    val srcLeft = ((crop.left - imgLeft) / drawScale).roundToInt().coerceIn(0, rotatedW - 1)
    val srcTop = ((crop.top - imgTop) / drawScale).roundToInt().coerceIn(0, rotatedH - 1)
    val srcRight = ((crop.right - imgLeft) / drawScale).roundToInt().coerceIn(srcLeft + 1, rotatedW)
    val srcBottom = ((crop.bottom - imgTop) / drawScale).roundToInt().coerceIn(srcTop + 1, rotatedH)
    return PixelRect(srcLeft, srcTop, srcRight, srcBottom)
}
