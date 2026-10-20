package com.cocido.oficios.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object OficiosSpacing {
    val none = 0.dp
    val xxxs = 2.dp
    val xxs = 4.dp
    /** 6dp — usado en Figma para gaps pequeños */
    val xss = 6.dp
    val xs = 8.dp
    /** 10dp — muy usado en Figma para padding vertical de componentes */
    val s10 = 10.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
    
    /** Padding del contenedor de pantalla según Figma: pt-16 px-32 pb-32 */
    val OficiosScreenPadding = PaddingValues(
        start = 32.dp,
        top = 16.dp,
        end = 32.dp,
        bottom = 32.dp
    )
    
    /** Gap vertical entre secciones de pantalla — Figma gap-16 */
    val screenGap = 16.dp
}

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosSpacing", replaceWith = ReplaceWith("OficiosSpacing"))
object AppSpacing {
    val xxs = OficiosSpacing.xxxs
    val xs = OficiosSpacing.xxs
    val sm = OficiosSpacing.xs
    val md = OficiosSpacing.sm
    val lg = OficiosSpacing.md
    val xl = OficiosSpacing.lg
    val xxl = OficiosSpacing.xl
    val xxxl = OficiosSpacing.xxl
}
