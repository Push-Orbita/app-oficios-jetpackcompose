package com.cocido.oficios.core.ui.map

internal const val MAP_LOAD_TIMEOUT_MILLIS = 15_000L

data class MapLoadState(
    val generation: Int = 0,
    val loaded: Boolean = false,
    val unavailable: Boolean = false,
) {
    val showGoogleMap: Boolean get() = !unavailable
    val showSpinner: Boolean get() = !loaded && !unavailable
    val showFallback: Boolean get() = unavailable && !loaded

    fun onLoaded(): MapLoadState = copy(loaded = true)

    fun onTimeout(): MapLoadState = if (loaded) this else copy(unavailable = true)

    fun retry(apiKeyBlank: Boolean): MapLoadState = MapLoadState(
        generation = generation + 1,
        unavailable = apiKeyBlank,
    )
}

fun shouldStartMapTimeout(apiKeyBlank: Boolean, visibleOnScreen: Boolean): Boolean =
    !apiKeyBlank && visibleOnScreen

fun isMapOnScreen(
    boundsLeft: Float,
    boundsTop: Float,
    boundsRight: Float,
    boundsBottom: Float,
    viewportWidth: Float,
    viewportHeight: Float,
): Boolean {
    if (viewportWidth <= 0f || viewportHeight <= 0f) return false
    val width = boundsRight - boundsLeft
    val height = boundsBottom - boundsTop
    if (width <= 0f || height <= 0f) return false
    return boundsRight > 0f &&
        boundsLeft < viewportWidth &&
        boundsBottom > 0f &&
        boundsTop < viewportHeight
}
