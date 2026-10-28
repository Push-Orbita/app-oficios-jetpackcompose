package com.cocido.oficios.core.designsystem.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.DrawableRes
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.badges.OficiosVerifiedBadge
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

/**
 * Nombre Bold 16 + badge-check (Figma 221:990). Hasta 3 líneas; el check
 * no compite por ancho con un chip de rol.
 */
@Composable
fun HomeUserIdentityColumn(
    userName: String,
    isVerified: Boolean,
    verifiedContentDescription: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes verifiedIconRes: Int = OficiosIcons.BadgeCheck,
) {
    val stackVerifiedBadge = LocalConfiguration.current.fontScale >= 1.3f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
    ) {
        if (stackVerifiedBadge) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (isVerified) {
                OficiosVerifiedBadge(
                    contentDescription = verifiedContentDescription,
                    iconRes = verifiedIconRes,
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xss),
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isVerified) {
                    OficiosVerifiedBadge(
                        contentDescription = verifiedContentDescription,
                        iconRes = verifiedIconRes,
                    )
                }
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
