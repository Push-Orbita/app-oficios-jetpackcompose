package com.cocido.oficios.core.ui.crop

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Recorte en espacio de la imagen ya rotada. La UI muestra la foto con
 * ContentScale.Fit; el marco vive encima y nunca sale de la imagen.
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

enum class CropHandle {
    Left,
    Right,
    Top,
    Bottom,
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}

val CropHandle.affectsLeft: Boolean
    get() = this == CropHandle.Left || this == CropHandle.TopLeft || this == CropHandle.BottomLeft

val CropHandle.affectsRight: Boolean
    get() = this == CropHandle.Right || this == CropHandle.TopRight || this == CropHandle.BottomRight

val CropHandle.affectsTop: Boolean
    get() = this == CropHandle.Top || this == CropHandle.TopLeft || this == CropHandle.TopRight

val CropHandle.affectsBottom: Boolean
    get() = this == CropHandle.Bottom || this == CropHandle.BottomLeft || this == CropHandle.BottomRight

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

    fun contains(x: Float, y: Float): Boolean =
        x >= left && x <= right && y >= top && y <= bottom
}

data class FittedImageLayout(
    val drawScale: Float,
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

data class ImageCropTransform(
    val rotationDegrees: Int = 0,
    val scale: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val aspect: ImageCropAspect = ImageCropAspect.Free,
    val crop: PixelRect? = null,
)

fun rotateClockwise(degrees: Int): Int = (degrees + 90) % 360

fun rotatedSize(width: Int, height: Int, rotationDegrees: Int): Pair<Int, Int> {
    return if (rotationDegrees % 180 == 0) width to height else height to width
}

fun lockedRatio(aspect: ImageCropAspect): Float? = when (aspect) {
    ImageCropAspect.Square -> 1f
    ImageCropAspect.Gallery43 -> 4f / 3f
    ImageCropAspect.Free -> null
}

fun cropMinSize(imageWidth: Int, imageHeight: Int): Int {
    val shortest = min(imageWidth, imageHeight).coerceAtLeast(1)
    return max(32, (shortest * 0.12f).roundToInt())
}

fun initialCropRect(rotatedWidth: Int, rotatedHeight: Int, aspect: ImageCropAspect): PixelRect {
    val width = rotatedWidth.coerceAtLeast(1)
    val height = rotatedHeight.coerceAtLeast(1)
    val ratio = lockedRatio(aspect) ?: return PixelRect(0, 0, width, height)
    return maxCenteredAspectRect(width, height, ratio)
}

fun maxCenteredAspectRect(imageWidth: Int, imageHeight: Int, ratio: Float): PixelRect {
    val width = imageWidth.coerceAtLeast(1)
    val height = imageHeight.coerceAtLeast(1)
    val imageRatio = width.toFloat() / height
    val cropW: Int
    val cropH: Int
    if (imageRatio > ratio) {
        cropH = height
        cropW = (height * ratio).roundToInt().coerceIn(1, width)
    } else {
        cropW = width
        cropH = (width / ratio).roundToInt().coerceIn(1, height)
    }
    val left = (width - cropW) / 2
    val top = (height - cropH) / 2
    return PixelRect(left, top, left + cropW, top + cropH)
}

fun cropAfterRotation(
    imageWidth: Int,
    imageHeight: Int,
    rotationDegrees: Int,
    aspect: ImageCropAspect,
): PixelRect {
    val (rotatedW, rotatedH) = rotatedSize(imageWidth, imageHeight, rotationDegrees)
    return initialCropRect(rotatedW, rotatedH, aspect)
}

fun clampUserScale(scale: Float): Float = scale.coerceIn(1f, 8f)

fun fittedImageLayout(
    viewportWidth: Float,
    viewportHeight: Float,
    rotatedWidth: Int,
    rotatedHeight: Int,
    userScale: Float,
    panX: Float,
    panY: Float,
    padding: Float,
): FittedImageLayout {
    val availW = (viewportWidth - padding * 2f).coerceAtLeast(1f)
    val availH = (viewportHeight - padding * 2f).coerceAtLeast(1f)
    val fit = min(
        availW / rotatedWidth.coerceAtLeast(1).toFloat(),
        availH / rotatedHeight.coerceAtLeast(1).toFloat(),
    )
    val drawScale = fit * clampUserScale(userScale)
    val displayedW = rotatedWidth * drawScale
    val displayedH = rotatedHeight * drawScale
    val centeredLeft = (viewportWidth - displayedW) / 2f
    val centeredTop = (viewportHeight - displayedH) / 2f
    val left = if (displayedW <= viewportWidth) {
        centeredLeft
    } else {
        (centeredLeft + panX).coerceIn(viewportWidth - displayedW, 0f)
    }
    val top = if (displayedH <= viewportHeight) {
        centeredTop
    } else {
        (centeredTop + panY).coerceIn(viewportHeight - displayedH, 0f)
    }
    return FittedImageLayout(drawScale, left, top, displayedW, displayedH)
}

fun imageToScreenX(imageX: Float, layout: FittedImageLayout): Float =
    layout.left + imageX * layout.drawScale

fun imageToScreenY(imageY: Float, layout: FittedImageLayout): Float =
    layout.top + imageY * layout.drawScale

fun screenToImageX(screenX: Float, layout: FittedImageLayout): Float =
    (screenX - layout.left) / layout.drawScale.coerceAtLeast(0.0001f)

fun screenToImageY(screenY: Float, layout: FittedImageLayout): Float =
    (screenY - layout.top) / layout.drawScale.coerceAtLeast(0.0001f)

fun imageRectToScreen(crop: PixelRect, layout: FittedImageLayout): PixelRect {
    return PixelRect(
        left = imageToScreenX(crop.left.toFloat(), layout).roundToInt(),
        top = imageToScreenY(crop.top.toFloat(), layout).roundToInt(),
        right = imageToScreenX(crop.right.toFloat(), layout).roundToInt(),
        bottom = imageToScreenY(crop.bottom.toFloat(), layout).roundToInt(),
    )
}

fun bitmapCropRect(
    imageWidth: Int,
    imageHeight: Int,
    transform: ImageCropTransform,
): PixelRect {
    val (rotatedW, rotatedH) = rotatedSize(imageWidth, imageHeight, transform.rotationDegrees)
    val crop = transform.crop ?: initialCropRect(rotatedW, rotatedH, transform.aspect)
    return clampCropToImage(crop, rotatedW, rotatedH, minSize = 1)
}

fun clampCropToImage(
    crop: PixelRect,
    imageWidth: Int,
    imageHeight: Int,
    minSize: Int = 1,
): PixelRect {
    val min = minSize.coerceAtLeast(1).coerceAtMost(min(imageWidth, imageHeight).coerceAtLeast(1))
    val maxRight = imageWidth.coerceAtLeast(1)
    val maxBottom = imageHeight.coerceAtLeast(1)
    val left = crop.left.coerceIn(0, (maxRight - min).coerceAtLeast(0))
    val top = crop.top.coerceIn(0, (maxBottom - min).coerceAtLeast(0))
    val right = crop.right.coerceIn(left + min, maxRight)
    val bottom = crop.bottom.coerceIn(top + min, maxBottom)
    return PixelRect(left, top, right, bottom)
}

fun moveCrop(
    crop: PixelRect,
    deltaX: Float,
    deltaY: Float,
    imageWidth: Int,
    imageHeight: Int,
): PixelRect {
    val width = crop.width
    val height = crop.height
    val maxLeft = (imageWidth - width).coerceAtLeast(0)
    val maxTop = (imageHeight - height).coerceAtLeast(0)
    val left = (crop.left + deltaX).roundToInt().coerceIn(0, maxLeft)
    val top = (crop.top + deltaY).roundToInt().coerceIn(0, maxTop)
    return PixelRect(left, top, left + width, top + height)
}

fun resizeCrop(
    crop: PixelRect,
    handle: CropHandle,
    pointerImageX: Float,
    pointerImageY: Float,
    imageWidth: Int,
    imageHeight: Int,
    aspect: ImageCropAspect,
    minSize: Int,
): PixelRect {
    val min = minSize.coerceAtLeast(1)
    val ratio = lockedRatio(aspect)
    return if (ratio == null) {
        resizeFree(crop, handle, pointerImageX, pointerImageY, imageWidth, imageHeight, min)
    } else {
        resizeLocked(crop, handle, pointerImageX, pointerImageY, imageWidth, imageHeight, ratio, min)
    }
}

fun hitCropHandle(
    screenX: Float,
    screenY: Float,
    crop: PixelRect,
    layout: FittedImageLayout,
    touchRadius: Float,
): CropHandle? {
    val left = imageToScreenX(crop.left.toFloat(), layout)
    val top = imageToScreenY(crop.top.toFloat(), layout)
    val right = imageToScreenX(crop.right.toFloat(), layout)
    val bottom = imageToScreenY(crop.bottom.toFloat(), layout)
    val cx = (left + right) / 2f
    val cy = (top + bottom) / 2f
    val points = listOf(
        CropHandle.TopLeft to (left to top),
        CropHandle.TopRight to (right to top),
        CropHandle.BottomLeft to (left to bottom),
        CropHandle.BottomRight to (right to bottom),
        CropHandle.Top to (cx to top),
        CropHandle.Bottom to (cx to bottom),
        CropHandle.Left to (left to cy),
        CropHandle.Right to (right to cy),
    )
    var best: CropHandle? = null
    var bestDist = touchRadius
    for ((handle, point) in points) {
        val dx = screenX - point.first
        val dy = screenY - point.second
        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
        if (dist <= bestDist) {
            best = handle
            bestDist = dist
        }
    }
    return best
}

private fun resizeFree(
    crop: PixelRect,
    handle: CropHandle,
    pointerX: Float,
    pointerY: Float,
    imageWidth: Int,
    imageHeight: Int,
    minSize: Int,
): PixelRect {
    var left = crop.left.toFloat()
    var top = crop.top.toFloat()
    var right = crop.right.toFloat()
    var bottom = crop.bottom.toFloat()
    val px = pointerX.coerceIn(0f, imageWidth.toFloat())
    val py = pointerY.coerceIn(0f, imageHeight.toFloat())
    if (handle.affectsLeft) left = px.coerceIn(0f, right - minSize)
    if (handle.affectsRight) right = px.coerceIn(left + minSize, imageWidth.toFloat())
    if (handle.affectsTop) top = py.coerceIn(0f, bottom - minSize)
    if (handle.affectsBottom) bottom = py.coerceIn(top + minSize, imageHeight.toFloat())
    return PixelRect(
        left.roundToInt(),
        top.roundToInt(),
        right.roundToInt(),
        bottom.roundToInt(),
    )
}

private fun resizeLocked(
    crop: PixelRect,
    handle: CropHandle,
    pointerX: Float,
    pointerY: Float,
    imageWidth: Int,
    imageHeight: Int,
    ratio: Float,
    minSize: Int,
): PixelRect {
    val growLeft = handle.affectsLeft
    val growRight = handle.affectsRight
    val growUp = handle.affectsTop
    val growDown = handle.affectsBottom
    val anchorX = when {
        growLeft -> crop.right.toFloat()
        growRight -> crop.left.toFloat()
        else -> (crop.left + crop.right) / 2f
    }
    val anchorY = when {
        growUp -> crop.bottom.toFloat()
        growDown -> crop.top.toFloat()
        else -> (crop.top + crop.bottom) / 2f
    }
    val maxW = when {
        growLeft && !growRight -> anchorX
        growRight && !growLeft -> imageWidth - anchorX
        else -> 2f * min(anchorX, imageWidth - anchorX)
    }.coerceAtLeast(minSize.toFloat())
    val maxH = when {
        growUp && !growDown -> anchorY
        growDown && !growUp -> imageHeight - anchorY
        else -> 2f * min(anchorY, imageHeight - anchorY)
    }.coerceAtLeast(minSize.toFloat())
    val maxWidth = min(maxW, maxH * ratio)
    val desiredWidth = when {
        growLeft || growRight -> abs(pointerX - anchorX)
        else -> abs(pointerY - anchorY) * ratio
    }
    val width = desiredWidth.coerceIn(minSize.toFloat(), maxWidth)
    val height = width / ratio
    val left: Float
    val right: Float
    val top: Float
    val bottom: Float
    if (growLeft && !growRight) {
        right = anchorX
        left = anchorX - width
    } else if (growRight && !growLeft) {
        left = anchorX
        right = anchorX + width
    } else {
        left = anchorX - width / 2f
        right = anchorX + width / 2f
    }
    if (growUp && !growDown) {
        bottom = anchorY
        top = anchorY - height
    } else if (growDown && !growUp) {
        top = anchorY
        bottom = anchorY + height
    } else {
        top = anchorY - height / 2f
        bottom = anchorY + height / 2f
    }
    return clampCropToImage(
        PixelRect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt()),
        imageWidth,
        imageHeight,
        minSize,
    )
}
