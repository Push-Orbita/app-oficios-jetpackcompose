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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.feature.home.R
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
            size = OficiosComponentSize.avatarLg,
            showBorder = true,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val trade = profile.specialties.firstOrNull { it.isPrimary }?.rubroName
                ?: profile.specialties.firstOrNull()?.rubroName
            if (!trade.isNullOrBlank()) {
                Text(
                    text = trade,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (profile.isVerified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(OficiosComponentSize.iconSm),
                    )
                    Spacer(Modifier.width(OficiosSpacing.xxs))
                    Text(
                        text = stringResource(R.string.home_verified_user),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
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
