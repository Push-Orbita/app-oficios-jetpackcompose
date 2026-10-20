package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.ui.geometry.Rect

/**
 * Rectángulo para [BringIntoViewRequester] que prioriza el campo enfocado (con margen)
 * por encima de mostrar el CTA del bloque cuando el viewport es chico (teclado abierto).
 */
internal fun fieldFocusedBringIntoViewRect(
    blockWidth: Int,
    blockHeight: Int,
    fieldTopPx: Float,
    fieldHeightPx: Float,
    fieldMarginPx: Float,
    viewportHeightPx: Int,
): Rect {
    val width = blockWidth.toFloat().coerceAtLeast(0f)
    val bottom = blockHeight.toFloat().coerceAtLeast(0f)
    if (blockWidth <= 0 || blockHeight <= 0) {
        return Rect(0f, 0f, width, bottom.coerceAtLeast(1f))
    }

    val fieldTop = fieldTopPx.coerceIn(0f, bottom)
    val fieldBottom = (fieldTop + fieldHeightPx.coerceAtLeast(0f)).coerceIn(fieldTop, bottom)
    val focusTop = (fieldTop - fieldMarginPx).coerceAtLeast(0f)
    val focusBottom = (fieldBottom + fieldMarginPx).coerceAtMost(bottom)
    val focusHeight = (focusBottom - focusTop).coerceAtLeast(1f)

    val viewport = viewportHeightPx
    if (viewport <= 1) {
        return Rect(0f, focusTop, width, focusBottom)
    }

    val viewportF = (viewport - 1).toFloat()
    if (focusHeight <= viewportF) {
        return Rect(0f, focusTop, width, focusBottom)
    }

    return Rect(0f, focusTop, width, focusTop + viewportF)
}
