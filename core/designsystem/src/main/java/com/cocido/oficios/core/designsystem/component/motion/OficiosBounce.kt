package com.cocido.oficios.core.designsystem.component.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.cocido.oficios.core.designsystem.theme.LocalReducedMotion
import com.cocido.oficios.core.designsystem.theme.OficiosMotion

@Stable
class OficiosBounceState internal constructor() {
    internal val scale = Animatable(1f)
    internal var isFirstRun = true
}

/**
 * Rebote de [OficiosMotion.DurationMedium] para confirmar una selección (favorito, estrella).
 * Se dispara cuando [selectionKey] cambia a un valor no nulo, nunca en la composición inicial;
 * con reduced motion no anima.
 */
@Composable
fun rememberOficiosBounce(selectionKey: Any?): OficiosBounceState {
    val state = remember { OficiosBounceState() }
    val reducedMotion = LocalReducedMotion.current
    LaunchedEffect(selectionKey) {
        if (state.isFirstRun) {
            state.isFirstRun = false
            return@LaunchedEffect
        }
        if (selectionKey == null || reducedMotion) return@LaunchedEffect
        state.scale.snapTo(1f)
        state.scale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = OficiosMotion.DurationMedium
                BounceScale at 80 using OficiosMotion.EmphasizedDecelerate
            },
        )
    }
    return state
}

fun Modifier.oficiosBounce(state: OficiosBounceState): Modifier =
    graphicsLayer {
        scaleX = state.scale.value
        scaleY = state.scale.value
    }

private const val BounceScale = 1.2f
