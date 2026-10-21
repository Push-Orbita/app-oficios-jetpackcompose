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
import com.cocido.oficios.core.designsystem.component.calendar.OficiosMonthCalendar
import com.cocido.oficios.core.designsystem.component.cards.AgendaAppointmentRow
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.domain.model.AgendaAppointmentTag
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.agendaRubroLabel
import com.cocido.oficios.domain.model.agendaTag
import com.cocido.oficios.domain.model.agendaTimeLabel
import com.cocido.oficios.domain.model.groupAgendaAppointments
import com.cocido.oficios.domain.model.scheduledDate
import com.cocido.oficios.feature.home.R
import java.time.YearMonth
import java.time.ZonedDateTime

/**
 * Agenda en Inicio trabajador — Flujos `526:667` (bloque Agenda) + `526:31` (home).
 * Calendario mensual (días con turno marcados) + lista por fecha (AHORA / HOY).
 */
@Composable
fun ProviderAgendaSection(
    upcomingOrders: List<ServiceOrderListItem>,
    onViewFullAgenda: () -> Unit,
    onOrderClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    now: ZonedDateTime = ZonedDateTime.now(DisplayDateTime.ARGENTINA),
) {
    val today = now.toLocalDate()
    val groups = remember(upcomingOrders, today) {
        groupAgendaAppointments(upcomingOrders, today)
    }
    val appointmentDates = remember(upcomingOrders) {
        upcomingOrders.mapNotNull { it.scheduledDate() }.toSet()
    }
    var visibleMonth by remember(today) { mutableStateOf(YearMonth.from(today)) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
        horizontalAlignment = Alignment.End,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.provider_home_agenda_title),
                style = MaterialTheme.typography.headlineLarge,
                color = OficiosColorWhite,
            )
            OficiosMonthCalendar(
                month = visibleMonth,
                selectedDate = today,
                onMonthChange = { visibleMonth = it },
                onDateSelected = {},
                isDateHighlighted = { it in appointmentDates && it != today },
            )
            groups.forEach { group ->
                Text(
                    text = group.headerLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = OficiosColorWhite,
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
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = stringResource(R.string.provider_home_view_full_agenda),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorWhite,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onViewFullAgenda),
            )
        }
    }
}

@Preview(name = "Provider Agenda List", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProviderAgendaSectionPreview() {
    OficiosTheme {
        ProviderAgendaSection(
            now = ZonedDateTime.of(2026, 3, 4, 13, 45, 0, 0, DisplayDateTime.ARGENTINA),
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
                    scheduledSortKey = "2026-03-04T13:30:00",
                ),
            ),
        )
    }
}
