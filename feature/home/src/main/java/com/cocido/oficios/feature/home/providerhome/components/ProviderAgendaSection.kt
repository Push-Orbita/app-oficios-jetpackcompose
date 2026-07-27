package com.cocido.oficios.feature.home.providerhome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.calendar.CalendarDayState
import com.cocido.oficios.core.designsystem.component.calendar.OficiosCalendarDay
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.orders.ServiceOrderStatusLabel
import java.time.LocalDate
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
    val today = remember { LocalDate.now() }

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

                val firstOfMonth = visibleMonth.atDay(1)
                val leadingBlanks = firstOfMonth.dayOfWeek.value - 1 // Lunes = 1
                val daysInMonth = visibleMonth.lengthOfMonth()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp * ((leadingBlanks + daysInMonth + 6) / 7)),
                ) {
                    items(leadingBlanks) {
                        Text(text = "", modifier = Modifier)
                    }
                    items(daysInMonth) { index ->
                        val day = index + 1
                        val date = visibleMonth.atDay(day)
                        OficiosCalendarDay(
                            dayNumber = day,
                            dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-AR")),
                            state = if (date == today) CalendarDayState.Today else CalendarDayState.Normal,
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.provider_home_upcoming_shifts_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (upcomingOrders.isEmpty()) {
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
                upcomingOrders.forEach { order ->
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

@Preview(name = "Provider Agenda Section", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProviderAgendaSectionPreview() {
    OficiosTheme {
        ProviderAgendaSection(onViewFullAgenda = {})
    }
}
