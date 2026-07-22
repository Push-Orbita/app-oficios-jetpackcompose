package com.cocido.oficios.core.designsystem.component.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor)
            .padding(horizontal = OficiosSpacing.xxs, vertical = 2.dp),
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
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
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
