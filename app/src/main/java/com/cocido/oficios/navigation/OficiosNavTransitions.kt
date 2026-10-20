package com.cocido.oficios.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.cocido.oficios.core.designsystem.theme.LocalReducedMotion
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.feature.home.navigation.isClientTabRoute
import com.cocido.oficios.feature.settings.navigation.SettingsDestination

private typealias NavTransitionScope = AnimatedContentTransitionScope<NavBackStackEntry>

/**
 * Push/pop: fade + slide horizontal corto (eje compartido M3, 300ms emphasized).
 * Cambio de tab de la bottom nav: solo fade de 200ms.
 * Sin animaciones del sistema: sin transición.
 */
internal class OficiosNavTransitions(
    private val reducedMotion: Boolean,
    private val slideOffsetPx: Int,
) {
    val enter: NavTransitionScope.() -> EnterTransition = {
        when {
            reducedMotion -> EnterTransition.None
            isTabSwitch() -> tabFadeIn()
            else -> pushIn(SlideDirection.Start)
        }
    }

    val exit: NavTransitionScope.() -> ExitTransition = {
        when {
            reducedMotion -> ExitTransition.None
            isTabSwitch() -> tabFadeOut()
            else -> pushOut(SlideDirection.Start)
        }
    }

    val popEnter: NavTransitionScope.() -> EnterTransition = {
        when {
            reducedMotion -> EnterTransition.None
            isTabSwitch() -> tabFadeIn()
            else -> pushIn(SlideDirection.End)
        }
    }

    val popExit: NavTransitionScope.() -> ExitTransition = {
        when {
            reducedMotion -> ExitTransition.None
            isTabSwitch() -> tabFadeOut()
            else -> pushOut(SlideDirection.End)
        }
    }

    private fun NavTransitionScope.isTabSwitch(): Boolean =
        isClientTabRoute(initialState.destination.route, SettingsDestination.route) &&
            isClientTabRoute(targetState.destination.route, SettingsDestination.route)

    private fun tabFadeIn(): EnterTransition =
        fadeIn(OficiosMotion.tween(OficiosMotion.DurationMedium))

    private fun tabFadeOut(): ExitTransition =
        fadeOut(OficiosMotion.tween(OficiosMotion.DurationMedium))

    private fun NavTransitionScope.pushIn(towards: SlideDirection): EnterTransition =
        fadeIn(OficiosMotion.tween(OficiosMotion.DurationLong, OficiosMotion.EmphasizedDecelerate)) +
            slideIntoContainer(
                towards = towards,
                animationSpec = OficiosMotion.tween<IntOffset>(OficiosMotion.DurationLong),
                initialOffset = ::shortSlide,
            )

    private fun NavTransitionScope.pushOut(towards: SlideDirection): ExitTransition =
        fadeOut(OficiosMotion.tween(OficiosMotion.DurationShort, OficiosMotion.EmphasizedAccelerate)) +
            slideOutOfContainer(
                towards = towards,
                animationSpec = OficiosMotion.tween<IntOffset>(OficiosMotion.DurationLong),
                targetOffset = ::shortSlide,
            )

    /** El contenedor propone un slide de pantalla completa; se conserva la dirección y se acorta. */
    private fun shortSlide(fullSlideOffset: Int): Int =
        if (fullSlideOffset < 0) -slideOffsetPx else slideOffsetPx
}

@Composable
internal fun rememberOficiosNavTransitions(): OficiosNavTransitions {
    val reducedMotion = LocalReducedMotion.current
    val slideOffsetPx = with(LocalDensity.current) { NavSlideOffset.roundToPx() }
    return remember(reducedMotion, slideOffsetPx) {
        OficiosNavTransitions(reducedMotion = reducedMotion, slideOffsetPx = slideOffsetPx)
    }
}

private val NavSlideOffset = 30.dp
