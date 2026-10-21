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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryTextFigma
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Fila de turno en home trabajador — Figma `259:435`.
 * Hoy: barra 24dp #006DEF + tag vertical HOY. Resto: barra 16dp #004DA8.
 */
@Composable
fun AgendaAppointmentRow(
    rubro: String,
    address: String,
    time: String,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    todayLabel: String = "HOY",
) {
    val shape = RoundedCornerShape(OficiosRadius.sm)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .clickable(onClick = onClick)
            .padding(end = OficiosSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .width(OficiosComponentSize.agendaTodayBarWidth)
                    .fillMaxHeight()
                    .background(OficiosColorAction),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = todayLabel,
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
                color = OficiosColorWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (address.isNotBlank()) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorSecondaryTextFigma,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (time.isNotBlank()) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorSecondaryTextFigma,
                    maxLines = 1,
                )
            }
        }
        Icon(
            imageVector = ImageVector.vectorResource(OficiosIcons.ChevronRight),
            contentDescription = null,
            tint = OficiosColorWhite,
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
                onClick = {},
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
