package com.cocido.oficios.feature.home.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.calendar.CalendarDayState
import com.cocido.oficios.core.designsystem.component.calendar.OficiosCalendarDay
import com.cocido.oficios.core.designsystem.component.calendar.OficiosScheduleSlot
import com.cocido.oficios.core.designsystem.component.calendar.ScheduleSlotData
import com.cocido.oficios.core.designsystem.component.calendar.ScheduleSlotState
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import com.cocido.oficios.feature.home.orders.DateTimePickerButton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val agendaLocale = Locale.forLanguageTag("es-AR")

@Composable
fun AgendaListScreen(
    uiState: AgendaListUiState,
    onEvent: (AgendaListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = "Agenda",
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEvent(AgendaListEvent.OpenConfig) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Configurar agenda",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when (uiState) {
            AgendaListUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = "Cargando agenda…",
            )
            is AgendaListUiState.Error -> OficiosErrorState(
                title = "No pudimos cargar tu agenda",
                description = uiState.message,
                onRetry = { onEvent(AgendaListEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            is AgendaListUiState.Success -> AgendaListContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun AgendaListContent(
    uiState: AgendaListUiState.Success,
    onEvent: (AgendaListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.subRubroOptions.isEmpty()) {
        OficiosEmptyState(
            title = "Todavía no tenés servicios que usen turnos",
            description = "Activá la opción de trabajar con turnos en alguno de tus servicios para organizarlo desde acá.",
            icon = Icons.Outlined.EventBusy,
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.testTag("agenda_list"),
        contentPadding = PaddingValues(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        if (uiState.subRubroOptions.size > 1) {
            item {
                OficiosDropdownField(
                    value = uiState.selectedSubRubro?.subRubroLabel.orEmpty(),
                    options = uiState.subRubroOptions.map { it.subRubroLabel },
                    onOptionSelected = { label ->
                        uiState.subRubroOptions.firstOrNull { it.subRubroLabel == label }?.let {
                            onEvent(AgendaListEvent.SubRubroSelected(it.id))
                        }
                    },
                    label = "Servicio",
                )
            }
        }

        item {
            AgendaViewSelector(selected = uiState.viewMode, onEvent = onEvent)
        }

        item {
            AgendaCalendar(uiState = uiState, onEvent = onEvent)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", agendaLocale),
                    ).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                OficiosButton(
                    text = "Hoy",
                    onClick = { onEvent(AgendaListEvent.GoToToday) },
                    variant = OficiosButtonVariant.Ghost,
                )
            }
        }

        item {
            if (uiState.showSlotForm) {
                SlotForm(uiState = uiState, onEvent = onEvent)
            } else {
                OficiosButton(
                    text = "Agregar disponibilidad",
                    onClick = { onEvent(AgendaListEvent.OpenCreateSlot) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        uiState.slotsError?.let { error ->
            item {
                OficiosCard {
                    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                        Text(
                            text = "No pudimos actualizar los horarios",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OficiosButton(
                            text = "Reintentar",
                            onClick = { onEvent(AgendaListEvent.RetrySlots) },
                            variant = OficiosButtonVariant.Outlined,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (uiState.isLoadingSlots) {
            item { OficiosLoadingState(message = "Actualizando horarios…") }
        } else if (uiState.selectedDateSlots.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = "No hay horarios para este día",
                    description = "Podés agregar disponibilidad para que los clientes elijan un turno real.",
                    icon = Icons.Outlined.CalendarMonth,
                )
            }
        } else {
            items(uiState.selectedDateSlots, key = { it.id }) { slot ->
                AgendaSlotRow(
                    slot = slot,
                    onEditClick = { onEvent(AgendaListEvent.OpenEditSlot(slot.id)) },
                    onDeleteClick = { onEvent(AgendaListEvent.RequestDeleteSlot(slot.id)) },
                )
            }
        }
    }

    if (uiState.slotPendingDeleteId != null) {
        OficiosDialog(
            title = "Eliminar disponibilidad",
            message = "Este horario dejará de estar disponible para nuevos pedidos. ¿Querés eliminarlo?",
            confirmLabel = "Eliminar",
            onConfirm = { onEvent(AgendaListEvent.ConfirmDeleteSlot) },
            onDismiss = { onEvent(AgendaListEvent.DismissDeleteSlot) },
            isDangerous = true,
        )
    }
}

@Composable
private fun AgendaViewSelector(
    selected: AgendaViewMode,
    onEvent: (AgendaListEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
    ) {
        OficiosButton(
            text = "Mes",
            onClick = { onEvent(AgendaListEvent.ViewModeChanged(AgendaViewMode.MONTH)) },
            variant = if (selected == AgendaViewMode.MONTH) OficiosButtonVariant.Secondary else OficiosButtonVariant.Outlined,
            modifier = Modifier.weight(1f),
        )
        OficiosButton(
            text = "Semana",
            onClick = { onEvent(AgendaListEvent.ViewModeChanged(AgendaViewMode.WEEK)) },
            variant = if (selected == AgendaViewMode.WEEK) OficiosButtonVariant.Secondary else OficiosButtonVariant.Outlined,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AgendaCalendar(
    uiState: AgendaListUiState.Success,
    onEvent: (AgendaListEvent) -> Unit,
) {
    OficiosCard {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onEvent(AgendaListEvent.PreviousPeriod) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = if (uiState.viewMode == AgendaViewMode.MONTH) "Mes anterior" else "Semana anterior",
                    )
                }
                Text(
                    text = periodLabel(uiState),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { onEvent(AgendaListEvent.NextPeriod) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = if (uiState.viewMode == AgendaViewMode.MONTH) "Mes siguiente" else "Semana siguiente",
                    )
                }
            }

            when (uiState.viewMode) {
                AgendaViewMode.MONTH -> MonthGrid(uiState = uiState, onDateSelected = {
                    onEvent(AgendaListEvent.DateSelected(it))
                })
                AgendaViewMode.WEEK -> WeekRow(uiState = uiState, onDateSelected = {
                    onEvent(AgendaListEvent.DateSelected(it))
                })
            }
        }
    }
}

@Composable
private fun MonthGrid(
    uiState: AgendaListUiState.Success,
    onDateSelected: (LocalDate) -> Unit,
) {
    val weekLabels = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        weekLabels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(width = 40.dp, height = 24.dp),
            )
        }
    }

    val leadingBlanks = uiState.visibleMonth.atDay(1).dayOfWeek.value % 7
    val cells: List<LocalDate?> = buildList {
        repeat(leadingBlanks) { add(null) }
        repeat(uiState.visibleMonth.lengthOfMonth()) { index -> add(uiState.visibleMonth.atDay(index + 1)) }
        while (size % 7 != 0) add(null)
    }
    cells.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            week.forEach { date ->
                if (date == null) {
                    Spacer(Modifier.size(width = 40.dp, height = 52.dp))
                } else {
                    OficiosCalendarDay(
                        dayNumber = date.dayOfMonth,
                        dayLabel = "",
                        state = calendarState(date, uiState),
                        onClick = { onDateSelected(date) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekRow(
    uiState: AgendaListUiState.Success,
    onDateSelected: (LocalDate) -> Unit,
) {
    val start = uiState.selectedDate.minusDays((uiState.selectedDate.dayOfWeek.value % 7).toLong())
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(7) { offset ->
            val date = start.plusDays(offset.toLong())
            OficiosCalendarDay(
                dayNumber = date.dayOfMonth,
                dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, agendaLocale).take(2).replaceFirstChar { it.uppercase() },
                state = calendarState(date, uiState),
                onClick = { onDateSelected(date) },
            )
        }
    }
}

private fun calendarState(date: LocalDate, uiState: AgendaListUiState.Success): CalendarDayState = when {
    date == uiState.selectedDate -> CalendarDayState.Selected
    date == LocalDate.now() -> CalendarDayState.Today
    date in uiState.datesWithSlots -> CalendarDayState.HasEvent
    else -> CalendarDayState.Normal
}

private fun periodLabel(uiState: AgendaListUiState.Success): String = when (uiState.viewMode) {
    AgendaViewMode.MONTH -> uiState.visibleMonth.month.getDisplayName(TextStyle.FULL, agendaLocale)
        .replaceFirstChar { it.uppercase() } + " ${uiState.visibleMonth.year}"
    AgendaViewMode.WEEK -> {
        val start = uiState.selectedDate.minusDays((uiState.selectedDate.dayOfWeek.value % 7).toLong())
        val end = start.plusDays(6)
        "${start.format(DateTimeFormatter.ofPattern("d MMM", agendaLocale))} – " +
            end.format(DateTimeFormatter.ofPattern("d MMM", agendaLocale))
    }
}

@Composable
private fun SlotForm(
    uiState: AgendaListUiState.Success,
    onEvent: (AgendaListEvent) -> Unit,
) {
    val displayedIssue = uiState.formIssue ?: uiState.derivedFormIssue?.takeUnless {
        it == AgendaSlotFormIssue.MISSING_DATE
    }
    OficiosCard {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Text(
                text = if (uiState.editingSlotId == null) "Nueva disponibilidad" else "Editar disponibilidad",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Elegí un inicio y un fin para el mismo día. No se permiten horarios superpuestos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DateTimePickerButton(
                label = "Inicio",
                selectedDateTime = uiState.slotStart,
                onDateTimeSelected = { onEvent(AgendaListEvent.SlotStartSelected(it)) },
            )
            DateTimePickerButton(
                label = "Fin",
                selectedDateTime = uiState.slotEnd,
                onDateTimeSelected = { onEvent(AgendaListEvent.SlotEndSelected(it)) },
            )
            OficiosCheckbox(
                checked = uiState.slotAutoAcceptance,
                onCheckedChange = { onEvent(AgendaListEvent.SlotAutoAcceptanceChanged(it)) },
                label = "Aceptar automáticamente los pedidos para este horario",
            )
            displayedIssue?.let {
                Text(
                    text = slotIssueMessage(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            uiState.formBackendError?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            OficiosButton(
                text = if (uiState.editingSlotId == null) "Guardar disponibilidad" else "Guardar cambios",
                onClick = { onEvent(AgendaListEvent.SubmitSlot) },
                enabled = !uiState.isSavingSlot,
                loading = uiState.isSavingSlot,
                modifier = Modifier.fillMaxWidth(),
            )
            OficiosButton(
                text = "Cancelar",
                onClick = { onEvent(AgendaListEvent.DismissSlotForm) },
                variant = OficiosButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun slotIssueMessage(issue: AgendaSlotFormIssue): String = when (issue) {
    AgendaSlotFormIssue.MISSING_DATE -> "Elegí el inicio y el fin del horario."
    AgendaSlotFormIssue.END_BEFORE_START -> "La hora de finalización debe ser posterior al inicio."
    AgendaSlotFormIssue.DIFFERENT_DAY -> "El inicio y el fin deben pertenecer al mismo día."
    AgendaSlotFormIssue.PAST_DATE -> "Elegí una fecha y hora futuras."
    AgendaSlotFormIssue.OVERLAP -> "Este horario se superpone con otro turno de tu agenda."
}

@Composable
private fun AgendaSlotRow(
    slot: AgendaSlot,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val isPast = slot.end.isBefore(LocalDateTime.now())
    val isEditable = !isPast && slot.status == AgendaSlotStatus.DISPONIBLE
    OficiosCard {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
            OficiosScheduleSlot(
                slot = ScheduleSlotData(
                    startTime = slot.start.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = slot.end.format(DateTimeFormatter.ofPattern("HH:mm")),
                    serviceTitle = slot.subRubroLabel,
                    clientName = slot.bookingId?.let { "Reserva #$it" },
                    state = when {
                        isPast -> ScheduleSlotState.Past
                        slot.status == AgendaSlotStatus.DISPONIBLE -> ScheduleSlotState.Available
                        slot.status == AgendaSlotStatus.RESERVADO -> ScheduleSlotState.Booked
                        else -> ScheduleSlotState.Blocked
                    },
                ),
                onClick = if (isEditable) onEditClick else null,
            )
            if (isEditable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar disponibilidad",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Eliminar disponibilidad",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
