package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.feature.home.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.calendar.OficiosScheduleSlot
import com.cocido.oficios.core.designsystem.component.calendar.ScheduleSlotData
import com.cocido.oficios.core.designsystem.component.calendar.ScheduleSlotState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler
import com.cocido.oficios.domain.model.ClientDomicilio
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateOrderScreen(
    uiState: CreateOrderUiState,
    onEvent: (CreateOrderEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasUnsavedChanges = uiState is CreateOrderUiState.Success &&
        (uiState.description.isNotBlank() || uiState.proposedDateTime != null)
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = hasUnsavedChanges,
        onConfirmExit = onNavigateBack,
    )

    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(title = stringResource(R.string.create_order_title), onBack = handleBack)
        },
    ) { paddingValues ->
        when (uiState) {
            CreateOrderUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = stringResource(R.string.create_order_loading),
            )
            is CreateOrderUiState.Error -> OficiosErrorState(
                title = stringResource(R.string.create_order_error),
                description = uiState.message,
                onRetry = { onEvent(CreateOrderEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            is CreateOrderUiState.Success -> CreateOrderForm(
                state = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CreateOrderForm(
    state: CreateOrderUiState.Success,
    onEvent: (CreateOrderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.create_order_request, state.workerName),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

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
                isError = state.hasAttemptedSubmit && state.selectedSubRubroId == null,
            )
        }

        if (state.domicilios.isEmpty() || state.contactos.isEmpty()) {
            Text(
                text = stringResource(R.string.create_order_profile_required),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val domicilioOptions = mutableListOf<Pair<ClientDomicilio, String>>()
            for (domicilio in state.domicilios) {
                domicilioOptions += domicilio to domicilio.displayLabel()
            }
            OficiosDropdownField(
                value = domicilioOptions.firstOrNull { it.first.id == state.selectedDomicilioId }?.second.orEmpty(),
                options = domicilioOptions.map { it.second },
                onOptionSelected = { label ->
                    domicilioOptions.firstOrNull { it.second == label }?.let {
                        onEvent(CreateOrderEvent.DomicilioSelected(it.first.id))
                    }
                },
                label = stringResource(R.string.create_order_address),
                isError = state.hasAttemptedSubmit && state.selectedDomicilioId == null,
            )
            OficiosDropdownField(
                value = state.contactos.firstOrNull { it.id == state.selectedContactoId }?.nombre.orEmpty(),
                options = state.contactos.map { it.nombre },
                onOptionSelected = { name ->
                    state.contactos.firstOrNull { it.nombre == name }?.let {
                        onEvent(CreateOrderEvent.ContactoSelected(it.id))
                    }
                },
                label = stringResource(R.string.create_order_contact),
                isError = state.hasAttemptedSubmit && state.selectedContactoId == null,
            )
        }

        if (state.isLoadingSlots) {
            Text(
                text = stringResource(R.string.create_order_slots_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.slotsErrorMessage?.let { error ->
                OficiosInlineAlert(message = error)
            }
            if (state.availableSlots.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.create_order_slots_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                state.availableSlots.forEach { slot ->
                    val dateLabel = slot.start.format(SLOT_DATE_FORMATTER)
                    OficiosScheduleSlot(
                        slot = ScheduleSlotData(
                            startTime = "$dateLabel ${slot.start.format(SLOT_TIME_FORMATTER)}",
                            endTime = slot.end.format(SLOT_TIME_FORMATTER),
                            state = if (state.selectedSlotId == slot.id) {
                                ScheduleSlotState.Booked
                            } else {
                                ScheduleSlotState.Available
                            },
                        ),
                        onClick = { onEvent(CreateOrderEvent.SlotSelected(slot.id)) },
                    )
                }
            } else if (state.selectedSubRubroId != null && state.slotsErrorMessage == null) {
                Text(
                    text = stringResource(R.string.create_order_no_slots),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        DateTimePickerButton(
            label = stringResource(R.string.create_order_propose),
            selectedDateTime = state.proposedDateTime,
            onDateTimeSelected = { onEvent(CreateOrderEvent.DateTimeSelected(it)) },
        )

        if (state.hasAttemptedSubmit && state.selectedSlotId == null && state.proposedDateTime == null) {
            Text(
                text = stringResource(R.string.create_order_pick_slot),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
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
            keyboardActions = KeyboardActions(onDone = { onEvent(CreateOrderEvent.Submit) }),
        )

        OficiosButton(
            text = stringResource(R.string.create_order_submit),
            onClick = { onEvent(CreateOrderEvent.Submit) },
            loading = state.isSubmitting,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}

@Composable
private fun ClientDomicilio.displayLabel(): String {
    val raw = listOfNotNull(nombre, listOfNotNull(calle, numero).joinToString(" ").ifBlank { null }, barrioName)
        .joinToString(" · ")
    return raw.ifBlank { stringResource(R.string.create_order_address_fallback, id) }
}

private val SLOT_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-AR"))
private val SLOT_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")
