package com.cocido.oficios.core.designsystem.component.interaction

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize

/**
 * Expande el área táctil/semántica a 48 dp sin pintar el extra.
 * El hijo conserva el tamaño visual de Figma; el layout mínimo es el touch target WCAG.
 */
fun Modifier.oficiosMinTouchTarget(): Modifier = sizeIn(
    minWidth = OficiosComponentSize.minTouchTarget,
    minHeight = OficiosComponentSize.minTouchTarget,
)
