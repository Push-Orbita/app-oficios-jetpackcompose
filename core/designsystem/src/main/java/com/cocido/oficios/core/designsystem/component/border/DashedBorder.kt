package com.cocido.oficios.core.designsystem.component.border

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Borde punteado 1:1 Figma (`border-dashed`). El stroke se inseta para no recortarse.
 */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp,
): Modifier = drawBehind {
    val strokePx = strokeWidth.toPx()
    val inset = strokePx / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(
            width = (size.width - strokePx).coerceAtLeast(0f),
            height = (size.height - strokePx).coerceAtLeast(0f),
        ),
        cornerRadius = CornerRadius((cornerRadius.toPx() - inset).coerceAtLeast(0f)),
        style = Stroke(
            width = strokePx,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                0f,
            ),
        ),
    )
}
