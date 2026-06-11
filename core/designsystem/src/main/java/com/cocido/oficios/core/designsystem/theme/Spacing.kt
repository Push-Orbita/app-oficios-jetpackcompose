package com.cocido.oficios.core.designsystem.theme

import androidx.compose.ui.unit.dp

object OficiosSpacing {
    val none = 0.dp
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val formSection = 10.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
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
