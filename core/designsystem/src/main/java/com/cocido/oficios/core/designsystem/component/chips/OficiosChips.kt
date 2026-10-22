package com.cocido.oficios.core.designsystem.component.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.cards.OrderStatus
import com.cocido.oficios.core.designsystem.component.feedback.rememberOficiosHaptics
import com.cocido.oficios.core.designsystem.component.interaction.OficiosTouchTarget
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
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
    val haptics = rememberOficiosHaptics()
    val visual = @Composable {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(containerColor)
                .border(BorderStroke(1.dp, borderColor), MaterialTheme.shapes.extraSmall)
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
    if (onClick != null) {
        OficiosTouchTarget(
            onClick = {
                haptics.tick()
                onClick()
            },
            modifier = modifier,
            accessibilityText = label,
        ) {
            visual()
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            visual()
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosSegmentChip — filtro de pantalla (Mis pedidos / Notificaciones)
// Figma: h~36, px-16 py-8, r8, seleccionado fill #006DEF, sin ícono.
// ---------------------------------------------------------------------------

@Composable
fun OficiosSegmentChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberOficiosHaptics()
    val shape = RoundedCornerShape(OficiosRadius.sm)
    OficiosTouchTarget(
        onClick = {
            haptics.tick()
            onClick()
        },
        modifier = modifier,
        accessibilityText = label,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = OficiosComponentSize.buttonHeightSmall)
                .clip(shape)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosFilterChip — Rubros / filtros. Visual ~36–40 dp, 14 sp, px 16.
// El touch 48 dp es overlay ([OficiosTouchTarget]), no infla el layout.
// ---------------------------------------------------------------------------

@Composable
fun OficiosFilterChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    iconUrl: String? = null,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val haptics = rememberOficiosHaptics()
    val shape = RoundedCornerShape(OficiosRadius.sm)

    OficiosTouchTarget(
        onClick = {
            haptics.tick()
            onToggle()
        },
        modifier = modifier,
        accessibilityText = label,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = OficiosComponentSize.filterChipHeight)
                .clip(shape)
                .background(containerColor)
                .border(BorderStroke(1.dp, borderColor), shape)
                .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            when {
                selected -> Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.CircleCheck),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(OficiosComponentSize.iconSm),
                )
                !iconUrl.isNullOrBlank() -> SubcomposeAsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(OficiosComponentSize.iconSm),
                    loading = {},
                    error = {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(OficiosComponentSize.iconSm),
                            )
                        }
                    },
                )
                leadingIcon != null -> Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(OficiosComponentSize.iconSm),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
            )
        }
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
        OficiosTheme.colors.success,
        OficiosTheme.colors.success,
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
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                OficiosSegmentChip(label = "Todas", selected = true, onClick = {})
                OficiosSegmentChip(label = "No leídas", selected = false, onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                OficiosFilterChip(label = "Activo", selected = true, onToggle = {})
                OficiosFilterChip(label = "Inactivo", selected = false, onToggle = {})
            }
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
