package com.cocido.oficios.feature.home.providerhome.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.core.designsystem.component.calendar.OficiosMonthCalendar
import com.cocido.oficios.core.designsystem.component.cards.AgendaAppointmentRow
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.AgendaAppointmentTag
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.agendaRubroLabel
import com.cocido.oficios.domain.model.agendaTag
import com.cocido.oficios.domain.model.agendaTimeLabel
import com.cocido.oficios.domain.model.defaultAgendaDateInMonth
import com.cocido.oficios.domain.model.groupAgendaAppointments
import com.cocido.oficios.domain.model.scheduledDate
import com.cocido.oficios.feature.home.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

/**
 * Agenda en Inicio trabajador — Flujos `526:667` (bloque Agenda) + `526:31` (home).
 * Calendario mensual (días con pedido marcados) + lista del día seleccionado.
 */
@Composable
fun ProviderAgendaSection(
    upcomingOrders: List<ServiceOrderListItem>,
    onViewFullAgenda: () -> Unit,
    onOrderClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    now: ZonedDateTime = ZonedDateTime.now(DisplayDateTime.ARGENTINA),
    selectedDate: LocalDate? = null,
    visibleMonth: YearMonth? = null,
    onDateSelected: ((LocalDate) -> Unit)? = null,
    onMonthChange: ((YearMonth) -> Unit)? = null,
    showTitle: Boolean = true,
    showViewFullAgenda: Boolean = true,
    hasAgendaConfigured: Boolean = true,
    onConfigureAgenda: (() -> Unit)? = null,
) {
    val today = now.toLocalDate()
    val appointmentDates = remember(upcomingOrders) {
        upcomingOrders.mapNotNull { it.scheduledDate() }.toSet()
    }
    var internalDate by remember { mutableStateOf(today) }
    var internalMonth by remember { mutableStateOf(YearMonth.from(today)) }
    val currentDate = selectedDate ?: internalDate
    val currentMonth = visibleMonth ?: internalMonth
    val dayOrders = remember(upcomingOrders, currentDate) {
        upcomingOrders.filter { it.scheduledDate() == currentDate }
    }
    val groups = remember(dayOrders, today) {
        groupAgendaAppointments(dayOrders, today)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
        horizontalAlignment = Alignment.End,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            if (showTitle) {
                Text(
                    text = stringResource(R.string.provider_home_agenda_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            OficiosMonthCalendar(
                month = currentMonth,
                selectedDate = currentDate,
                today = today,
                onMonthChange = { month ->
                    val date = defaultAgendaDateInMonth(
                        month = month,
                        today = today,
                        fallbackDay = currentDate.dayOfMonth,
                    )
                    if (selectedDate == null) {
                        internalMonth = month
                        internalDate = date
                    }
                    onMonthChange?.invoke(month)
                },
                onDateSelected = { date ->
                    if (selectedDate == null) {
                        internalDate = date
                        internalMonth = YearMonth.from(date)
                    }
                    onDateSelected?.invoke(date)
                },
                isDateMarked = { it in appointmentDates },
            )
            if (groups.isEmpty()) {
                if (upcomingOrders.isEmpty()) {
                    Text(
                        text = stringResource(R.string.agenda_bookings_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            if (!hasAgendaConfigured) {
                                R.string.provider_home_calendar_setup_body
                            } else {
                                R.string.agenda_bookings_empty_description
                            },
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (onConfigureAgenda != null) {
                        Text(
                            text = stringResource(R.string.provider_home_configure_agenda),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onConfigureAgenda),
                        )
                    }
                } else {
                    Text(
                        text = stringResource(
                            if (currentDate == today) {
                                R.string.provider_home_agenda_empty_today
                            } else {
                                R.string.provider_home_agenda_empty_day
                            },
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                groups.forEach { group ->
                    Text(
                        text = group.headerLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    group.items.forEach { order ->
                        val tag = order.agendaTag(now)
                        AgendaAppointmentRow(
                            rubro = order.agendaRubroLabel(),
                            address = order.addressLabel,
                            time = order.agendaTimeLabel(),
                            isToday = tag == AgendaAppointmentTag.TODAY,
                            isNow = tag == AgendaAppointmentTag.NOW,
                            todayLabel = stringResource(R.string.provider_home_agenda_today_tag),
                            nowLabel = stringResource(R.string.provider_home_agenda_now_tag),
                            onClick = { onOrderClick(order.id) },
                            avatarName = order.counterpartName.ifBlank { order.agendaRubroLabel() },
                            avatarUrl = order.avatarUrl,
                        )
                    }
                }
            }
        }
        if (showViewFullAgenda) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(R.string.provider_home_view_full_agenda),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onViewFullAgenda),
                )
            }
        }
    }
}

@Preview(name = "Provider Agenda List", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProviderAgendaSectionPreview() {
    OficiosTheme {
        ProviderAgendaSection(
            now = ZonedDateTime.of(2026, 10, 21, 13, 45, 0, 0, DisplayDateTime.ARGENTINA),
            onViewFullAgenda = {},
            onOrderClick = {},
            upcomingOrders = listOf(
                ServiceOrderListItem(
                    id = 1,
                    counterpartName = "Juan",
                    subRubroLabel = "Albañilería",
                    status = ServiceRequestStatus.ACCEPTED,
                    bookingStatus = BookingStatus.IN_PROGRESS,
                    scheduledLabel = "13:30hs",
                    addressLabel = "Juan José Silva 3800, Mz 52, Casa 4",
                    rubroLabel = "Albañilería",
                    scheduledSortKey = "2026-09-25T07:00:00",
                ),
                ServiceOrderListItem(
                    id = 2,
                    counterpartName = "Ana",
                    subRubroLabel = "Plomería",
                    status = ServiceRequestStatus.ACCEPTED,
                    bookingStatus = BookingStatus.CONFIRMED,
                    scheduledLabel = "10:00hs",
                    addressLabel = "Av. 25 de Mayo 100",
                    rubroLabel = "Plomería",
                    scheduledSortKey = "2026-10-22T10:00:00",
                ),
            ),
        )
    }
}
