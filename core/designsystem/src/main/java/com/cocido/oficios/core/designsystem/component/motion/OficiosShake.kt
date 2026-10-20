package com.cocido.oficios.core.designsystem.component.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.feedback.rememberOficiosHaptics
import com.cocido.oficios.core.designsystem.theme.LocalReducedMotion
import com.cocido.oficios.core.designsystem.theme.OficiosMotion

@Stable
class OficiosShakeState internal constructor() {
    internal val offsetX = Animatable(0f)
}

/**
 * Rechazo de un dato ingresado (código inválido): vibración de error y shake horizontal de
 * [OficiosMotion.DurationLong]. Se dispara cada vez que [rejectionKey] cambia a un valor no nulo;
 * con reduced motion queda solo el háptico.
 */
@Composable
fun rememberOficiosRejectShake(rejectionKey: Any?): OficiosShakeState {
    val state = remember { OficiosShakeState() }
    val haptics = rememberOficiosHaptics()
    val reducedMotion = LocalReducedMotion.current
    val amplitudePx = with(LocalDensity.current) { ShakeAmplitude.toPx() }
    LaunchedEffect(rejectionKey) {
        if (rejectionKey == null) return@LaunchedEffect
        haptics.reject()
        if (reducedMotion) return@LaunchedEffect
        state.offsetX.snapTo(0f)
        state.offsetX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = OficiosMotion.DurationLong
                -amplitudePx at 40
                amplitudePx at 90
                -amplitudePx * 0.7f at 140
                amplitudePx * 0.7f at 190
                -amplitudePx * 0.35f at 240
                amplitudePx * 0.35f at 270
            },
        )
    }
    return state
}

fun Modifier.oficiosShake(state: OficiosShakeState): Modifier =
    graphicsLayer { translationX = state.offsetX.value }

private val ShakeAmplitude = 8.dp
