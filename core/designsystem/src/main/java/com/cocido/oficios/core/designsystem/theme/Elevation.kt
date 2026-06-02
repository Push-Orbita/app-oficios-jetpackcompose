package com.cocido.oficios.core.designsystem.theme

import androidx.compose.ui.unit.dp

object OficiosElevation {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
}

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosElevation", replaceWith = ReplaceWith("OficiosElevation"))
object AppElevation {
    val level0 = OficiosElevation.none
    val level1 = OficiosElevation.sm
    val level2 = OficiosElevation.md
    val level3 = OficiosElevation.lg
    val level4 = OficiosElevation.lg
    val level5 = OficiosElevation.lg
}
