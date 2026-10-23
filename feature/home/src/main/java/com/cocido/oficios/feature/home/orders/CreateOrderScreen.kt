package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosVerifiedBadge
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormActionsBar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormColumn
import com.cocido.oficios.core.designsystem.component.calendar.HourChipState
import com.cocido.oficios.core.designsystem.component.calendar.OficiosHourGrid
import com.cocido.oficios.core.designsystem.component.calendar.OficiosMonthCalendar
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosTrackingMap
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.worker.components.WorkerFiveStarRow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateOrderScreen(
    uiState: CreateOrderUiState,
    onEvent: (CreateOrderEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    showFavorites: Boolean? = true,
    today: LocalDate = LocalDate.now(),
) {
    val hasUnsavedChanges = uiState is CreateOrderUiState.Success &&
        (uiState.description.isNotBlank() || uiState.selectedDate != null)
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = hasUnsavedChanges &&
            (uiState as? CreateOrderUiState.Success)?.step == CreateOrderStep.FORM,
        onConfirmExit = {
            val success = uiState as? CreateOrderUiState.Success
            if (success?.step == CreateOrderStep.SUMMARY) {
                onEvent(CreateOrderEvent.EditRequest)
            } else {
                onNavigateBack()
            }
        },
    )
    val showBottomBar = uiState !is CreateOrderUiState.Success ||
        uiState.step == CreateOrderStep.FORM

    val topBarTitle = when (val state = uiState) {
        is CreateOrderUiState.Success -> stringResource(
            when {
                state.step == CreateOrderStep.SUMMARY -> R.string.create_order_title_summary
                state.hasAgenda -> R.string.create_order_title_schedule
                else -> R.string.create_order_title_request
            },
        )
        else -> stringResource(R.string.create_order_title_request)
    }
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(title = topBarTitle, onBack = handleBack)
        },
        bottomBar = {
            if (showBottomBar) {
                ClientHomeBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    showFavorites = showFavorites,
                )
            }
        },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                CreateOrderUiState.Loading -> OficiosLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.create_order_loading),
                )
                is CreateOrderUiState.Error -> OficiosErrorState(
                    title = stringResource(R.string.create_order_error),
                    description = state.message,
                    onRetry = { onEvent(CreateOrderEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                is CreateOrderUiState.Success -> if (state.step == CreateOrderStep.SUMMARY) {
                    CreateOrderSummary(
                        state = state,
                        onEvent = onEvent,
                    )
                } else {
                    CreateOrderForm(
                        state = state,
                        onEvent = onEvent,
                        today = today,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateOrderForm(
    state: CreateOrderUiState.Success,
    onEvent: (CreateOrderEvent) -> Unit,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    OficiosFormColumn(
        modifier = modifier,
        contentPadding = OficiosSpacing.OficiosScreenPadding,
        footer = {
            OficiosFormActionsBar(
                primaryLabel = stringResource(R.string.create_order_continue),
                onPrimary = { onEvent(CreateOrderEvent.ContinueToSummary) },
                primaryEnabled = CreateOrderSchedule.canContinue(
                    selectedSubRubroId = state.selectedSubRubroId,
                    selectedDomicilioId = state.selectedDomicilioId,
                    selectedContactoId = state.selectedContactoId,
                    selectedDate = state.selectedDate,
                    selectedHour = state.selectedHour,
                ),
            )
        },
    ) {
        if (state.hasAgenda) {
            WorkerMiniCard(state)
        }

        if (state.subRubroOptions.size > 1) {
            OficiosDropdownField(
                value = state.subRubroOptions.firstOrNull { it.id == state.selectedSubRubroId }?.name.orEmpty(),
                options = state.subRubroOptions.map { it.name },
                onOptionSelected = { name ->
                    state.subRubroOptions.firstOrNull { it.name == name }?.let {
                        onEvent(CreateOrderEvent.SubRubroSelected(it.id))
                    }
                },
                label = stringResource(R.string.create_order_specialty),
                isError = state.hasAttemptedContinue && state.selectedSubRubroId == null,
            )
        }

        OficiosTextField(
            value = state.description,
            onValueChange = { onEvent(CreateOrderEvent.DescriptionChanged(it)) },
            label = stringResource(R.string.create_order_description),
            placeholder = stringResource(R.string.create_order_description_hint),
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEvent(CreateOrderEvent.ContinueToSummary) }),
        )

        if (state.contactos.isEmpty() || state.domicilios.isEmpty()) {
            OficiosInlineAlert(message = stringResource(R.string.create_order_profile_required))
        } else {
            Text(
                text = stringResource(R.string.create_order_location),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val selected = state.domicilios.firstOrNull { it.id == state.selectedDomicilioId }
            OficiosTextField(
                value = selected?.displayLabel().orEmpty(),
                onValueChange = {},
                label = stringResource(R.string.create_order_location),
                readOnly = true,
                placeholder = stringResource(R.string.create_order_add_location),
                trailingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(OficiosIcons.CirclePlus),
                        contentDescription = stringResource(R.string.create_order_add_location),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(OficiosComponentSize.iconMd)
                            .clickable { onEvent(CreateOrderEvent.AddLocation) },
                    )
                },
                modifier = Modifier.clickable { onEvent(CreateOrderEvent.AddLocation) },
                isError = state.hasAttemptedContinue && state.selectedDomicilioId == null,
                supportingText = if (state.hasAttemptedContinue && state.selectedDomicilioId == null) {
                    stringResource(R.string.create_order_pick_location)
                } else {
                    null
                },
            )
            OficiosDropdownField(
                value = state.contactos.firstOrNull { it.id == state.selectedContactoId }
                    ?.let { "${it.nombre} · ${it.telefonoWhatsapp}" }
                    .orEmpty(),
                options = state.contactos.map { "${it.nombre} · ${it.telefonoWhatsapp}" },
                onOptionSelected = { label ->
                    state.contactos.firstOrNull { "${it.nombre} · ${it.telefonoWhatsapp}" == label }?.let {
                        onEvent(CreateOrderEvent.ContactoSelected(it.id))
                    }
                },
                label = stringResource(R.string.create_order_contact),
                isError = state.hasAttemptedContinue && state.selectedContactoId == null,
            )
        }

        Text(
            text = stringResource(
                if (state.hasAgenda) R.string.create_order_date else R.string.create_order_date_time,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OficiosMonthCalendar(
            month = state.visibleMonth,
            selectedDate = state.selectedDate,
            onMonthChange = { onEvent(CreateOrderEvent.MonthChanged(it)) },
            onDateSelected = { onEvent(CreateOrderEvent.DateSelected(it)) },
            isDateEnabled = { date ->
                CreateOrderSchedule.isDateEnabled(date, today, state.workDayKeys)
            },
            isDateHighlighted = { date ->
                state.hasAgenda && state.availableSlots.any { slot ->
                    slot.start.toLocalDate() == date
                }
            },
        )

        if (state.hasAgenda) {
            Text(
                text = stringResource(R.string.create_order_hours),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (state.isLoadingSlots) {
            Text(
                text = stringResource(R.string.create_order_slots_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.slotsErrorMessage?.let { OficiosInlineAlert(message = it) }
            val availableHours = CreateOrderSchedule.availableHoursForDate(
                state.availableSlots,
                state.selectedDate,
            )
            OficiosHourGrid(
                selectedHour = state.selectedHour,
                hourState = { hour ->
                    hourChipState(
                        hour = hour,
                        selectedHour = state.selectedHour,
                        hasAgenda = state.hasAgenda,
                        availableHours = availableHours,
                        selectedDate = state.selectedDate,
                    )
                },
                onHourSelected = { onEvent(CreateOrderEvent.HourSelected(it)) },
            )
        }

        if (state.hasAttemptedContinue &&
            (state.selectedDate == null || state.selectedHour == null)
        ) {
            Text(
                text = stringResource(R.string.create_order_pick_slot),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

    }
}

@Composable
private fun CreateOrderSummary(
    state: CreateOrderUiState.Success,
    onEvent: (CreateOrderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val domicilio = state.domicilios.firstOrNull { it.id == state.selectedDomicilioId }
    val visit = CreateOrderSchedule.visitDateTime(state.selectedDate, state.selectedHour)
    OficiosFormColumn(
        modifier = modifier,
        contentPadding = OficiosSpacing.OficiosScreenPadding,
        footer = {
            OficiosFormActionsBar(
                primaryLabel = stringResource(R.string.create_order_submit),
                onPrimary = { onEvent(CreateOrderEvent.Submit) },
                backLabel = stringResource(R.string.create_order_edit),
                onBack = { onEvent(CreateOrderEvent.EditRequest) },
                primaryEnabled = !state.isSubmitting,
                primaryLoading = state.isSubmitting,
            )
        },
    ) {
        SummaryMap(domicilio)

        SummaryInfoRow(
            icon = OficiosIcons.ReceiptText,
            label = stringResource(R.string.create_order_summary_detail),
            value = summaryDetail(state),
        )
        SummaryInfoRow(
            icon = OficiosIcons.MapPinHouse,
            label = stringResource(R.string.create_order_summary_address),
            value = domicilio?.displayLabel().orEmpty(),
        )
        SummaryInfoRow(
            icon = OficiosIcons.Clock,
            label = stringResource(R.string.create_order_summary_visit),
            value = visit?.format(VISIT_FORMATTER).orEmpty(),
        )

    }
}

@Composable
private fun WorkerMiniCard(state: CreateOrderUiState.Success) {
    OficiosCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            OficiosAvatar(
                name = state.workerName,
                imageUrl = state.workerAvatarUrl,
                size = OficiosComponentSize.avatarMd,
                showBorder = true,
                circular = false,
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    Text(
                        text = state.workerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.workerVerified) {
                        OficiosVerifiedBadge(contentDescription = null)
                    }
                }
                WorkerFiveStarRow(
                    rating = state.workerRating,
                    reviewCount = state.workerReviewCount,
                )
            }
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.Heart),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
        }
    }
}

@Composable
private fun SummaryMap(domicilio: ClientDomicilio?) {
    val lat = domicilio?.latitud
    val lng = domicilio?.longitud
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(OficiosComponentSize.mapCardHeight)
            .clip(RoundedCornerShape(OficiosRadius.lg))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(OficiosRadius.lg)),
        contentAlignment = Alignment.Center,
    ) {
        if (lat != null && lng != null) {
            OficiosTrackingMap(
                destinationLatitude = lat,
                destinationLongitude = lng,
                modifier = Modifier.fillMaxSize(),
                addressLabel = domicilio.displayLabel(),
            )
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.MapPinHouse),
                contentDescription = stringResource(R.string.create_order_summary_address),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(OficiosComponentSize.iconLg),
            )
        }
    }
}

@Composable
private fun SummaryInfoRow(icon: Int, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(OficiosComponentSize.iconBox)
                .clip(RoundedCornerShape(OficiosRadius.sm))
                .background(MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun summaryDetail(state: CreateOrderUiState.Success): String {
    val specialty = state.specialtyLabel.ifBlank {
        state.subRubroOptions.firstOrNull { it.id == state.selectedSubRubroId }?.name.orEmpty()
    }
    val description = state.description.trim()
    return buildString {
        if (specialty.isNotBlank()) append(specialty)
        if (description.isNotBlank()) {
            if (isNotEmpty()) append(", ")
            append("“$description”")
        }
        if (isEmpty()) append(state.workerName)
    }
}

private fun hourChipState(
    hour: LocalTime,
    selectedHour: LocalTime?,
    hasAgenda: Boolean,
    availableHours: Set<Int>,
    selectedDate: LocalDate?,
): HourChipState {
    if (selectedHour != null && hour.hour == selectedHour.hour) return HourChipState.Selected
    if (selectedDate == null) return HourChipState.Unavailable
    return if (hasAgenda) {
        if (hour.hour in availableHours) HourChipState.Available else HourChipState.Unavailable
    } else {
        HourChipState.Available
    }
}

@Composable
private fun ClientDomicilio.displayLabel(): String {
    val raw = listOfNotNull(
        nombre,
        listOfNotNull(calle, numero).joinToString(" ").ifBlank { null },
        barrioName,
    ).joinToString(" · ")
    return raw.ifBlank { stringResource(R.string.create_order_address_fallback, id) }
}

private val VISIT_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM, HH:mm'hs'", Locale.forLanguageTag("es-AR"))
