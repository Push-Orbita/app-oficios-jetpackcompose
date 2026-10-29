package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Fila de turno / pedido — Figma agenda `259:435` + patrón de búsqueda
 * (avatar circular, título, meta, chevron).
 * AHORA: card invertida + barra 24dp. HOY: barra 24dp + tag vertical.
 */
@Composable
fun AgendaAppointmentRow(
    rubro: String,
    address: String,
    time: String,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNow: Boolean = false,
    todayLabel: String = "HOY",
    nowLabel: String = "AHORA",
    avatarName: String? = null,
    avatarUrl: String? = null,
    /** Franja gris 16dp de agenda. Mis pedidos la oculta; HOY/AHORA se mantienen. */
    showIdleSideBar: Boolean = true,
) {
    val highlighted = isNow
    val showSideLabel = isNow || isToday
    val sideLabel = if (isNow) nowLabel else todayLabel
    val containerColor = if (highlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
    val titleColor = if (highlighted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
    val bodyColor = if (highlighted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    val chevronColor = if (highlighted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
    val title = agendaRowTitle(rubro = rubro, counterpartName = avatarName)
    val detail = agendaRowDetail(
        rubro = rubro,
        address = address,
        title = title,
        counterpartName = avatarName,
    )
    val showAvatar = avatarName != null || !avatarUrl.isNullOrBlank()
    OficiosCard(
        onClick = onClick,
        containerColor = containerColor,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = OficiosSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        if (showSideLabel) {
            Box(
                modifier = Modifier
                    .width(OficiosComponentSize.agendaTodayBarWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sideLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .graphicsLayer { rotationZ = -90f },
                )
            }
        } else if (showIdleSideBar) {
            Box(
                modifier = Modifier
                    .width(OficiosComponentSize.agendaDefaultBarWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = OficiosSpacing.sm,
                    top = OficiosSpacing.sm,
                    bottom = OficiosSpacing.sm,
                    end = OficiosSpacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            if (showAvatar) {
                OficiosAvatar(
                    name = avatarName?.ifBlank { rubro }.orEmpty().ifBlank { title },
                    imageUrl = avatarUrl,
                    size = OficiosComponentSize.avatarSm,
                    circular = true,
                    showBorder = !highlighted,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bodyColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (time.isNotBlank()) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bodyColor,
                        maxLines = 1,
                    )
                }
            }
        }
        Icon(
            imageVector = ImageVector.vectorResource(OficiosIcons.ChevronRight),
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
        )
        }
    }
}

internal fun agendaRowTitle(rubro: String, counterpartName: String?): String =
    counterpartName?.takeIf { it.isNotBlank() } ?: rubro

internal fun agendaRowDetail(
    rubro: String,
    address: String,
    title: String,
    counterpartName: String?,
): String = buildList {
    if (rubro.isNotBlank() && rubro != title) add(rubro)
    if (address.isNotBlank() && address != title && address != counterpartName) add(address)
}.joinToString(" · ")

@Preview(name = "Agenda rows", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun AgendaAppointmentRowPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            AgendaAppointmentRow(
                rubro = "Albañilería",
                address = "Juan José Silva 3800, Mz 52, Casa 4",
                time = "13:30hs",
                isToday = true,
                isNow = true,
                onClick = {},
                avatarName = "María González",
            )
            AgendaAppointmentRow(
                rubro = "Albañilería",
                address = "Juan José Silva 3800, Mz 52, Casa 4",
                time = "13:30hs",
                isToday = true,
                onClick = {},
                avatarName = "María González",
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
            AgendaAppointmentRow(
                rubro = "Plomeria",
                address = "Maipú 745",
                time = "09:30hs",
                isToday = false,
                onClick = {},
                avatarName = "Luis Pérez",
                showIdleSideBar = false,
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
        }
    }
}
