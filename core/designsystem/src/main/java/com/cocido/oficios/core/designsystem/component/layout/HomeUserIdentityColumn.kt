package com.cocido.oficios.core.designsystem.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

/**
 * Nombre (+ badge verificado) y subtítulo opcional para headers de home cliente/proveedor.
 * Con fontScale alto evita truncar el nombre por falta de espacio junto al ícono.
 */
@Composable
fun HomeUserIdentityColumn(
    userName: String,
    isVerified: Boolean,
    verifiedContentDescription: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (isVerified) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = verifiedContentDescription,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = verifiedContentDescription,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
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
