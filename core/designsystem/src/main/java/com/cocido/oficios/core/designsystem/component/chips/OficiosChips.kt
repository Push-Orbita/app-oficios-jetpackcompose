package com.cocido.oficios.core.designsystem.component.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.cards.OrderStatus
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosChip — chip básico de etiqueta/selección
// ---------------------------------------------------------------------------

@Composable
fun OficiosChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    leadingIcon: ImageVector? = null,
    verticalPadding: androidx.compose.ui.unit.Dp = OficiosSpacing.xs,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), MaterialTheme.shapes.extraSmall)
            .then(clickModifier)
            .padding(horizontal = OficiosSpacing.sm, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(OficiosComponentSize.iconSm),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosFilterChip — chip con estado seleccionado/no seleccionado
// ---------------------------------------------------------------------------

@Composable
fun OficiosFilterChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onToggle)
            .padding(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(OficiosComponentSize.iconSm),
            )
            Spacer(Modifier.width(OficiosSpacing.xxxs))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

// ---------------------------------------------------------------------------
// OficiosStatusChip — chip semántico de estado de orden
// Mapping sin hardcode de colores → usa colorScheme
// ---------------------------------------------------------------------------

@Composable
fun OficiosStatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = statusColors(status)

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor.copy(alpha = 0.15f))
            .border(
                border = BorderStroke(1.dp, containerColor.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.extraSmall,
            )
            .padding(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xxxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = status.label(),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun statusColors(status: OrderStatus): Pair<Color, Color> = when (status) {
    is OrderStatus.Pending -> Pair(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.tertiary,
    )
    is OrderStatus.Confirmed, is OrderStatus.Approved -> Pair(
        OficiosColorSuccess,
        OficiosColorSuccess,
    )
    is OrderStatus.InReview -> Pair(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant,
    )
    is OrderStatus.Completed -> Pair(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.high),
    )
    is OrderStatus.Rejected, is OrderStatus.Cancelled -> Pair(
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.error,
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Chips", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ChipsPreview() {
    OficiosTheme {
        Column(
            modifier = Modifier.padding(OficiosSpacing.md),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            OficiosChip(label = "Chip base")
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                OficiosFilterChip(label = "Activo", selected = true, onToggle = {})
                OficiosFilterChip(label = "Inactivo", selected = false, onToggle = {})
            }
            Spacer(Modifier.height(OficiosSpacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                OficiosStatusChip(OrderStatus.Pending)
                OficiosStatusChip(OrderStatus.Confirmed)
                OficiosStatusChip(OrderStatus.Rejected)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                OficiosStatusChip(OrderStatus.InReview)
                OficiosStatusChip(OrderStatus.Completed)
                OficiosStatusChip(OrderStatus.Cancelled)
            }
        }
    }
}
