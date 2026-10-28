package com.cocido.oficios.core.designsystem.component.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Campana 48×48, fondo #004DA8, radio 8 — Figma 221:995 / 224:1041.
 */
@Composable
fun OficiosNotificationButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    unreadCount: Int = 0,
) {
    val shape = RoundedCornerShape(OficiosRadius.sm)
    OficiosNotificationBadge(count = unreadCount) {
        Box(
            modifier = modifier
                .widthIn(min = OficiosComponentSize.avatarSm)
                .heightIn(min = OficiosComponentSize.avatarSm)
                .size(OficiosComponentSize.avatarSm)
                .clip(shape)
                .background(MaterialTheme.colorScheme.outline)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .semantics { role = Role.Button }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.BellDot),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosNotificationButtonPreview() {
    OficiosTheme {
        OficiosNotificationButton(
            onClick = {},
            contentDescription = "Notificaciones",
            unreadCount = 2,
        )
    }
}
