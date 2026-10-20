package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.oficiosTween

/** Fase visible de una pantalla con datos remotos. */
enum class OficiosContentPhase { Loading, Content, Empty, Error }

/** Para estados con flags: un error con datos ya cargados se muestra como snackbar, no como fase. */
fun oficiosContentPhase(isLoading: Boolean, hasError: Boolean, isEmpty: Boolean): OficiosContentPhase = when {
    isLoading -> OficiosContentPhase.Loading
    hasError && isEmpty -> OficiosContentPhase.Error
    isEmpty -> OficiosContentPhase.Empty
    else -> OficiosContentPhase.Content
}

/**
 * Fade de 200ms entre Loading / Content / Empty / Error.
 *
 * Solo anima cuando cambia [contentKey]: por defecto la clase del estado, así un
 * `Success(isRefreshing = true)` → `Success(...)` se recompone en el lugar.
 * El lambda recibe el estado de esa fase; usarlo en vez del estado externo para
 * que el contenido saliente no lea datos de la fase entrante.
 */
@Composable
fun <S : Any> OficiosStateContent(
    targetState: S,
    modifier: Modifier = Modifier,
    contentKey: (S) -> Any? = { it::class },
    label: String = "OficiosStateContent",
    content: @Composable AnimatedContentScope.(S) -> Unit,
) {
    val fade = oficiosTween<Float>(OficiosMotion.DurationMedium)
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { (fadeIn(fade) togetherWith fadeOut(fade)).using(null) },
        contentAlignment = Alignment.TopStart,
        contentKey = contentKey,
        label = label,
        content = content,
    )
}
