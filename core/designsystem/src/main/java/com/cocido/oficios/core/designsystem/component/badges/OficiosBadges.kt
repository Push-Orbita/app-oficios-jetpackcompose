package com.cocido.oficios.core.designsystem.component.badges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosBadge — etiqueta de conteo / indicador
// ---------------------------------------------------------------------------

@Composable
fun OficiosBadge(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    size: Dp? = null,
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    val sizeModifier = if (size != null) {
        Modifier.size(size)
    } else {
        Modifier.defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
    }
    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(shape)
            .background(containerColor)
            .then(
                if (size == null) {
                    Modifier.padding(horizontal = OficiosSpacing.xxs, vertical = 2.dp)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

// ---------------------------------------------------------------------------
// OficiosRoleChip — etiqueta chica de rol (Cliente / Trabajador) sobre navy.
// Texto blanco y negrita para contraste AA; fill outline de tarjeta.
// ---------------------------------------------------------------------------

@Composable
fun OficiosRoleChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.xs)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.outline)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)), shape)
            .padding(horizontal = OficiosSpacing.xs, vertical = OficiosSpacing.xxxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// OficiosNotificationBadge — wrapper que superpone un badge numérico
// sobre un icono u otro composable
// ---------------------------------------------------------------------------

@Composable
fun OficiosNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
    showBadge: Boolean = count > 0,
    maxCount: Int = 99,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        if (showBadge) {
            val label = if (count > maxCount) "${maxCount}+" else count.toString()
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = OficiosSpacing.sm, y = -OficiosSpacing.xxs)
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = density.density,
                        fontScale = density.fontScale.coerceAtMost(1.15f),
                    ),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Badges", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun BadgesPreview() {
    OficiosTheme {
        Column(
            modifier = Modifier.padding(OficiosSpacing.md),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            OficiosBadge(label = "3")
            OficiosBadge(label = "Nuevo", containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.background)
            OficiosRoleChip(label = "Cliente")
            OficiosRoleChip(label = "Trabajador")
            OficiosNotificationBadge(count = 12) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
            OficiosNotificationBadge(count = 150) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
        }
    }
}
