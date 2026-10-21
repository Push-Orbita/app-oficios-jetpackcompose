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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
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
import com.cocido.oficios.core.designsystem.component.navigation.OficiosScreenTitle
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryTextFigma
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
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

    OficiosScaffold(
        modifier = modifier,
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
                        onNavigateBack = { onEvent(CreateOrderEvent.EditRequest) },
                    )
                } else {
                    CreateOrderForm(
                        state = state,
                        onEvent = onEvent,
                        onNavigateBack = handleBack,
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
    onNavigateBack: () -> Unit,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        OficiosScreenTitle(
            title = stringResource(
                if (state.hasAgenda) R.string.create_order_title_schedule else R.string.create_order_title_request,
            ),
            onNavigateBack = onNavigateBack,
        )

        if (state.hasAgenda) {
            WorkerMiniCard(state)
        }

        if (!state.hasAgenda && state.subRubroOptions.size > 1) {
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

        if (!state.hasAgenda) {
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
        }

        if (!state.hasAgenda) {
            if (state.contactos.isEmpty()) {
                OficiosInlineAlert(message = stringResource(R.string.create_order_profile_required))
            } else {
                Text(
                    text = stringResource(R.string.create_order_location),
                    style = MaterialTheme.typography.titleMedium,
                    color = OficiosColorWhite,
                )
                val selected = state.domicilios.firstOrNull { it.id == state.selectedDomicilioId }
                OficiosTextField(
                    value = selected?.displayLabel().orEmpty(),
                    onValueChange = {},
                    label = "",
                    floatingLabel = false,
                    readOnly = true,
                    placeholder = stringResource(R.string.create_order_add_location),
                    trailingContent = {
                        Icon(
                            imageVector = ImageVector.vectorResource(OficiosIcons.CirclePlus),
                            contentDescription = stringResource(R.string.create_order_add_location),
                            tint = OficiosColorWhite,
                            modifier = Modifier
                                .size(OficiosComponentSize.iconMd)
                                .clickable { onEvent(CreateOrderEvent.AddLocation) },
                        )
                    },
                    modifier = Modifier.clickable { onEvent(CreateOrderEvent.AddLocation) },
                    isError = state.hasAttemptedContinue && state.selectedDomicilioId == null,
                )
            }
        }

        Text(
            text = stringResource(
                if (state.hasAgenda) R.string.create_order_date else R.string.create_order_date_time,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = OficiosColorWhite,
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
                color = OficiosColorWhite,
            )
        }
        if (state.isLoadingSlots) {
            Text(
                text = stringResource(R.string.create_order_slots_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = OficiosColorTextSecondaryAccessible,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            OficiosButton(
                text = stringResource(R.string.create_order_continue),
                onClick = { onEvent(CreateOrderEvent.ContinueToSummary) },
                variant = OficiosButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun CreateOrderSummary(
    state: CreateOrderUiState.Success,
    onEvent: (CreateOrderEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val domicilio = state.domicilios.firstOrNull { it.id == state.selectedDomicilioId }
    val visit = CreateOrderSchedule.visitDateTime(state.selectedDate, state.selectedHour)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        OficiosScreenTitle(
            title = stringResource(R.string.create_order_title_summary),
            onNavigateBack = onNavigateBack,
        )

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md, Alignment.End),
        ) {
            OficiosButton(
                text = stringResource(R.string.create_order_edit),
                onClick = { onEvent(CreateOrderEvent.EditRequest) },
                variant = OficiosButtonVariant.Primary,
            )
            OficiosButton(
                text = stringResource(R.string.create_order_submit),
                onClick = { onEvent(CreateOrderEvent.Submit) },
                loading = state.isSubmitting,
                enabled = !state.isSubmitting,
                variant = OficiosButtonVariant.Secondary,
            )
        }
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
                        color = OficiosColorWhite,
                    )
                    if (state.workerVerified) {
                        OficiosVerifiedBadge(contentDescription = null)
                    }
                }
                WorkerFiveStarRow(rating = state.workerRating)
            }
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.Heart),
                contentDescription = null,
                tint = OficiosColorWhite,
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
            .border(1.dp, OficiosColorOutlineFill, RoundedCornerShape(OficiosRadius.lg)),
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
                tint = OficiosColorSecondaryTextFigma,
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
                .background(OficiosColorOutlineFill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = OficiosColorWhite,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = OficiosColorWhite,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorTextSecondaryAccessible,
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
