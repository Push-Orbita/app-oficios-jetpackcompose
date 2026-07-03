package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBottomNavigationBar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosNavItem
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R

@Composable
fun ClientHomeHeader(
    userName: String,
    emailVerified: Boolean,
    profilePhotoUrl: String? = null,
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
                imageUrl = profilePhotoUrl,
                size = OficiosComponentSize.avatarSm,
                showBorder = true,
            )
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
                )
                if (emailVerified) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = stringResource(R.string.home_verified_user),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                }
            }
        }
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

@Composable
fun HomeSearchBar(
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OficiosSpacing.sm))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(OficiosSpacing.sm),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = stringResource(R.string.home_voice_search),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.home_search),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
        }
    }
}

@Composable
fun ClientHomeBottomBar(
    selectedTab: ClientHomeTab,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        OficiosNavItem(ClientHomeTab.INICIO.route, stringResource(R.string.home_nav_inicio), Icons.Outlined.Home),
        OficiosNavItem(ClientHomeTab.PERFIL.route, stringResource(R.string.home_nav_perfil), Icons.Outlined.Person),
        OficiosNavItem(ClientHomeTab.FAVORITOS.route, stringResource(R.string.home_nav_favoritos), Icons.Outlined.FavoriteBorder),
        OficiosNavItem(ClientHomeTab.AYUDA.route, stringResource(R.string.home_nav_ayuda), Icons.AutoMirrored.Outlined.HelpOutline),
        OficiosNavItem(ClientHomeTab.AJUSTES.route, stringResource(R.string.home_nav_ajustes), Icons.Outlined.Menu),
    )
    OficiosBottomNavigationBar(
        currentRoute = selectedTab.route,
        items = items,
        onItemSelected = { item ->
            ClientHomeTab.entries.firstOrNull { it.route == item.route }?.let(onTabSelected)
        },
        modifier = modifier,
    )
}

enum class ClientHomeTab(val route: String) {
    INICIO("home_tab_inicio"),
    PERFIL("home_tab_perfil"),
    FAVORITOS("home_tab_favoritos"),
    AYUDA("home_tab_ayuda"),
    AJUSTES("home_tab_ajustes"),
}
