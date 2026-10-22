package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.LocalReducedMotion
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosSkeleton — placeholder de carga con forma del contenido final
//
// El shimmer es un pulso de opacidad (sin gradiente). Con animaciones del
// sistema desactivadas queda estático.
// ---------------------------------------------------------------------------

@Composable
fun OficiosSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(OficiosRadius.md),
) {
    val pulse = rememberSkeletonPulse()
    Spacer(
        modifier = modifier
            .graphicsLayer { alpha = pulse.value }
            .background(color = MaterialTheme.colorScheme.outline, shape = shape),
    )
}

/** Línea de texto simulada. */
@Composable
fun OficiosSkeletonLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = OficiosSpacing.sm,
) {
    OficiosSkeleton(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        shape = RoundedCornerShape(OficiosRadius.xs),
    )
}

/** Card de lista: avatar opcional + líneas, con el mismo marco que [OficiosCard]. */
@Composable
fun OficiosSkeletonCard(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    lines: Int = 2,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showAvatar) {
                    OficiosSkeleton(
                        modifier = Modifier.size(OficiosComponentSize.avatarSm),
                        shape = CircleShape,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    OficiosSkeletonLine(widthFraction = 0.6f, height = OficiosComponentSize.iconSm)
                    OficiosSkeletonLine(widthFraction = 0.4f)
                }
            }
            repeat((lines - 1).coerceAtLeast(0)) { index ->
                OficiosSkeletonLine(widthFraction = if (index % 2 == 0) 0.9f else 0.7f)
            }
        }
    }
}

/**
 * Lista de cards placeholder a pantalla completa. [contentDescription] es lo que
 * anuncia TalkBack mientras carga (ej. "Cargando pedidos").
 */
@Composable
fun OficiosSkeletonList(
    contentDescription: String,
    modifier: Modifier = Modifier,
    itemCount: Int = 4,
    showAvatar: Boolean = true,
    lines: Int = 2,
    contentPadding: PaddingValues = PaddingValues(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .semantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        repeat(itemCount) {
            OficiosSkeletonCard(showAvatar = showAvatar, lines = lines)
        }
    }
}

@Composable
private fun rememberSkeletonPulse(): State<Float> {
    if (LocalReducedMotion.current) return StaticPulse
    return rememberInfiniteTransition(label = "OficiosSkeletonPulse").animateFloat(
        initialValue = 1f,
        targetValue = PulseMinAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = PulseHalfCycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "OficiosSkeletonAlpha",
    )
}

private object StaticPulse : State<Float> {
    override val value: Float = 1f
}

private const val PulseMinAlpha = 0.5f
private const val PulseHalfCycleMillis = 400

@Preview(name = "Skeleton list", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosSkeletonListPreview() {
    OficiosTheme {
        OficiosSkeletonList(contentDescription = "Cargando", itemCount = 3)
    }
}
