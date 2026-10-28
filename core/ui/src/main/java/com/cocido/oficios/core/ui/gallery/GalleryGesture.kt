package com.cocido.oficios.core.ui.gallery

import kotlin.math.abs

data class GalleryPan(val x: Float, val y: Float)

object GalleryGesture {
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 4f
    const val REST_SCALE = 1.01f
    const val DISMISS_THRESHOLD_PX = 140f

    /** Un dedo en escala 1 se lo queda el pager. Pinch o pan ampliado sí se consumen. */
    fun shouldConsumeDrag(pointerCount: Int, scale: Float): Boolean =
        pointerCount >= 2 || scale > REST_SCALE

    /** Swipe vertical hacia abajo, a escala 1, cierra el visor. */
    fun shouldDismissDrag(pointerCount: Int, scale: Float, panX: Float, panY: Float): Boolean =
        pointerCount == 1 && scale <= REST_SCALE && panY > 0f && abs(panY) > abs(panX)

    fun shouldCloseFromDismiss(dismissY: Float, threshold: Float = DISMISS_THRESHOLD_PX): Boolean =
        dismissY >= threshold

    fun clampPan(
        x: Float,
        y: Float,
        scale: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ): GalleryPan {
        if (scale <= REST_SCALE || viewportWidth <= 0f || viewportHeight <= 0f) {
            return GalleryPan(0f, 0f)
        }
        val maxX = viewportWidth * (scale - 1f) / 2f
        val maxY = viewportHeight * (scale - 1f) / 2f
        return GalleryPan(
            x = x.coerceIn(-maxX, maxX),
            y = y.coerceIn(-maxY, maxY),
        )
    }

    fun initialPage(index: Int, size: Int): Int {
        if (size <= 0) return 0
        return index.coerceIn(0, size - 1)
    }
}
