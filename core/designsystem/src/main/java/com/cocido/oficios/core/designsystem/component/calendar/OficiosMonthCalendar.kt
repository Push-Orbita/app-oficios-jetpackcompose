package com.cocido.oficios.core.designsystem.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

enum class HourChipState {
    Available,
    Selected,
    Unavailable,
    /** Visible y clickable, sin relleno — horas apagadas en Calendario General. */
    Idle,
}

private val WEEK_START = DayOfWeek.SUNDAY
private val WEEKDAY_LABELS = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")

@Composable
fun OficiosMonthCalendar(
    month: YearMonth,
    selectedDate: LocalDate?,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate? = null,
    isDateEnabled: (LocalDate) -> Boolean = { true },
    isDateHighlighted: (LocalDate) -> Boolean = { false },
    isDateMarked: (LocalDate) -> Boolean = { false },
) {
    val locale = Locale.forLanguageTag("es-AR")
    val firstOfMonth = month.atDay(1)
    val lead = (firstOfMonth.dayOfWeek.value % 7)
    val gridStart = firstOfMonth.minusDays(lead.toLong())
    val cells = (0 until 42).map { gridStart.plusDays(it.toLong()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(OficiosRadius.lg))
            .padding(OficiosSpacing.s10),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OficiosComponentSize.textFieldHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MonthNavButton(
                icon = OficiosIcons.ChevronLeft,
                contentDescription = "Mes anterior",
                onClick = { onMonthChange(month.minusMonths(1)) },
            )
            Text(
                text = month.month.getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { it.titlecase(locale) } + " " + month.year,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            MonthNavButton(
                icon = OficiosIcons.ChevronRight,
                contentDescription = "Mes siguiente",
                onClick = { onMonthChange(month.plusMonths(1)) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            WEEKDAY_LABELS.forEach { label ->
                val color = MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier.size(OficiosComponentSize.calendarDayCell),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                    )
                }
            }
        }
        cells.chunked(7)
            .filter { week -> week.any { date -> date.month == month.month } }
            .forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                week.forEach { date ->
                    val inMonth = date.month == month.month
                    val enabled = inMonth && isDateEnabled(date)
                    val selected = date == selectedDate
                    DayCell(
                        day = date.dayOfMonth,
                        selected = selected,
                        isToday = inMonth && date == today,
                        highlighted = inMonth && isDateHighlighted(date),
                        marked = inMonth && isDateMarked(date),
                        enabled = enabled,
                        inMonth = inMonth,
                        onClick = { onDateSelected(date) },
                    )
                }
            }
        }
    }
}

@Composable
fun OficiosHourGrid(
    hours: List<java.time.LocalTime>,
    selectedHour: java.time.LocalTime?,
    hourState: (java.time.LocalTime) -> HourChipState,
    onHourSelected: (java.time.LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (hours.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(OficiosRadius.lg))
            .padding(OficiosSpacing.s10),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
    ) {
        hours.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                row.forEach { hour ->
                    val state = hourState(hour)
                    HourChip(
                        label = "%02d:%02d".format(hour.hour, hour.minute),
                        state = state,
                        onClick = { if (state != HourChipState.Unavailable) onHourSelected(hour) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(4 - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MonthNavButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(OficiosComponentSize.textFieldHeight)
            .clip(RoundedCornerShape(OficiosRadius.sm))
            .background(MaterialTheme.colorScheme.outline)
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
        )
    }
}

@Composable
private fun DayCell(
    day: Int,
    selected: Boolean,
    isToday: Boolean,
    highlighted: Boolean,
    marked: Boolean,
    enabled: Boolean,
    inMonth: Boolean,
    onClick: () -> Unit,
) {
    val colors = OficiosTheme.colors
    val textColor = when {
        selected -> colors.onSelected
        highlighted -> colors.onPrimary
        !inMonth || !enabled -> if (!inMonth) MaterialTheme.colorScheme.outline else colors.disabled
        else -> MaterialTheme.colorScheme.onSurface
    }
    val clickable = enabled && inMonth
    val dotColor = if (selected) colors.onSelected else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(OficiosComponentSize.calendarDayCell)
            .clip(CircleShape)
            .then(
                when {
                    selected -> Modifier.background(colors.selectedContainer)
                    highlighted -> Modifier.background(MaterialTheme.colorScheme.primary)
                    isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else -> Modifier
                },
            )
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            style = if (selected || highlighted || isToday) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = textColor,
            textAlign = TextAlign.Center,
        )
        if (marked && inMonth) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
private fun HourChip(
    label: String,
    state: HourChipState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, borderColor) = when (state) {
        HourChipState.Selected -> Triple(
            OficiosTheme.colors.selectedContainer,
            OficiosTheme.colors.onSelected,
            null
        )
        HourChipState.Available -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary
        )
        HourChipState.Unavailable,
        HourChipState.Idle,
        -> Triple(
            MaterialTheme.colorScheme.outline.copy(alpha = 0f),
            MaterialTheme.colorScheme.outline,
            null
        )
    }
    Box(
        modifier = modifier
            .height(OficiosComponentSize.hourChipHeight)
            .clip(RoundedCornerShape(OficiosRadius.sm))
            .background(bg)
            .then(
                if (borderColor != null) Modifier.border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(OficiosRadius.sm)
                ) else Modifier
            )
            .then(
                if (state != HourChipState.Unavailable) Modifier.clickable(onClick = onClick) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = fg,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewMonthCalendar() {
    OficiosTheme {
        OficiosMonthCalendar(
            month = YearMonth.of(2026, 3),
            selectedDate = LocalDate.of(2026, 3, 4),
            onMonthChange = {},
            onDateSelected = {},
            modifier = Modifier.padding(OficiosSpacing.md),
        )
    }
}
