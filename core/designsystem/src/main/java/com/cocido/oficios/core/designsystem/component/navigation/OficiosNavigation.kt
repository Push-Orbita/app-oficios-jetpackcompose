package com.cocido.oficios.core.designsystem.component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.theme.OficiosBottomNavLabelStyle
import com.cocido.oficios.core.designsystem.theme.OficiosColors
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.core.designsystem.theme.oficiosTween

// ---------------------------------------------------------------------------
// OficiosNavItem — modelo para un ítem de navegación inferior
// ---------------------------------------------------------------------------

data class OficiosNavItem(
    val route: String,
    val label: String,
    val iconRes: Int, // DrawableRes del ícono Lucide
    val badgeCount: Int = 0,
)

// ---------------------------------------------------------------------------
// OficiosBottomNavigationBar
//
// Claro (BBVA): íconos y labels siempre primary. Seleccionado = círculo 48dp
//        1:1 con ícono 24 blanco; el label sigue primary. Oscuro: estilo navy
//        actual, indicador circular. Sin pastilla ni ripple ovalado.
//
// Items según Figma:
//   - Inicio (house)
//   - Mis pedidos (scroll-text o receipt-text)
//   - Favoritos (heart) — oculto para trabajadores
//   - Perfil (user)
//
// Ayuda se QUITÓ de la barra inferior: ahora es item dentro de Ajustes.
// ---------------------------------------------------------------------------

// Insets de gesture bar: OficiosScaffold → OficiosBottomBarSlot (no duplicar aquí).
@Composable
fun OficiosBottomNavigationBar(
    currentRoute: String,
    items: List<OficiosNavItem>,
    onItemSelected: (OficiosNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cada pantalla-tab compone su propia barra: arrancar desde la última tab mostrada
    // hace que el indicador se desplace en vez de aparecer ya seleccionado.
    var indicatorRoute by remember { mutableStateOf(BottomNavIndicatorMemory.lastRoute ?: currentRoute) }
    LaunchedEffect(currentRoute) {
        indicatorRoute = currentRoute
        BottomNavIndicatorMemory.lastRoute = currentRoute
    }
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
                    selected = indicatorRoute == item.route,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private object BottomNavIndicatorMemory {
    var lastRoute: String? = null
}

@Composable
private fun OficiosBottomNavItem(
    item: OficiosNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OficiosTheme.colors
    val colorSpec = oficiosTween<Color>(OficiosMotion.DurationMedium)
    val iconTint by animateColorAsState(
        targetValue = bottomNavIconTint(selected, colors),
        animationSpec = colorSpec,
        label = "BottomNavIconTint",
    )
    val labelColor by animateColorAsState(
        targetValue = bottomNavLabelColor(selected, colors),
        animationSpec = colorSpec,
        label = "BottomNavLabelColor",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) colors.iconContainer else Color.Transparent,
        animationSpec = colorSpec,
        label = "BottomNavIndicatorColor",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = OficiosSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(OficiosComponentSize.bottomNavIndicator)
                .testTag(if (selected) BottomNavSelectedIndicatorTag else BottomNavIndicatorTag),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(indicatorColor, CircleShape),
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                OficiosNotificationBadge(count = item.badgeCount) {
                    Icon(
                        imageVector = ImageVector.vectorResource(item.iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                }
            }
        }
        Spacer(Modifier.height(OficiosSpacing.xxxs))
        BottomNavLabel(
            text = item.label,
            color = labelColor,
            baseStyle = OficiosBottomNavLabelStyle.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OficiosSpacing.xxxs),
        )
    }
}

internal const val BottomNavSelectedIndicatorTag = "bottomNavSelectedIndicator"
internal const val BottomNavIndicatorTag = "bottomNavIndicator"

internal fun bottomNavIconTint(selected: Boolean, colors: OficiosColors): Color = when {
    selected -> colors.onIconContainer
    colors.isDark -> colors.onSurfaceVariant
    else -> colors.primary
}

internal fun bottomNavLabelColor(selected: Boolean, colors: OficiosColors): Color = when {
    colors.isDark && selected -> colors.selectedOnSurface
    colors.isDark -> colors.onSurfaceVariant
    else -> colors.primary
}

@Composable
private fun BottomNavLabel(
    text: String,
    color: Color,
    baseStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val minFontSize = (11f / density.fontScale).coerceAtLeast(8f).sp
    BoxWithConstraints(modifier) {
        var fontSize by remember(text, maxWidth, baseStyle.fontSize) {
            mutableStateOf(baseStyle.fontSize)
        }
        Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = baseStyle.copy(fontSize = fontSize),
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { result ->
                if ((result.didOverflowWidth || result.hasVisualOverflow) && fontSize > minFontSize) {
                    val scaled = fontSize * 0.9f
                    fontSize = if (scaled < minFontSize) minFontSize else scaled
                }
            },
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Bottom Navigation Cliente claro", showBackground = true, backgroundColor = 0xFFF4F5F7)
@Composable
private fun BottomNavClienteLightPreview() {
    OficiosTheme {
        val items = listOf(
            OficiosNavItem("home", "Inicio", OficiosIcons.House),
            OficiosNavItem("search", "Buscar", OficiosIcons.Search),
            OficiosNavItem("orders", "Mis pedidos", OficiosIcons.ReceiptText, badgeCount = 3),
            OficiosNavItem("favorites", "Favoritos", OficiosIcons.Heart),
            OficiosNavItem("profile", "Perfil", OficiosIcons.User),
        )
        OficiosBottomNavigationBar(
            currentRoute = "home",
            items = items,
            onItemSelected = {},
        )
    }
}

@Preview(name = "Bottom Navigation Cliente oscuro", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun BottomNavClientePreview() {
    OficiosTheme(darkTheme = true) {
        val items = listOf(
            OficiosNavItem("home", "Inicio", OficiosIcons.House),
            OficiosNavItem("search", "Buscar", OficiosIcons.Search),
            OficiosNavItem("orders", "Mis pedidos", OficiosIcons.ReceiptText, badgeCount = 3),
            OficiosNavItem("favorites", "Favoritos", OficiosIcons.Heart),
            OficiosNavItem("profile", "Perfil", OficiosIcons.User),
        )
        OficiosBottomNavigationBar(
            currentRoute = "home",
            items = items,
            onItemSelected = {},
        )
    }
}

@Preview(name = "Bottom Navigation Trabajador", showBackground = true, backgroundColor = 0xFFF4F5F7)
@Composable
private fun BottomNavTrabajadorPreview() {
    OficiosTheme {
        val items = listOf(
            OficiosNavItem("home", "Inicio", OficiosIcons.House),
            OficiosNavItem("orders", "Mis pedidos", OficiosIcons.ReceiptText),
            OficiosNavItem("profile", "Perfil", OficiosIcons.User),
        )
        OficiosBottomNavigationBar(
            currentRoute = "home",
            items = items,
            onItemSelected = {},
        )
    }
}
