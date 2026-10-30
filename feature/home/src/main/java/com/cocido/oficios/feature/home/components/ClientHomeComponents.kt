package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.oficios.feature.home.common.HomeChromeViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosNotificationButton
import com.cocido.oficios.core.designsystem.component.layout.HomeUserIdentityColumn
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
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openProfileLabel = stringResource(R.string.home_open_profile)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = openProfileLabel
                }
                .clickable(role = Role.Button, onClick = onProfileClick),
        ) {
            OficiosAvatar(
                name = userName,
                imageUrl = profilePhotoUrl,
                size = OficiosComponentSize.avatarSm,
                showBorder = true,
            )
            HomeUserIdentityColumn(
                userName = userName,
                isVerified = emailVerified,
                verifiedContentDescription = stringResource(R.string.home_verified_user),
            )
        }
        OficiosNotificationButton(
            onClick = onNotificationsClick,
            contentDescription = stringResource(R.string.home_notifications),
            unreadCount = unreadNotificationCount,
        )
    }
}

@Composable
fun ClientHomeBottomBar(
    selectedTab: ClientHomeTab,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
    showFavorites: Boolean? = null,
) {
    val roleShowsFavorites = if (showFavorites != null) {
        showFavorites
    } else {
        hiltViewModel<HomeChromeViewModel>().showFavorites.collectAsState().value
    }
    val items = buildList {
        add(OficiosNavItem(ClientHomeTab.INICIO.route, stringResource(R.string.home_nav_inicio), OficiosIcons.House))
        if (roleShowsFavorites) {
            add(OficiosNavItem(ClientHomeTab.BUSCAR.route, stringResource(R.string.home_nav_buscar), OficiosIcons.Search))
        }
        add(OficiosNavItem(ClientHomeTab.PEDIDOS.route, stringResource(R.string.home_nav_pedidos), OficiosIcons.ReceiptText))
        if (roleShowsFavorites) {
            add(OficiosNavItem(ClientHomeTab.FAVORITOS.route, stringResource(R.string.home_nav_favoritos), OficiosIcons.Heart))
        }
        add(OficiosNavItem(ClientHomeTab.AJUSTES.route, stringResource(R.string.home_nav_ajustes), OficiosIcons.User))
    }
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
    BUSCAR("home_tab_buscar"),
    PEDIDOS("home_tab_pedidos"),
    FAVORITOS("home_tab_favoritos"),
    AJUSTES("home_tab_ajustes"),
}
