package com.cocido.oficios.core.designsystem.theme

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring as composeSpring
import androidx.compose.animation.core.tween as composeTween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

// ---------------------------------------------------------------------------
// OficiosMotion — duraciones, curvas y specs únicos de la app
//
// Ninguna animación supera DurationLong. Con "Quitar animaciones" del sistema
// (ANIMATOR_DURATION_SCALE = 0) las specs devuelven duración 0 / snap.
// ---------------------------------------------------------------------------

object OficiosMotion {
    const val DurationShort = 100
    const val DurationMedium = 200
    const val DurationLong = 300

    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> tween(
        durationMillis: Int = DurationMedium,
        easing: Easing = Emphasized,
        reducedMotion: Boolean = false,
    ): FiniteAnimationSpec<T> = composeTween(
        durationMillis = if (reducedMotion) 0 else durationMillis,
        easing = easing,
    )

    /** Spring sin rebote para cambios de tamaño/posición que no tienen duración fija. */
    fun <T> spring(
        reducedMotion: Boolean = false,
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = Spring.StiffnessMediumLow,
    ): FiniteAnimationSpec<T> = if (reducedMotion) {
        snap()
    } else {
        composeSpring(dampingRatio = dampingRatio, stiffness = stiffness)
    }
}

/** `true` cuando el usuario desactivó las animaciones del sistema. */
val LocalReducedMotion = compositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun <T> oficiosTween(
    durationMillis: Int = OficiosMotion.DurationMedium,
    easing: Easing = OficiosMotion.Emphasized,
): FiniteAnimationSpec<T> = OficiosMotion.tween(durationMillis, easing, LocalReducedMotion.current)

@Composable
@ReadOnlyComposable
fun <T> oficiosSpring(
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMediumLow,
): FiniteAnimationSpec<T> = OficiosMotion.spring(LocalReducedMotion.current, dampingRatio, stiffness)

/** Lee ANIMATOR_DURATION_SCALE y se actualiza si el usuario lo cambia con la app abierta. */
@Composable
internal fun rememberSystemReducedMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    var reduced by remember { mutableStateOf(context.isAnimatorDurationScaleZero()) }
    DisposableEffect(context) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = context.isAnimatorDurationScaleZero()
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        reduced = context.isAnimatorDurationScaleZero()
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}

private fun Context.isAnimatorDurationScaleZero(): Boolean = runCatching {
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}.getOrDefault(false)
