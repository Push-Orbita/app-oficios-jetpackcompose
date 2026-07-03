package com.cocido.oficios.core.designsystem.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosNavItem — modelo para un ítem de navegación inferior
// ---------------------------------------------------------------------------

data class OficiosNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

// ---------------------------------------------------------------------------
// OficiosBottomNavigationBar
//
// Figma: border-top #413764 (1dp), selected container #413764 radius 8dp
//        label Satoshi Medium → FontFamily.SansSerif
// ---------------------------------------------------------------------------

// Insets de gesture bar: OficiosScaffold → OficiosBottomBarSlot (no duplicar aquí).
@Composable
fun OficiosBottomNavigationBar(
    currentRoute: String,
    items: List<OficiosNavItem>,
    onItemSelected: (OficiosNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OficiosComponentSize.bottomBarHeight)
                .selectableGroup()
                .padding(horizontal = OficiosSpacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                OficiosBottomNavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OficiosBottomNavItem(
    item: OficiosNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = OficiosSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(OficiosSpacing.sm),
                )
                .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.xxs),
            contentAlignment = Alignment.Center,
        ) {
            OficiosNotificationBadge(count = item.badgeCount) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = contentColor,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
        }
        Spacer(Modifier.height(OficiosSpacing.xxxs))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OficiosSpacing.xxxs),
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Bottom Navigation", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun BottomNavPreview() {
    OficiosTheme {
        val items = listOf(
            OficiosNavItem("home", "Inicio", Icons.Outlined.Home),
            OficiosNavItem("orders", "Pedidos", Icons.Outlined.Work, badgeCount = 3),
            OficiosNavItem("chat", "Mensajes", Icons.AutoMirrored.Outlined.Chat, badgeCount = 12),
            OficiosNavItem("profile", "Perfil", Icons.Outlined.Person),
        )
        OficiosBottomNavigationBar(
            currentRoute = "orders",
            items = items,
            onItemSelected = {},
        )
    }
}
