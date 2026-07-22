package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.theme.OficiosColorTertiary
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerProfile

@Composable
fun WorkerProfileHeader(
    profile: WorkerProfile,
    modifier: Modifier = Modifier,
) {
    val reputation = profile.reputation
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        OficiosAvatar(
            name = profile.displayName,
            imageUrl = profile.avatarUrl,
            size = OficiosComponentSize.avatarMd,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (profile.isVerified) {
                    Spacer(Modifier.width(OficiosSpacing.xxs))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(OficiosComponentSize.iconSm),
                    )
                }
            }

            reputation?.rating?.let { rating ->
                Spacer(Modifier.padding(top = OficiosSpacing.xxxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = OficiosColorTertiary,
                        modifier = Modifier.size(OficiosComponentSize.iconSm),
                    )
                    Spacer(Modifier.width(OficiosSpacing.xxxs))
                    Text(
                        text = String.format("%.1f", rating),
                        style = MaterialTheme.typography.labelMedium,
                        color = OficiosColorTertiary,
                    )
                    reputation.reviewCount?.let { count ->
                        Text(
                            text = "($count)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = OficiosSpacing.xxxs),
                        )
                    }
                }
            }
        }
    }
}
