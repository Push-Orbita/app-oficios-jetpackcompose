package com.cocido.oficios.feature.home.providerhome.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.layout.HomeUserIdentityColumn
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R

// ---------------------------------------------------------------------------
// ProviderHomeHeader — README pantallas 12-15: nombre + especialidades + badge
// verificado. Análogo a ClientHomeHeader pero con la línea de especialidad.
// ---------------------------------------------------------------------------

@Composable
fun ProviderHomeHeader(
    userName: String,
    isVerified: Boolean,
    specialtyLabel: String?,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            OficiosAvatar(
                name = userName,
                imageUrl = null,
                size = OficiosComponentSize.avatarSm,
                showBorder = true,
            )
            HomeUserIdentityColumn(
                userName = userName,
                isVerified = isVerified,
                verifiedContentDescription = stringResource(R.string.home_verified_user),
                subtitle = specialtyLabel,
                verifiedTint = MaterialTheme.colorScheme.tertiary,
            )
        }
        OficiosNotificationBadge(count = unreadNotificationCount) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(OficiosComponentSize.avatarSm)
                    .clip(RoundedCornerShape(OficiosSpacing.sm))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(OficiosSpacing.sm),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.home_notifications),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
