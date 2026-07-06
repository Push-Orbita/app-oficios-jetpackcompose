package com.cocido.oficios.core.designsystem.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosCalendarDay — celda de día en el selector de agenda
//
// Estados: Normal · Hoy · Seleccionado · Con evento · Deshabilitado
// ---------------------------------------------------------------------------

enum class CalendarDayState {
    Normal,
    Today,
    Selected,
    HasEvent,
    Disabled,
}

@Composable
fun OficiosCalendarDay(
    dayNumber: Int,
    dayLabel: String,
    state: CalendarDayState = CalendarDayState.Normal,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDisabled = state == CalendarDayState.Disabled
    val (bgColor, textColor, borderColor) = when (state) {
        CalendarDayState.Selected -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            Color.Transparent,
        )
        CalendarDayState.Today -> Triple(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.primary,
        )
        CalendarDayState.HasEvent -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.tertiary,
        )
        CalendarDayState.Disabled -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.disabled),
            Color.Transparent,
        )
        CalendarDayState.Normal -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurface,
            Color.Transparent,
        )
    }

    val clickMod = if (!isDisabled && onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Column(
        modifier = modifier
            .width(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.dp, borderColor, MaterialTheme.shapes.small)
                else Modifier
            )
            .then(clickMod)
            .padding(vertical = OficiosSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = if (isDisabled) OficiosAlpha.disabled else OficiosAlpha.high),
        )
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
        )
        if (state == CalendarDayState.HasEvent) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosScheduleSlot — franja horaria de la agenda del trabajador
//
// Estados: Disponible · Ocupado · Bloqueado · Pasado
// ---------------------------------------------------------------------------

enum class ScheduleSlotState {
    Available,
    Booked,
    Blocked,
    Past,
}

data class ScheduleSlotData(
    val startTime: String,
    val endTime: String,
    val clientName: String? = null,
    val serviceTitle: String? = null,
    val state: ScheduleSlotState = ScheduleSlotState.Available,
)

@Composable
fun OficiosScheduleSlot(
    slot: ScheduleSlotData,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (bgColor, borderColor, labelColor) = when (slot.state) {
        ScheduleSlotState.Available -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurface,
        )
        ScheduleSlotState.Booked -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onSurface,
        )
        ScheduleSlotState.Blocked -> Triple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.medium),
        )
        ScheduleSlotState.Past -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.disabled),
        )
    }

    val clickMod = if (onClick != null && slot.state != ScheduleSlotState.Past)
        Modifier.clickable(onClick = onClick) else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .then(clickMod)
            .padding(OficiosSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icono de estado
        Icon(
            imageVector = when (slot.state) {
                ScheduleSlotState.Booked -> Icons.Outlined.CheckCircle
                else -> Icons.Outlined.Schedule
            },
            contentDescription = null,
            tint = when (slot.state) {
                ScheduleSlotState.Booked -> OficiosColorSuccess
                ScheduleSlotState.Blocked -> MaterialTheme.colorScheme.error
                else -> OficiosColorMutedText
            },
            modifier = Modifier.size(OficiosComponentSize.iconSm),
        )

        Spacer(Modifier.width(OficiosSpacing.sm))

        // Tiempo
        Text(
            text = "${slot.startTime} – ${slot.endTime}",
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            modifier = Modifier.width(80.dp),
        )

        // Info del slot (cuando hay turno reservado)
        Column(modifier = Modifier.weight(1f).padding(start = OficiosSpacing.sm)) {
            slot.serviceTitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = labelColor)
            }
            slot.clientName?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = OficiosColorMutedText)
            }
        }

        // Estado en texto
        val stateLabel = when (slot.state) {
            ScheduleSlotState.Available -> "Disponible"
            ScheduleSlotState.Booked -> "Reservado"
            ScheduleSlotState.Blocked -> "Bloqueado"
            ScheduleSlotState.Past -> "Pasado"
        }
        Text(
            text = stateLabel,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = OficiosAlpha.high),
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Calendar Days", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun CalendarDaysPreview() {
    OficiosTheme {
        Row(
            modifier = Modifier.padding(OficiosSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            OficiosCalendarDay(dayNumber = 3, dayLabel = "Lun", state = CalendarDayState.Normal, onClick = {})
            OficiosCalendarDay(dayNumber = 4, dayLabel = "Mar", state = CalendarDayState.Today, onClick = {})
            OficiosCalendarDay(dayNumber = 5, dayLabel = "Mié", state = CalendarDayState.Selected, onClick = {})
            OficiosCalendarDay(dayNumber = 6, dayLabel = "Jue", state = CalendarDayState.HasEvent, onClick = {})
            OficiosCalendarDay(dayNumber = 7, dayLabel = "Vie", state = CalendarDayState.Disabled)
        }
    }
}

@Preview(name = "Schedule Slots", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun ScheduleSlotsPreview() {
    OficiosTheme {
        Column(
            modifier = Modifier.padding(OficiosSpacing.md),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            OficiosScheduleSlot(
                slot = ScheduleSlotData("08:00", "09:00", state = ScheduleSlotState.Available),
                onClick = {},
            )
            OficiosScheduleSlot(
                slot = ScheduleSlotData("09:00", "11:00", "María González", "Reparación de cañería", ScheduleSlotState.Booked),
                onClick = {},
            )
            OficiosScheduleSlot(
                slot = ScheduleSlotData("11:00", "12:00", state = ScheduleSlotState.Blocked),
                onClick = {},
            )
            OficiosScheduleSlot(
                slot = ScheduleSlotData("07:00", "08:00", state = ScheduleSlotState.Past),
            )
        }
    }
}
