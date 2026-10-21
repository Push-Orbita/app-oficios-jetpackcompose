package com.cocido.oficios.core.designsystem.component.interaction

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Constraints
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import kotlin.math.max

/**
 * Asegura un *layout* mínimo de 48 dp (infla el slot).
 * Usar solo en filas full-width u otros contenedores donde 48 dp de alto está bien.
 * Chips y botones Small usan [OficiosTouchTarget]: el 48 dp es overlay y no abre un hueco.
 */
fun Modifier.oficiosMinTouchTarget(): Modifier = sizeIn(
    minWidth = OficiosComponentSize.minTouchTarget,
    minHeight = OficiosComponentSize.minTouchTarget,
)

/**
 * Envuelve un visual Figma y coloca un overlay táctil/semántico de 48 dp.
 * El padre conserva el tamaño visual; el overlay puede desbordar sin sumar gap.
 */
@Composable
fun OficiosTouchTarget(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityText: String? = null,
    accessibilityContentDescription: String? = null,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = LocalIndication.current,
    content: @Composable () -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.clearAndSetSemantics { }) {
            content()
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .oficiosTouchOverlaySize()
                .clickable(
                    interactionSource = resolvedInteractionSource,
                    indication = indication,
                    enabled = enabled,
                    role = role,
                    onClick = onClick,
                )
                .semantics {
                    this.role = role
                    if (!accessibilityText.isNullOrBlank()) {
                        text = AnnotatedString(accessibilityText)
                    }
                    if (!accessibilityContentDescription.isNullOrBlank()) {
                        contentDescription = accessibilityContentDescription
                    }
                },
        )
    }
}

/**
 * Mide al hijo como mínimo 48×48 sin achicar un visual más grande.
 * Combinado con [matchParentSize], el slot del padre sigue siendo el visual.
 */
private fun Modifier.oficiosTouchOverlaySize(): Modifier = layout { measurable, constraints ->
    val minPx = OficiosComponentSize.minTouchTarget.roundToPx()
    val width = max(boundedDimension(constraints, width = true), minPx)
    val height = max(boundedDimension(constraints, width = false), minPx)
    val placeable = measurable.measure(Constraints.fixed(width, height))
    layout(width, height) {
        placeable.place(0, 0)
    }
}

private fun boundedDimension(constraints: Constraints, width: Boolean): Int {
    val hasBound = if (width) constraints.hasBoundedWidth else constraints.hasBoundedHeight
    val max = if (width) constraints.maxWidth else constraints.maxHeight
    val min = if (width) constraints.minWidth else constraints.minHeight
    val raw = if (hasBound) max else min
    return raw.coerceAtLeast(0)
}
