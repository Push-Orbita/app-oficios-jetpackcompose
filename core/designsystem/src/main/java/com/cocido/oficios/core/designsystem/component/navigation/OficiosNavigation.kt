package com.cocido.oficios.core.designsystem.component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.theme.OficiosBottomNavLabelStyle
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
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
// Figma: border-top 1px #004DA8, p16 gap16, icon-box 40 r8 (fondo #004DA8
//        si seleccionado), label Satoshi Medium 16sp blanco, sin escala animada.
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
    val colorSpec = oficiosTween<Color>(OficiosMotion.DurationMedium)
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = colorSpec,
        label = "BottomNavContentColor",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent,
        animationSpec = colorSpec,
        label = "BottomNavIndicatorColor",
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) IndicatorWidthSelected else OficiosComponentSize.iconMd,
        animationSpec = oficiosTween(OficiosMotion.DurationMedium),
        label = "BottomNavIndicatorWidth",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) SelectedIconScale else 1f,
        animationSpec = oficiosTween(OficiosMotion.DurationMedium),
        label = "BottomNavIconScale",
    )

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clickable(onClick = onClick)
            .padding(vertical = OficiosSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(width = IndicatorWidthSelected, height = IndicatorHeight),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = indicatorWidth, height = IndicatorHeight)
                    .background(color = indicatorColor, shape = RoundedCornerShape(OficiosRadius.pill)),
            )
            OficiosNotificationBadge(count = item.badgeCount) {
                Icon(
                    imageVector = ImageVector.vectorResource(item.iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
        }
        Spacer(Modifier.height(OficiosSpacing.xxxs))
        BottomNavLabel(
            text = item.label,
            color = contentColor,
            baseStyle = OficiosBottomNavLabelStyle, // Satoshi Medium 16sp
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OficiosSpacing.xxxs),
        )
    }
}

private val IndicatorHeight = OficiosComponentSize.iconMd + OficiosSpacing.xs
private val IndicatorWidthSelected = OficiosComponentSize.iconMd + OficiosSpacing.md * 2
private const val SelectedIconScale = 1.08f

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

@Preview(name = "Bottom Navigation Cliente", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun BottomNavClientePreview() {
    OficiosTheme {
        val items = listOf(
            OficiosNavItem("home", "Inicio", OficiosIcons.House),
            OficiosNavItem("orders", "Mis pedidos", OficiosIcons.ReceiptText, badgeCount = 3),
            OficiosNavItem("favorites", "Favoritos", OficiosIcons.Heart),
            OficiosNavItem("profile", "Perfil", OficiosIcons.User),
        )
        OficiosBottomNavigationBar(
            currentRoute = "orders",
            items = items,
            onItemSelected = {},
        )
    }
}

@Preview(name = "Bottom Navigation Trabajador", showBackground = true, backgroundColor = 0xFF00358B)
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
