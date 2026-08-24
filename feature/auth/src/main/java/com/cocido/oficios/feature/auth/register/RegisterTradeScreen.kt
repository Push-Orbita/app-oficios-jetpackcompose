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
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.categories.CategoryGridItem
import com.cocido.oficios.core.designsystem.component.categories.OficiosCategoriesGrid
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRegisterTradeTitleStyle
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthRegisterLayout

@Composable
fun RegisterTradeScreen(
    uiState: RegisterTradeUiState,
    onEvent: (RegisterTradeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthRegisterLayout(
        modifier = modifier,
        isLoading = uiState.isLoading || uiState.isLoadingCatalog,
    ) {
        Text(
            text = stringResource(R.string.auth_register_trade_title),
            style = OficiosRegisterTradeTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
        )

        uiState.catalogError?.let { message ->
            OficiosInlineAlert(message = message)
            OficiosButton(
                text = stringResource(R.string.auth_register_trade_retry_catalog),
                onClick = { onEvent(RegisterTradeEvent.RetryLoadCatalog) },
                variant = OficiosButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (uiState.selectedCategoryId == null && uiState.categoryOptions.isNotEmpty()) {
            Text(
                text = stringResource(R.string.auth_register_trade_category_label),
                style = MaterialTheme.typography.titleMedium,
            )
            OficiosCategoriesGrid(
                categories = uiState.categoryOptions.map { CategoryGridItem(it.id, it.label) },
                onCategoryClick = { item ->
                    uiState.categoryOptions.find { it.id == item.id }?.let { option ->
                        onEvent(RegisterTradeEvent.CategoryGridSelected(option))
                    }
                },
            )
        } else {
            OficiosDropdownField(
                value = uiState.selectedCategory,
                options = uiState.categories,
                onOptionSelected = { onEvent(RegisterTradeEvent.CategoryChanged(it)) },
                label = stringResource(R.string.auth_register_trade_category_label),
                placeholder = stringResource(R.string.auth_register_trade_category_hint),
                enabled = !uiState.isLoadingCatalog && uiState.categoryOptions.isNotEmpty(),
                usePlaceholderOnly = true,
            )

            OficiosDropdownField(
                value = uiState.selectedTrade,
                options = uiState.trades,
                onOptionSelected = { onEvent(RegisterTradeEvent.TradeChanged(it)) },
                label = stringResource(R.string.auth_register_trade_trade_label),
                placeholder = stringResource(R.string.auth_register_trade_trade_hint),
                enabled = uiState.isTradeEnabled && !uiState.isLoadingTrades,
                usePlaceholderOnly = true,
            )

            RegisterTradeSpecializationsSection(
                options = uiState.specializationOptions,
                isLoading = uiState.isLoadingSpecializations,
                selected = uiState.selectedSpecializations,
                descriptionValue = uiState.otherSpecialization,
                onToggle = { id, checked -> onEvent(RegisterTradeEvent.SpecializationToggled(id, checked)) },
                onDescriptionChanged = { onEvent(RegisterTradeEvent.OtherSpecializationChanged(it)) },
            )
        }

        if (uiState.selectedCategoryId != null) {
        RegisterTradeLabeledSection(
            title = stringResource(R.string.auth_register_trade_experience_label),
        ) {
            OficiosDropdownField(
                value = uiState.selectedExperienceRange,
                options = uiState.experienceRanges,
                onOptionSelected = { onEvent(RegisterTradeEvent.ExperienceRangeChanged(it)) },
                label = stringResource(R.string.auth_register_trade_experience_hint),
                placeholder = stringResource(R.string.auth_register_trade_experience_hint),
                usePlaceholderOnly = true,
            )
        }

        RegisterTradeLabeledSection(
            title = stringResource(R.string.auth_register_trade_neighborhood_label),
        ) {
            OficiosAutocompleteField(
                value = uiState.neighborhoodQuery,
                onValueChange = { onEvent(RegisterTradeEvent.NeighborhoodQueryChanged(it)) },
                suggestions = uiState.neighborhoodOptions,
                onSuggestionSelected = { onEvent(RegisterTradeEvent.NeighborhoodSelected(it)) },
                suggestionLabel = { it.label },
                label = "",
                placeholder = stringResource(R.string.auth_register_trade_neighborhood_hint),
                onSuggestionsDismissed = { onEvent(RegisterTradeEvent.NeighborhoodSuggestionsDismissed) },
            )
        }

        RegisterTradeLabeledSection(
            title = stringResource(R.string.auth_register_trade_work_days_label),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
                RegisterTradeUiState.defaultWorkDays.forEach { (dayId, dayLabel) ->
                    OficiosCheckbox(
                        checked = dayId in uiState.selectedWorkDays,
                        onCheckedChange = { onEvent(RegisterTradeEvent.WorkDayToggled(dayId, it)) },
                        label = dayLabel,
                        compactRow = true,
                    )
                }
            }
        }

        RegisterTradeLabeledSection(
            title = stringResource(R.string.auth_register_trade_service_location_label),
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
        }

        RegisterTradeLabeledSection(
            title = stringResource(R.string.auth_register_trade_schedule_label),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.formSection)) {
                uiState.timeSlots.forEachIndexed { index, slot ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    ) {
                        OficiosDropdownField(
                            value = slot.from,
                            options = uiState.timeOptions,
                            onOptionSelected = { onEvent(RegisterTradeEvent.TimeSlotFromChanged(index, it)) },
                            label = stringResource(R.string.auth_register_trade_from_label),
                            placeholder = stringResource(R.string.auth_register_trade_from_label),
                            modifier = Modifier.weight(1f),
                            usePlaceholderOnly = true,
                        )
                        OficiosDropdownField(
                            value = slot.to,
                            options = uiState.timeOptions,
                            onOptionSelected = { onEvent(RegisterTradeEvent.TimeSlotToChanged(index, it)) },
                            label = stringResource(R.string.auth_register_trade_to_label),
                            placeholder = stringResource(R.string.auth_register_trade_to_label),
                            modifier = Modifier.weight(1f),
                            usePlaceholderOnly = true,
                        )
                    }
                }
                RegisterTradeCenteredAction(
                    text = stringResource(R.string.auth_register_trade_add_schedule),
                    onClick = { onEvent(RegisterTradeEvent.AddTimeSlot) },
                )
            }
        }

        RegisterTradeLabeledSection(
            title = stringResource(R.string.auth_register_trade_payment_label),
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
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = OficiosComponentSize.buttonHeight),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OficiosButton(
                text = stringResource(R.string.auth_continue),
                onClick = { onEvent(RegisterTradeEvent.Continue) },
                variant = OficiosButtonVariant.Primary,
                size = OficiosButtonSize.Prominent,
                enabled = uiState.canContinue && !uiState.isLoading,
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
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.formSection),
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
            size = OficiosButtonSize.Prominent,
        )
    }
}

@Composable
private fun RegisterTradeSpecializationsSection(
    options: List<CatalogOption>,
    isLoading: Boolean,
    selected: Set<String>,
    descriptionValue: String,
    onToggle: (String, Boolean) -> Unit,
    onDescriptionChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                    color = OficiosColorMutedText,
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
