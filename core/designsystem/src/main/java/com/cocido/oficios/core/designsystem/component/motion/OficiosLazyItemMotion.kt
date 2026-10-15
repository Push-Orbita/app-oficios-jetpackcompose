package com.cocido.oficios.core.designsystem.component.motion

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.cocido.oficios.core.designsystem.theme.LocalReducedMotion
import com.cocido.oficios.core.designsystem.theme.OficiosMotion

/**
 * [LazyItemScope.animateItem] con los specs de [OficiosMotion]: fade al entrar/salir y reubicación
 * suave al reordenar o filtrar. Con reduced motion los ítems aparecen y se reubican sin animar.
 */
@Composable
fun LazyItemScope.oficiosAnimateItem(modifier: Modifier = Modifier): Modifier {
    if (LocalReducedMotion.current) return modifier
    return modifier.animateItem(
        fadeInSpec = OficiosMotion.tween(OficiosMotion.DurationMedium, OficiosMotion.EmphasizedDecelerate),
        placementSpec = OficiosMotion.tween<IntOffset>(OficiosMotion.DurationLong),
        fadeOutSpec = OficiosMotion.tween(OficiosMotion.DurationShort, OficiosMotion.EmphasizedAccelerate),
    )
}
