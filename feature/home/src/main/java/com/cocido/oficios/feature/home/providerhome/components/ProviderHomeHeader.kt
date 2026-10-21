package com.cocido.oficios.feature.home.providerhome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.component.layout.HomeUserIdentityColumn
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R

@Composable
fun ProviderHomeHeader(
    userName: String,
    isVerified: Boolean,
    specialtyLabel: String?,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    profilePhotoUrl: String? = null,
) {
    val subtitle = if (isVerified) {
        specialtyLabel
    } else {
        stringResource(R.string.provider_home_pending_validation)
    }
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
                imageUrl = profilePhotoUrl,
                size = OficiosComponentSize.avatarSm,
                showBorder = true,
            )
            HomeUserIdentityColumn(
                userName = userName,
                isVerified = isVerified,
                verifiedContentDescription = stringResource(R.string.home_verified_user),
                subtitle = subtitle,
            )
        }
        OficiosNotificationBadge(count = unreadNotificationCount) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(OficiosComponentSize.avatarSm)
                    .clip(RoundedCornerShape(OficiosRadius.sm))
                    .background(OficiosColorOutlineFill),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.BellDot),
                    contentDescription = stringResource(R.string.home_notifications),
                    tint = OficiosColorWhite,
                )
            }
        }
    }
}
