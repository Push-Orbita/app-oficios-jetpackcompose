package com.cocido.oficios.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.categories.CategoryGridItem
import com.cocido.oficios.core.designsystem.component.categories.OficiosCategoriesGrid
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.core.designsystem.component.inputs.FieldAndCtaBlock
import com.cocido.oficios.core.designsystem.component.inputs.FocusFirstInvalidEffect
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.inputs.anchorFirstInvalid
import com.cocido.oficios.core.designsystem.component.inputs.rememberFirstInvalidFieldNavigator
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleDayUi
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleEditor
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleRangeUi
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRegisterTradeTitleStyle
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.navigation.ConfirmDiscardOnSystemBack
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthRegisterLayout

@Composable
fun RegisterTradeScreen(
    uiState: RegisterTradeUiState,
    onEvent: (RegisterTradeEvent) -> Unit,
    modifier: Modifier = Modifier,
    section: RegisterTradeFormSection = RegisterTradeFormSection.FULL,
    showContinue: Boolean = true,
    continueLabel: String? = null,
    onContinue: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    confirmDiscardOnSystemBack: Boolean = true,
) {
    if (confirmDiscardOnSystemBack) {
        ConfirmDiscardOnSystemBack(hasUnsavedChanges = uiState.selectedCategoryId != null)
    }

    AuthRegisterLayout(
        modifier = modifier,
        isLoading = uiState.isLoading || uiState.isLoadingCatalog,
        topBar = onBack?.let { back ->
            {
                OficiosBackTopBar(
                    title = stringResource(
                        when (section) {
                            RegisterTradeFormSection.ZONA -> R.string.auth_wizard_zona_title
                            RegisterTradeFormSection.AGENDA -> R.string.auth_wizard_agenda_title
                            RegisterTradeFormSection.COBRO -> R.string.auth_wizard_cobro_title
                            RegisterTradeFormSection.IDENTIDAD -> R.string.auth_wizard_identidad_title
                            else -> R.string.auth_register_trade_title
                        },
                    ),
                    onBack = back,
                )
            }
        },
    ) {
        if (onBack == null) {
            Text(
                text = stringResource(
                    when (section) {
                        RegisterTradeFormSection.ZONA -> R.string.auth_wizard_zona_title
                        RegisterTradeFormSection.AGENDA -> R.string.auth_wizard_agenda_title
                        RegisterTradeFormSection.COBRO -> R.string.auth_wizard_cobro_title
                        RegisterTradeFormSection.IDENTIDAD -> R.string.auth_wizard_identidad_title
                        else -> R.string.auth_register_trade_title
                    },
                ),
                style = OficiosRegisterTradeTitleStyle,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        val afterCategory = section != RegisterTradeFormSection.FULL || uiState.selectedCategoryId != null
        val showOficio = section == RegisterTradeFormSection.FULL || section == RegisterTradeFormSection.OFICIO
        val showZona = afterCategory && (section == RegisterTradeFormSection.FULL || section == RegisterTradeFormSection.ZONA)
        val showAgenda = afterCategory && (section == RegisterTradeFormSection.FULL || section == RegisterTradeFormSection.AGENDA)
        val showCobro = afterCategory && (section == RegisterTradeFormSection.FULL || section == RegisterTradeFormSection.COBRO)
        val showIdentidad = afterCategory && (section == RegisterTradeFormSection.FULL || section == RegisterTradeFormSection.IDENTIDAD)

        // Banner agregado solo si no hay errores por campo (p.ej. categoría sin elegir en grilla).
        val hasFieldErrors = listOf(
            uiState.categoryError,
            uiState.tradeError,
            uiState.specializationError,
            uiState.experienceError,
            uiState.neighborhoodError,
            uiState.serviceLocationError,
            uiState.scheduleError,
            uiState.turnDurationError,
            uiState.paymentError,
            uiState.localCalleError,
            uiState.localNumeroError,
        ).any { it != null }
        if (!hasFieldErrors) {
            uiState.stepValidationError?.let { resId ->
                OficiosInlineAlert(
                    message = stringResource(resId),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        uiState.catalogError?.let { message ->
            OficiosInlineAlert(message = message)
            OficiosButton(
                text = stringResource(R.string.auth_register_trade_retry_catalog),
                onClick = { onEvent(RegisterTradeEvent.RetryLoadCatalog) },
                variant = OficiosButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val fieldNavigator = rememberFirstInvalidFieldNavigator()
        val invalidKeys = buildList {
            if (uiState.categoryError != null) add(TradeField.Category)
            if (uiState.tradeError != null) add(TradeField.Trade)
            if (uiState.specializationError != null) add(TradeField.Specialization)
            if (uiState.experienceError != null) add(TradeField.Experience)
            if (uiState.neighborhoodError != null) add(TradeField.Neighborhood)
            if (uiState.serviceLocationError != null) add(TradeField.ServiceLocation)
            if (uiState.scheduleError != null) add(TradeField.Schedule)
            if (uiState.turnDurationError != null) add(TradeField.TurnDuration)
            if (uiState.paymentError != null) add(TradeField.Payment)
            if (uiState.localCalleError != null) add(TradeField.LocalCalle)
            if (uiState.localNumeroError != null) add(TradeField.LocalNumero)
        }
        FocusFirstInvalidEffect(
            attemptId = uiState.errorFocusNonce,
            invalidKeys = invalidKeys,
            navigator = fieldNavigator,
        )

        if (showOficio && uiState.selectedCategoryId == null) {
            Column(
                modifier = Modifier.anchorFirstInvalid(fieldNavigator, TradeField.Category),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                if (uiState.categoryOptions.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.auth_register_trade_category_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    OficiosCategoriesGrid(
                        categories = uiState.categoryOptions.map {
                            CategoryGridItem(id = it.id, label = it.label, iconUrl = it.iconUrl)
                        },
                        onCategoryClick = { item ->
                            uiState.categoryOptions.find { it.id == item.id }?.let { option ->
                                onEvent(RegisterTradeEvent.CategoryGridSelected(option))
                            }
                        },
                    )
                }
                uiState.categoryError?.let { FieldInlineError(it) }
            }
        } else if (showOficio) {
            FieldAndCtaBlock {
                Text(
                    text = uiState.selectedCategory,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (uiState.selectedTrade.isBlank()) {
                    Column(
                        modifier = Modifier.anchorFirstInvalid(fieldNavigator, TradeField.Trade),
                        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                    ) {
                        OficiosCategoriesGrid(
                            categories = uiState.tradeOptions.map {
                                CategoryGridItem(id = it.id, label = it.label, iconUrl = it.iconUrl)
                            },
                            onCategoryClick = { item ->
                                uiState.tradeOptions.find { it.id == item.id }?.let { option ->
                                    onEvent(RegisterTradeEvent.TradeChanged(option.label))
                                }
                            },
                        )
                        uiState.tradeError?.let { FieldInlineError(it) }
                    }
                } else {
                    OficiosDropdownField(
                        value = uiState.selectedTrade,
                        options = uiState.trades,
                        onOptionSelected = { onEvent(RegisterTradeEvent.TradeChanged(it)) },
                        label = stringResource(R.string.auth_register_trade_trade_label),
                        placeholder = stringResource(R.string.auth_register_trade_trade_hint),
                        enabled = uiState.isTradeEnabled && !uiState.isLoadingTrades,
                        isError = uiState.tradeError != null,
                        supportingText = uiState.tradeError,
                        focusRequester = fieldNavigator.focusRequester(TradeField.Trade),
                    )
                }

                RegisterTradeSpecializationsSection(
                    options = uiState.specializationOptions,
                    isLoading = uiState.isLoadingSpecializations,
                    selected = uiState.selectedSpecializations,
                    descriptionValue = uiState.otherSpecialization,
                    errorText = uiState.specializationError,
                    sectionModifier = Modifier.anchorFirstInvalid(
                        fieldNavigator,
                        TradeField.Specialization,
                    ),
                    onToggle = { id, checked -> onEvent(RegisterTradeEvent.SpecializationToggled(id, checked)) },
                    onDescriptionChanged = { onEvent(RegisterTradeEvent.OtherSpecializationChanged(it)) },
                )

                if (uiState.selectedCategoryId != null) {
                    RegisterTradeLabeledSection(
                        title = stringResource(R.string.auth_register_trade_experience_label),
                    ) {
                        OficiosDropdownField(
                            value = uiState.selectedExperienceRange,
                            options = uiState.experienceRanges,
                            onOptionSelected = { onEvent(RegisterTradeEvent.ExperienceRangeChanged(it)) },
                            label = stringResource(R.string.auth_register_trade_experience_field_label),
                            placeholder = stringResource(R.string.auth_register_trade_experience_hint),
                            isError = uiState.experienceError != null,
                            supportingText = uiState.experienceError,
                            focusRequester = fieldNavigator.focusRequester(TradeField.Experience),
                        )
                    }
                }
            }
        }

        if (showZona) {
            RegisterTradeLabeledSection(
                title = stringResource(R.string.auth_register_trade_neighborhood_label),
            ) {
                OficiosAutocompleteField(
                    value = uiState.neighborhoodQuery,
                    onValueChange = { onEvent(RegisterTradeEvent.NeighborhoodQueryChanged(it)) },
                    suggestions = uiState.neighborhoodOptions,
                    onSuggestionSelected = { onEvent(RegisterTradeEvent.NeighborhoodSelected(it)) },
                    suggestionLabel = { it.label },
                    label = stringResource(R.string.auth_register_trade_neighborhood_field_label),
                    placeholder = stringResource(R.string.auth_register_trade_neighborhood_hint),
                    isLoading = uiState.isSearchingBarrios,
                    isError = uiState.neighborhoodError != null,
                    supportingText = uiState.neighborhoodError,
                    focusRequester = fieldNavigator.focusRequester(TradeField.Neighborhood),
                    errorText = uiState.barrioSearchError,
                    onRetry = { onEvent(RegisterTradeEvent.RetryNeighborhoodSearch) },
                    onFocused = { onEvent(RegisterTradeEvent.NeighborhoodFocused) },
                    onSuggestionsDismissed = { onEvent(RegisterTradeEvent.NeighborhoodSuggestionsDismissed) },
                )
            }
            RegisterTradeLabeledSection(
                title = stringResource(R.string.auth_register_trade_service_location_label),
                modifier = Modifier.anchorFirstInvalid(fieldNavigator, TradeField.ServiceLocation),
            ) {
                OficiosCheckbox(
                    checked = uiState.servesAtClientHome,
                    onCheckedChange = { onEvent(RegisterTradeEvent.ServesAtClientHomeChanged(it)) },
                    label = stringResource(R.string.auth_register_trade_serves_at_home),
                    compactRow = true,
                )
                OficiosCheckbox(
                    checked = uiState.servesAtLocal,
                    onCheckedChange = { onEvent(RegisterTradeEvent.ServesAtLocalChanged(it)) },
                    label = stringResource(R.string.auth_register_trade_serves_at_local),
                    compactRow = true,
                )
                uiState.serviceLocationError?.let { FieldInlineError(it) }
            }
        }

        if (showAgenda) {
            RegisterTradeLabeledSection(
                title = stringResource(R.string.auth_register_trade_work_days_label),
                modifier = Modifier.anchorFirstInvalid(fieldNavigator, TradeField.Schedule),
            ) {
                WeeklyScheduleEditor(
                    days = uiState.weeklySchedule.toEditorDays(
                        RegisterTradeUiState.defaultWorkDays.toMap(),
                    ),
                    includeSunday = uiState.weeklySchedule.includeSunday,
                    onIncludeSundayChange = { onEvent(RegisterTradeEvent.IncludeSunday(it)) },
                    onToggleDay = { day, enabled -> onEvent(RegisterTradeEvent.WeekDayToggled(day, enabled)) },
                    onRangeFromChanged = { day, index, value ->
                        onEvent(RegisterTradeEvent.WeekRangeFrom(day, index, value))
                    },
                    onRangeToChanged = { day, index, value ->
                        onEvent(RegisterTradeEvent.WeekRangeTo(day, index, value))
                    },
                    onAddRange = { onEvent(RegisterTradeEvent.AddWeekRange(it)) },
                    onRemoveRange = { day, index -> onEvent(RegisterTradeEvent.RemoveWeekRange(day, index)) },
                    onInsertRange = { day, index, range ->
                        onEvent(RegisterTradeEvent.InsertWeekRange(day, index, range.from, range.to))
                    },
                    onCopyMondayToWeekdays = { onEvent(RegisterTradeEvent.CopyMondayToWeekdays) },
                )
                uiState.scheduleError?.let { FieldInlineError(it) }
            }
            RegisterTradeLabeledSection(
                title = stringResource(R.string.auth_register_trade_turns_label),
            ) {
                OficiosCheckbox(
                    checked = uiState.requiresTurns,
                    onCheckedChange = { onEvent(RegisterTradeEvent.RequiresTurnsChanged(it)) },
                    label = stringResource(R.string.auth_register_trade_requires_turns),
                    compactRow = true,
                )
                if (uiState.requiresTurns) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    ) {
                        val turnError = uiState.turnDurationError
                        OficiosTextField(
                            value = uiState.turnDurationMinutes,
                            onValueChange = { onEvent(RegisterTradeEvent.TurnDurationChanged(it)) },
                            label = stringResource(R.string.auth_register_trade_turn_duration),
                            placeholder = "60",
                            isError = turnError != null,
                            supportingText = turnError
                                ?: stringResource(R.string.auth_register_trade_turn_duration_hint),
                            focusRequester = fieldNavigator.focusRequester(TradeField.TurnDuration),
                            modifier = Modifier.weight(1f),
                        )
                        OficiosTextField(
                            value = uiState.turnIntervalMinutes,
                            onValueChange = { onEvent(RegisterTradeEvent.TurnIntervalChanged(it)) },
                            label = stringResource(R.string.auth_register_trade_turn_interval),
                            placeholder = "0",
                            supportingText = stringResource(R.string.auth_register_trade_turn_interval_hint),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (showCobro) {
            RegisterTradeLabeledSection(
                title = stringResource(R.string.auth_register_trade_payment_label),
                modifier = Modifier.anchorFirstInvalid(fieldNavigator, TradeField.Payment),
            ) {
                Text(
                    text = stringResource(R.string.auth_register_trade_payment_hint),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OficiosCheckbox(
                    checked = ProviderPaymentMethods.EFECTIVO in uiState.selectedPaymentMethods,
                    onCheckedChange = {
                        onEvent(RegisterTradeEvent.PaymentMethodToggled(ProviderPaymentMethods.EFECTIVO, it))
                    },
                    label = stringResource(R.string.auth_register_trade_payment_efectivo),
                    compactRow = true,
                )
                OficiosCheckbox(
                    checked = ProviderPaymentMethods.TRANSFERENCIA in uiState.selectedPaymentMethods,
                    onCheckedChange = {
                        onEvent(RegisterTradeEvent.PaymentMethodToggled(ProviderPaymentMethods.TRANSFERENCIA, it))
                    },
                    label = stringResource(R.string.auth_register_trade_payment_transferencia),
                    compactRow = true,
                )
                OficiosCheckbox(
                    checked = ProviderPaymentMethods.MERCADOPAGO in uiState.selectedPaymentMethods,
                    onCheckedChange = {
                        onEvent(RegisterTradeEvent.PaymentMethodToggled(ProviderPaymentMethods.MERCADOPAGO, it))
                    },
                    label = stringResource(R.string.auth_register_trade_payment_mercadopago),
                    compactRow = true,
                )
                uiState.paymentError?.let { FieldInlineError(it) }
            }
        }

        if (showIdentidad && uiState.servesAtLocal) {
            RegisterTradeLabeledSection(
                title = stringResource(R.string.auth_wizard_local_address),
            ) {
                OficiosTextField(
                    value = uiState.localCalle,
                    onValueChange = { onEvent(RegisterTradeEvent.LocalCalleChanged(it)) },
                    label = stringResource(R.string.auth_wizard_street),
                    isError = uiState.localCalleError != null,
                    supportingText = uiState.localCalleError,
                    focusRequester = fieldNavigator.focusRequester(TradeField.LocalCalle),
                    modifier = Modifier.fillMaxWidth(),
                )
                OficiosTextField(
                    value = uiState.localNumero,
                    onValueChange = { onEvent(RegisterTradeEvent.LocalNumeroChanged(it)) },
                    label = stringResource(R.string.auth_wizard_number),
                    isError = uiState.localNumeroError != null,
                    supportingText = uiState.localNumeroError,
                    focusRequester = fieldNavigator.focusRequester(TradeField.LocalNumero),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (showContinue) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = OficiosComponentSize.buttonHeight),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OficiosButton(
                    text = continueLabel ?: stringResource(R.string.auth_continue),
                    onClick = { onContinue?.invoke() ?: onEvent(RegisterTradeEvent.Continue) },
                    variant = OficiosButtonVariant.Primary,
                    size = OficiosButtonSize.Form,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    loading = uiState.isLoading,
                )
            }
        }
    }
}

@Composable
private fun RegisterTradeLabeledSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Composable
private fun RegisterTradeCenteredAction(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        OficiosButton(
            text = text,
            onClick = onClick,
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}

@Composable
private fun RegisterTradeSpecializationsSection(
    options: List<CatalogOption>,
    isLoading: Boolean,
    selected: Set<String>,
    descriptionValue: String,
    errorText: String? = null,
    sectionModifier: Modifier = Modifier,
    onToggle: (String, Boolean) -> Unit,
    onDescriptionChanged: (String) -> Unit,
) {
    Column(
        modifier = sectionModifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.none)) {
            Text(
                text = stringResource(R.string.auth_register_trade_specializations_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (isLoading) {
                Text(
                    text = stringResource(R.string.auth_register_trade_loading_specializations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            options.forEach { option ->
                val optionId = option.id.toString()
                OficiosCheckbox(
                    checked = optionId in selected,
                    onCheckedChange = { onToggle(optionId, it) },
                    label = option.label,
                    enabled = !isLoading,
                    compactRow = true,
                )
            }
            errorText?.let { FieldInlineError(it) }
        }

        // Campo libre y siempre visible: el catálogo de especializaciones es fijo,
        // pero un trabajador puede ofrecer varias cosas más allá de esas casillas.
        // No reemplaza a las especializaciones (que son las que se usan para que
        // los clientes lo encuentren en la búsqueda) — es información adicional.
        OficiosTextField(
            value = descriptionValue,
            onValueChange = onDescriptionChanged,
            label = stringResource(R.string.auth_register_trade_description_label),
            placeholder = stringResource(R.string.auth_register_trade_description_hint),
            singleLine = false,
        )
    }
}

@Composable
private fun FieldInlineError(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            error(message)
        },
    )
}

private enum class TradeField {
    Category,
    Trade,
    Specialization,
    Experience,
    Neighborhood,
    ServiceLocation,
    Schedule,
    TurnDuration,
    Payment,
    LocalCalle,
    LocalNumero,
}

private fun WeeklyAvailability.toEditorDays(labels: Map<String, String>): List<WeeklyScheduleDayUi> {
    val keys = if (includeSunday) WeeklyAvailability.ALL_DAYS else WeeklyAvailability.WEEKDAYS
    return keys.map { key ->
        val ranges = rangesOn(key)
        WeeklyScheduleDayUi(
            key = key,
            label = labels[key] ?: key,
            enabled = ranges.isNotEmpty(),
            ranges = ranges.map { WeeklyScheduleRangeUi(it.start, it.end) },
        )
    }
}
