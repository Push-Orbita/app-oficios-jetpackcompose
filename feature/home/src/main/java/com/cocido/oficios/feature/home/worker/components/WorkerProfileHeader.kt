package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosVerifiedBadge
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryTextFigma
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.R as DsR
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerProfileHeader(
    profile: WorkerProfile,
    modifier: Modifier = Modifier,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    canFavorite: Boolean = false,
    showRate: Boolean = true,
    onHireClick: (() -> Unit)? = null,
    onRateClick: (() -> Unit)? = null,
) {
    val oficios = profile.specialties.map { it.rubroName }.filter { it.isNotBlank() }.distinct()
    val specialized = profile.specialties.flatMap { it.subRubroNames }.filter { it.isNotBlank() }.distinct()
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                ) {
                    OficiosAvatar(
                        name = profile.displayName,
                        imageUrl = profile.avatarUrl,
                        size = OficiosComponentSize.avatarMd,
                        showBorder = true,
                        circular = false,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xss),
                        ) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            if (profile.isVerified) {
                                OficiosVerifiedBadge(
                                    contentDescription = stringResource(R.string.home_worker_verified_cd),
                                )
                            }
                        }
                        WorkerFiveStarRow(rating = profile.reputation?.rating)
                    }
                }
                if (canFavorite && onFavoriteClick != null) {
                    val favoriteCd = stringResource(
                        if (isFavorite) R.string.home_worker_favorite_remove else R.string.home_worker_favorite_add,
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(OficiosIcons.Heart),
                        contentDescription = favoriteCd,
                        tint = if (isFavorite) {
                            OficiosColorAccent
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier
                            .size(OficiosComponentSize.iconMd)
                            .semantics {
                                contentDescription = favoriteCd
                                role = Role.Button
                            }
                            .clickable(onClick = onFavoriteClick),
                    )
                }
            }
            profile.about?.takeIf { it.isNotBlank() }?.let { about ->
                Text(
                    text = about,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorSecondaryTextFigma,
                )
            }
            if (oficios.isNotEmpty()) {
                LabeledValue(
                    label = stringResource(R.string.home_worker_oficios_label),
                    value = oficios.joinToString(", "),
                )
            }
            if (specialized.isNotEmpty()) {
                LabeledValue(
                    label = stringResource(R.string.home_worker_specialized_label),
                    value = specialized.joinToString(", "),
                )
            }
            if (onHireClick != null || (showRate && onRateClick != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onHireClick != null) {
                        OficiosButton(
                            text = stringResource(R.string.home_worker_contact),
                            onClick = onHireClick,
                            variant = OficiosButtonVariant.Primary,
                        )
                    }
                    if (showRate && onRateClick != null) {
                        OficiosButton(
                            text = stringResource(R.string.home_worker_rate),
                            onClick = onRateClick,
                            variant = OficiosButtonVariant.Secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorSecondaryTextFigma,
        )
    }
}

@Composable
fun WorkerFiveStarRow(rating: Float?) {
    val ratingCd = if (rating != null) {
        stringResource(DsR.string.ds_worker_rating, rating)
    } else {
        stringResource(DsR.string.ds_worker_no_rating)
    }
    if (rating == null) {
        Text(
            text = stringResource(DsR.string.ds_worker_no_rating),
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorSecondaryTextFigma,
        )
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        modifier = Modifier.semantics { contentDescription = ratingCd },
    ) {
        Text(
            text = if (rating % 1f == 0f) rating.toInt().toString() else String.format("%.1f", rating),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                val filled = rating - index
                val icon = when {
                    filled >= 1f -> OficiosIcons.Star
                    filled > 0f -> OficiosIcons.StarHalf
                    else -> OficiosIcons.Star
                }
                val tint = if (filled > 0f) {
                    OficiosColorAccent
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                }
                Icon(
                    imageVector = ImageVector.vectorResource(icon),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
        }
    }
}
