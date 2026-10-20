package com.cocido.oficios.feature.home.worker

data class GalleryPan(val x: Float, val y: Float)

object GalleryGesture {
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 4f
    private const val REST_SCALE = 1.01f

    /** Un dedo en escala 1 se lo queda el pager. Pinch o pan ampliado sí se consumen. */
    fun shouldConsumeDrag(pointerCount: Int, scale: Float): Boolean =
        pointerCount >= 2 || scale > REST_SCALE

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
}
