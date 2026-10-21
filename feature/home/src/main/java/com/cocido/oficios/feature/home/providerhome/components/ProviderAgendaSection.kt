package com.cocido.oficios.feature.home.providerhome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.calendar.CalendarDayState
import com.cocido.oficios.core.designsystem.component.calendar.OficiosCalendarDay
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.orders.ServiceOrderStatusLabel
import java.time.LocalDate
import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// ---------------------------------------------------------------------------
// ProviderAgendaSection — README pantallas 12-15: mini-calendario mensual
// navegable + "próximos turnos". La agenda de gestión real (turnos, estados
// ocupado/disponible) es Fase 4 — acá solo se puede navegar el mes y ver el
// día de hoy, todavía sin datos reales de `agenda/slot`/`sesion`.
// ---------------------------------------------------------------------------

@Composable
fun ProviderAgendaSection(
    onViewFullAgenda: () -> Unit,
    modifier: Modifier = Modifier,
    upcomingOrders: List<ServiceOrderListItem> = emptyList(),
    onOrderClick: (Long) -> Unit = {},
) {
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = remember { LocalDate.now() }
    val eventDates = remember(upcomingOrders) { upcomingOrders.mapNotNull { it.scheduledDate() }.toSet() }
    val ordersForSelectedDay = upcomingOrders.filter { it.scheduledDate() == selectedDate }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.provider_home_agenda_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            OficiosButton(
                text = stringResource(R.string.provider_home_view_full_agenda),
                onClick = onViewFullAgenda,
                variant = OficiosButtonVariant.Ghost,
            )
        }

        OficiosCard {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.provider_home_previous_month),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR"))
                            .replaceFirstChar { it.uppercase() } + " ${visibleMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.provider_home_next_month),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Grilla no-lazy: cada fila mide el alto de su celda mas alta, por lo que
                // se adapta al font scale (1.0, 1.3, 2.0) sin recortar la ultima semana.
                // La altura fija anterior (agendaCell * semanas) cortaba filas con fuentes grandes.
                val cells = agendaMonthCells(visibleMonth)
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
                    cells.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            week.forEach { date ->
                                if (date == null) {
                                    Spacer(Modifier.width(OficiosComponentSize.agendaCell))
                                } else {
                                    OficiosCalendarDay(
                                        dayNumber = date.dayOfMonth,
                                        dayLabel = date.dayOfWeek.getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.forLanguageTag("es-AR"),
                                        ),
                                        state = when {
                                            date == selectedDate -> CalendarDayState.Selected
                                            date == today -> CalendarDayState.Today
                                            else -> CalendarDayState.Normal
                                        },
                                        hasEvent = date in eventDates,
                                        onClick = { selectedDate = date },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.provider_home_upcoming_shifts_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (ordersForSelectedDay.isEmpty()) {
            OficiosCard {
                OficiosEmptyState(
                    title = stringResource(R.string.provider_home_upcoming_shifts_empty_title),
                    description = stringResource(R.string.provider_home_upcoming_shifts_empty_description),
                    icon = Icons.Outlined.EventBusy,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                ordersForSelectedDay.forEach { order ->
                    OficiosCard(onClick = { onOrderClick(order.id) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
                            ServiceOrderStatusLabel(status = order.status, bookingStatus = order.bookingStatus)
                            Text(
                                text = order.counterpartName.ifBlank { order.subRubroLabel },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            order.scheduledLabel?.let {
                                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Celdas del mini-calendario mensual (lunes = primera columna). Devuelve una lista
 * multiplo de 7: `null` para los huecos iniciales y finales, y la fecha para cada dia.
 * Pura y testeable: garantiza que todos los dias del mes quedan incluidos (sin recortes).
 */
internal fun agendaMonthCells(month: YearMonth): List<LocalDate?> {
    val leadingBlanks = month.atDay(1).dayOfWeek.value - 1 // Lunes = 1
    val daysInMonth = month.lengthOfMonth()
    return buildList {
        repeat(leadingBlanks) { add(null) }
        repeat(daysInMonth) { index -> add(month.atDay(index + 1)) }
        while (size % 7 != 0) add(null)
    }
}

private fun ServiceOrderListItem.scheduledDate(): LocalDate? {
    val raw = scheduledSortKey?.trim().orEmpty()
    if (raw.isEmpty()) return null
    return DisplayDateTime.toZonedDateTime(raw)?.toLocalDate()
        ?: runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()
}

@Preview(name = "Provider Agenda Section", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProviderAgendaSectionPreview() {
    OficiosTheme {
        ProviderAgendaSection(onViewFullAgenda = {})
    }
}
