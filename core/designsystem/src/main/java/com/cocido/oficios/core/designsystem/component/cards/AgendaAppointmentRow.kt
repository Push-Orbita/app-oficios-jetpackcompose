package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryTextFigma
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Fila de turno — Figma `259:435` / `259:606`.
 * AHORA: card blanca + barra 24dp #006DEF. HOY: barra 24dp + tag vertical. Resto: barra 16dp #004DA8.
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
) {
    val shape = RoundedCornerShape(OficiosRadius.sm)
    val highlighted = isNow
    val showSideLabel = isNow || isToday
    val sideLabel = if (isNow) nowLabel else todayLabel
    val surface = if (highlighted) OficiosColorWhite else Color.Transparent
    val titleColor = if (highlighted) OficiosColorBackground else OficiosColorWhite
    val bodyColor = if (highlighted) OficiosColorBackground else OficiosColorSecondaryTextFigma
    val chevronColor = if (highlighted) OficiosColorBackground else OficiosColorWhite
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(surface, shape)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .clickable(onClick = onClick)
            .padding(end = OficiosSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showSideLabel) {
            Box(
                modifier = Modifier
                    .width(OficiosComponentSize.agendaTodayBarWidth)
                    .fillMaxHeight()
                    .background(OficiosColorAction),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sideLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = OficiosColorWhite,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .graphicsLayer { rotationZ = -90f },
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(OficiosComponentSize.agendaDefaultBarWidth)
                    .fillMaxHeight()
                    .background(OficiosColorOutlineFill),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = OficiosSpacing.xs,
                    top = OficiosSpacing.xs,
                    bottom = OficiosSpacing.xs,
                    end = OficiosSpacing.xs,
                ),
        ) {
            Text(
                text = rubro,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (address.isNotBlank()) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = bodyColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (time.isNotBlank()) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge,
                    color = bodyColor,
                    maxLines = 1,
                )
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
            )
            AgendaAppointmentRow(
                rubro = "Albañilería",
                address = "Juan José Silva 3800, Mz 52, Casa 4",
                time = "13:30hs",
                isToday = true,
                onClick = {},
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
            AgendaAppointmentRow(
                rubro = "Plomeria",
                address = "Maipú 745",
                time = "09:30hs",
                isToday = false,
                onClick = {},
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
        }
    }
}
