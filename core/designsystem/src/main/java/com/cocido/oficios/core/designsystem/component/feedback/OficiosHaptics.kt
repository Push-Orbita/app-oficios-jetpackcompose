package com.cocido.oficios.core.designsystem.component.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Vocabulario háptico de la app. Solo para momentos con significado:
 * [confirm] al cerrar una acción con éxito, [reject] ante un dato inválido y [tick] en toggles
 * y selecciones. La navegación común no vibra.
 */
@Stable
class OficiosHaptics internal constructor(private val view: View) {

    fun confirm() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        },
    )

    fun reject() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        },
    )

    fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK)

    private fun perform(feedbackConstant: Int) {
        view.performHapticFeedback(feedbackConstant)
    }
}

@Composable
fun rememberOficiosHaptics(): OficiosHaptics {
    val view = LocalView.current
    return remember(view) { OficiosHaptics(view) }
}
