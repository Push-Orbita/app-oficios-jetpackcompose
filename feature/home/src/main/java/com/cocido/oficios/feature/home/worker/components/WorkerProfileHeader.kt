package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.theme.OficiosColorTertiary
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.feature.home.R
import java.util.Locale

@Composable
fun WorkerProfileHeader(
    profile: WorkerProfile,
    modifier: Modifier = Modifier,
) {
    val trade = profile.specialties.firstOrNull { it.isPrimary }?.rubroName
        ?: profile.specialties.firstOrNull()?.rubroName
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            OficiosAvatar(
                name = profile.displayName,
                imageUrl = profile.avatarUrl,
                size = OficiosComponentSize.avatarLg,
                showBorder = true,
                circular = true,
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
                            contentDescription = stringResource(R.string.home_worker_verified_cd),
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
                ReputationLine(profile)
                profile.zoneLabel?.let { zone ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = stringResource(R.string.home_worker_zone_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(OficiosComponentSize.iconSm),
                        )
                        Spacer(Modifier.width(OficiosSpacing.xxs))
                        Text(
                            text = zone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReputationLine(profile: WorkerProfile) {
    val reputation = profile.reputation
    val count = reputation?.reviewCount ?: 0
    val rating = reputation?.rating
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = OficiosColorTertiary,
                modifier = Modifier.size(OficiosComponentSize.iconSm),
            )
            Spacer(Modifier.width(OficiosSpacing.xxs))
            if (rating == null || count <= 0) {
                Text(
                    text = stringResource(R.string.home_worker_new),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Text(
                    text = String.format(Locale.US, "%.1f", rating),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = pluralStringResource(R.plurals.home_worker_review_count, count, count),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = OficiosSpacing.xxs),
                )
            }
        }
    }
}
