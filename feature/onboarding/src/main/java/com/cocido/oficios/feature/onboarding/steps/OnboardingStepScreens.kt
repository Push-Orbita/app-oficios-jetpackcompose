package com.cocido.oficios.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.badges.OficiosBadge
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPhoneField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOption
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.picker.rememberImageSourcePicker
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.client.ClientContactDomicilioUiState
import com.cocido.oficios.feature.onboarding.client.ClientContactoUiState
import com.cocido.oficios.feature.onboarding.client.ClientDomicilioUiState
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.feature.onboarding.provider.ProviderDniUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderSelfieUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderLocalUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderPaymentMethodsUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderProfessionalInfoUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderAvailabilityUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderRubrosUiState

@Composable
fun OnboardingStepScaffold(
    title: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isLoading: Boolean = false,
    canSubmit: Boolean = true,
    submitText: String? = null,
    onSubmit: () -> Unit,
    content: @Composable () -> Unit,
) {
    val buttonText = submitText ?: stringResource(R.string.onboarding_step_save)
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(OficiosSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            if (error != null) OficiosInlineAlert(message = error)
            content()
            OficiosButton(
                text = buttonText,
                onClick = onSubmit,
                enabled = canSubmit && !isLoading,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ClientContactDomicilioStepScreen(
    uiState: ClientContactDomicilioUiState,
    onNombreChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onCountrySelected: (PhoneCountryOption) -> Unit,
    onEmailChanged: (String) -> Unit,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioSelected: (com.cocido.oficios.domain.model.CatalogOption) -> Unit,
    onBarrioSuggestionsDismissed: () -> Unit,
    onCalleChanged: (String) -> Unit,
    onNumeroChanged: (String) -> Unit,
    onPisoChanged: (String) -> Unit,
    onDepartamentoChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_contact_domicilio_title),
        error = uiState.error,
        isLoading = uiState.isLoading || uiState.isPreloading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_contact_domicilio_contact_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.onboarding_step_contact_domicilio_contact_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OficiosTextField(
            value = uiState.nombre,
            onValueChange = onNombreChanged,
            label = stringResource(R.string.onboarding_step_contact_nombre_label),
        )
        OficiosPhoneField(
            value = uiState.telefono,
            onValueChange = onTelefonoChanged,
            selectedCountry = uiState.selectedCountry,
            onCountrySelected = onCountrySelected,
            label = stringResource(R.string.onboarding_step_contact_whatsapp_label),
        )
        OficiosTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.onboarding_step_contact_email_label),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Text(
            text = stringResource(R.string.onboarding_step_contact_domicilio_address_section),
            style = MaterialTheme.typography.titleMedium,
        )
        OficiosAutocompleteField(
            value = uiState.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            suggestions = uiState.barrioOptions,
            onSuggestionSelected = onBarrioSelected,
            suggestionLabel = { it.label },
            label = stringResource(R.string.onboarding_step_barrio_label),
            placeholder = stringResource(R.string.onboarding_step_barrio_hint),
            onSuggestionsDismissed = onBarrioSuggestionsDismissed,
        )
        OficiosTextField(
            value = uiState.calle,
            onValueChange = onCalleChanged,
            label = stringResource(R.string.onboarding_step_calle_label),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            OficiosTextField(
                value = uiState.numero,
                onValueChange = onNumeroChanged,
                label = stringResource(R.string.onboarding_step_numero_label),
                modifier = Modifier.weight(1f),
            )
            OficiosTextField(
                value = uiState.piso,
                onValueChange = onPisoChanged,
                label = stringResource(R.string.onboarding_step_piso_label),
                modifier = Modifier.weight(1f),
            )
            OficiosTextField(
                value = uiState.departamento,
                onValueChange = onDepartamentoChanged,
                label = stringResource(R.string.onboarding_step_departamento_label),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun ClientDomicilioStepScreen(
    uiState: ClientDomicilioUiState,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioSelected: (com.cocido.oficios.domain.model.CatalogOption) -> Unit,
    onBarrioSuggestionsDismissed: () -> Unit,
    onCalleChanged: (String) -> Unit,
    onNumeroChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_domicilio_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        OficiosAutocompleteField(
            value = uiState.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            suggestions = uiState.barrioOptions,
            onSuggestionSelected = onBarrioSelected,
            suggestionLabel = { it.label },
            label = stringResource(R.string.onboarding_step_barrio_label),
            placeholder = stringResource(R.string.onboarding_step_barrio_hint),
            onSuggestionsDismissed = onBarrioSuggestionsDismissed,
        )
        OficiosTextField(
            value = uiState.calle,
            onValueChange = onCalleChanged,
            label = stringResource(R.string.onboarding_step_calle_label),
        )
        OficiosTextField(
            value = uiState.numero,
            onValueChange = onNumeroChanged,
            label = stringResource(R.string.onboarding_step_numero_label),
        )
    }
}

@Composable
fun ClientContactoStepScreen(
    uiState: ClientContactoUiState,
    onNombreChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_contacto_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        OficiosTextField(value = uiState.nombre, onValueChange = onNombreChanged, label = stringResource(R.string.onboarding_step_contact_nombre_label))
        OficiosTextField(value = uiState.telefono, onValueChange = onTelefonoChanged, label = stringResource(R.string.onboarding_step_contact_whatsapp_label))
        OficiosTextField(value = uiState.email, onValueChange = onEmailChanged, label = stringResource(R.string.onboarding_step_contact_email_label))
    }
}

@Composable
fun ProviderDniStepScreen(
    uiState: ProviderDniUiState,
    onDocumentoChanged: (String) -> Unit,
    onFrontImage: (String?) -> Unit,
    onBackImage: (String?) -> Unit,
    onSubmit: () -> Unit,
) {
    val pickerTitle = stringResource(R.string.onboarding_image_picker_title)
    val cameraLabel = stringResource(R.string.onboarding_image_picker_camera)
    val galleryLabel = stringResource(R.string.onboarding_image_picker_gallery)
    val cancelLabel = stringResource(R.string.onboarding_image_picker_cancel)

    val frontPicker = rememberImageSourcePicker(
        title = pickerTitle,
        cameraLabel = cameraLabel,
        galleryLabel = galleryLabel,
        cancelLabel = cancelLabel,
        onImageSelected = onFrontImage,
    )
    val backPicker = rememberImageSourcePicker(
        title = pickerTitle,
        cameraLabel = cameraLabel,
        galleryLabel = galleryLabel,
        cancelLabel = cancelLabel,
        onImageSelected = onBackImage,
    )

    val canSubmit = uiState.documento.isNotBlank() &&
        (uiState.frontImagePath != null || uiState.frontRemoteLoaded) &&
        (uiState.backImagePath != null || uiState.backRemoteLoaded)

    OficiosScaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(OficiosSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.onboarding_step_dni_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            if (uiState.error != null) {
                OficiosInlineAlert(message = uiState.error)
            }

            DniStepSection(
                title = stringResource(R.string.onboarding_step_dni_number_section),
                description = stringResource(R.string.onboarding_step_dni_number_hint),
            ) {
                OficiosTextField(
                    value = uiState.documento,
                    onValueChange = onDocumentoChanged,
                    label = stringResource(R.string.onboarding_step_dni_number_label),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            DniStepSection(
                title = stringResource(R.string.onboarding_step_dni_photos_section),
                description = stringResource(R.string.onboarding_step_dni_photos_hint),
            ) {
                DniImageUploadCard(
                    title = stringResource(R.string.onboarding_step_dni_front_title),
                    subtitle = stringResource(R.string.onboarding_step_dni_front_subtitle),
                    isLoaded = uiState.frontImagePath != null || uiState.frontRemoteLoaded,
                    onClick = frontPicker.requestImage,
                )
                DniImageUploadCard(
                    title = stringResource(R.string.onboarding_step_dni_back_title),
                    subtitle = stringResource(R.string.onboarding_step_dni_back_subtitle),
                    isLoaded = uiState.backImagePath != null || uiState.backRemoteLoaded,
                    onClick = backPicker.requestImage,
                )
                OficiosButton(
                    text = stringResource(R.string.onboarding_step_submit_dni),
                    onClick = onSubmit,
                    enabled = canSubmit && !uiState.isLoading,
                    loading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ProviderSelfieStepScreen(
    uiState: ProviderSelfieUiState,
    onImageSelected: (String?) -> Unit,
    onSubmit: () -> Unit,
) {
    val pickerTitle = stringResource(R.string.onboarding_image_picker_title)
    val cameraLabel = stringResource(R.string.onboarding_image_picker_camera)
    val galleryLabel = stringResource(R.string.onboarding_image_picker_gallery)
    val cancelLabel = stringResource(R.string.onboarding_image_picker_cancel)

    val selfiePicker = rememberImageSourcePicker(
        title = pickerTitle,
        cameraLabel = cameraLabel,
        galleryLabel = galleryLabel,
        cancelLabel = cancelLabel,
        onImageSelected = onImageSelected,
    )

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_selfie_title),
        error = uiState.error,
        isLoading = uiState.isLoading || uiState.isPreloading,
        canSubmit = uiState.imagePath != null,
        submitText = stringResource(R.string.onboarding_step_selfie_submit),
        onSubmit = onSubmit,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_selfie_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (uiState.rejectionReason != null) {
            OficiosInlineAlert(message = uiState.rejectionReason)
        }
        DniImageUploadCard(
            title = stringResource(R.string.onboarding_step_selfie_card_title),
            subtitle = stringResource(R.string.onboarding_step_selfie_card_subtitle),
            isLoaded = uiState.imagePath != null || uiState.remoteLoaded,
            onClick = selfiePicker.requestImage,
        )
    }
}

@Composable
private fun DniStepSection(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun DniImageUploadCard(
    title: String,
    subtitle: String,
    isLoaded: Boolean,
    onClick: () -> Unit,
) {
    OficiosCard(
        onClick = onClick,
        selected = isLoaded,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isLoaded) Icons.Outlined.CheckCircle else Icons.Outlined.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(OficiosSpacing.lg),
                tint = if (isLoaded) OficiosColorSuccess else MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = if (isLoaded) stringResource(R.string.onboarding_step_dni_status_loaded) else subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OficiosBadge(
                label = stringResource(
                    if (isLoaded) {
                        R.string.onboarding_step_dni_status_loaded
                    } else {
                        R.string.onboarding_step_dni_status_pending
                    },
                ),
                containerColor = if (isLoaded) {
                    OficiosColorSuccess
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isLoaded) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
fun ProviderProfessionalInfoStepScreen(
    uiState: ProviderProfessionalInfoUiState,
    onDescripcionChanged: (String) -> Unit,
    onExperienciaChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_professional_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.descripcion.isNotBlank() && uiState.experiencia.isNotBlank(),
        onSubmit = onSubmit,
    ) {
        OficiosTextField(
            value = uiState.descripcion,
            onValueChange = onDescripcionChanged,
            label = stringResource(R.string.onboarding_step_professional_description_label),
        )
        OficiosTextField(
            value = uiState.experiencia,
            onValueChange = onExperienciaChanged,
            label = stringResource(R.string.onboarding_step_professional_experience_label),
        )
    }
}

@Composable
fun ProviderRubrosStepScreen(
    uiState: ProviderRubrosUiState,
    onCategorySelected: (Long) -> Unit,
    onTradeSelected: (Long) -> Unit,
    onSubRubroToggled: (Long, Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_rubros_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        OficiosDropdownField(
            value = uiState.categoryOptions.find { it.id == uiState.selectedCategoryId }?.label.orEmpty(),
            options = uiState.categoryOptions.map { it.label },
            onOptionSelected = { label ->
                uiState.categoryOptions.find { it.label == label }?.let { onCategorySelected(it.id) }
            },
            label = stringResource(R.string.onboarding_step_rubros_category_label),
            placeholder = stringResource(R.string.onboarding_step_rubros_select_placeholder),
            usePlaceholderOnly = true,
        )
        OficiosDropdownField(
            value = uiState.tradeOptions.find { it.id == uiState.selectedTradeId }?.label.orEmpty(),
            options = uiState.tradeOptions.map { it.label },
            onOptionSelected = { label ->
                uiState.tradeOptions.find { it.label == label }?.let { onTradeSelected(it.id) }
            },
            label = stringResource(R.string.onboarding_step_rubros_trade_label),
            placeholder = stringResource(R.string.onboarding_step_rubros_select_placeholder),
            usePlaceholderOnly = true,
        )
        uiState.specializationOptions.forEach { option ->
            OficiosCheckbox(
                checked = option.id in uiState.selectedSubRubroIds,
                onCheckedChange = { onSubRubroToggled(option.id, it) },
                label = option.label,
                compactRow = true,
            )
        }
    }
}

@Composable
fun ProviderAvailabilityStepScreen(
    uiState: ProviderAvailabilityUiState,
    onWorkDayToggled: (String, Boolean) -> Unit,
    onTimeSlotFromChanged: (Int, String) -> Unit,
    onTimeSlotToChanged: (Int, String) -> Unit,
    onAddTimeSlot: () -> Unit,
    onServesAtClientHomeChanged: (Boolean) -> Unit,
    onServesAtLocalChanged: (Boolean) -> Unit,
    onRequiresTurnsChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_availability_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        AvailabilitySection(title = stringResource(R.string.onboarding_step_availability_work_days)) {
            uiState.workDayOptions.forEach { (dayId, dayLabel) ->
                OficiosCheckbox(
                    checked = dayId in uiState.selectedWorkDays,
                    onCheckedChange = { onWorkDayToggled(dayId, it) },
                    label = dayLabel,
                    compactRow = true,
                )
            }
        }

        AvailabilitySection(title = stringResource(R.string.onboarding_step_availability_service_location)) {
            OficiosCheckbox(
                checked = uiState.servesAtClientHome,
                onCheckedChange = onServesAtClientHomeChanged,
                label = stringResource(R.string.onboarding_step_availability_serves_at_home),
                compactRow = true,
            )
            OficiosCheckbox(
                checked = uiState.servesAtLocal,
                onCheckedChange = onServesAtLocalChanged,
                label = stringResource(R.string.onboarding_step_availability_serves_at_local),
                compactRow = true,
            )
            OficiosCheckbox(
                checked = uiState.requiresTurns,
                onCheckedChange = onRequiresTurnsChanged,
                label = stringResource(R.string.onboarding_step_availability_requires_turns),
                compactRow = true,
            )
        }

        AvailabilitySection(title = stringResource(R.string.onboarding_step_availability_schedule)) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.formSection)) {
                uiState.timeSlots.forEachIndexed { index, slot ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    ) {
                        OficiosDropdownField(
                            value = slot.from,
                            options = uiState.timeOptions,
                            onOptionSelected = { onTimeSlotFromChanged(index, it) },
                            label = stringResource(R.string.onboarding_step_availability_from),
                            placeholder = stringResource(R.string.onboarding_step_availability_from),
                            modifier = Modifier.weight(1f),
                            usePlaceholderOnly = true,
                        )
                        OficiosDropdownField(
                            value = slot.to,
                            options = uiState.timeOptions,
                            onOptionSelected = { onTimeSlotToChanged(index, it) },
                            label = stringResource(R.string.onboarding_step_availability_to),
                            placeholder = stringResource(R.string.onboarding_step_availability_to),
                            modifier = Modifier.weight(1f),
                            usePlaceholderOnly = true,
                        )
                    }
                }
                OficiosButton(
                    text = stringResource(R.string.onboarding_step_availability_add_schedule),
                    onClick = onAddTimeSlot,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ProviderPaymentMethodsStepScreen(
    uiState: ProviderPaymentMethodsUiState,
    onMethodToggled: (String, Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_payment_methods_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_payment_methods_hint),
            style = MaterialTheme.typography.bodyMedium,
        )
        PaymentMethodCheckbox(
            method = ProviderPaymentMethods.EFECTIVO,
            label = stringResource(R.string.onboarding_step_payment_efectivo),
            checked = ProviderPaymentMethods.EFECTIVO in uiState.selectedMethods,
            onCheckedChange = { onMethodToggled(ProviderPaymentMethods.EFECTIVO, it) },
        )
        PaymentMethodCheckbox(
            method = ProviderPaymentMethods.TRANSFERENCIA,
            label = stringResource(R.string.onboarding_step_payment_transferencia),
            checked = ProviderPaymentMethods.TRANSFERENCIA in uiState.selectedMethods,
            onCheckedChange = { onMethodToggled(ProviderPaymentMethods.TRANSFERENCIA, it) },
        )
        PaymentMethodCheckbox(
            method = ProviderPaymentMethods.MERCADOPAGO,
            label = stringResource(R.string.onboarding_step_payment_mercadopago),
            checked = ProviderPaymentMethods.MERCADOPAGO in uiState.selectedMethods,
            onCheckedChange = { onMethodToggled(ProviderPaymentMethods.MERCADOPAGO, it) },
        )
    }
}

@Composable
private fun PaymentMethodCheckbox(
    method: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    OficiosCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = label,
        compactRow = true,
    )
}

@Composable
private fun AvailabilitySection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
fun ProviderLocalStepScreen(
    uiState: ProviderLocalUiState,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioSelected: (com.cocido.oficios.domain.model.CatalogOption) -> Unit,
    onBarrioSuggestionsDismissed: () -> Unit,
    onCalleChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_local_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.selectedBarrioId != null && uiState.calle.isNotBlank(),
        onSubmit = onSubmit,
    ) {
        OficiosAutocompleteField(
            value = uiState.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            suggestions = uiState.barrioOptions,
            onSuggestionSelected = onBarrioSelected,
            suggestionLabel = { it.label },
            label = stringResource(R.string.onboarding_step_barrio_label),
            placeholder = stringResource(R.string.onboarding_step_barrio_hint),
            onSuggestionsDismissed = onBarrioSuggestionsDismissed,
        )
        OficiosTextField(value = uiState.calle, onValueChange = onCalleChanged, label = stringResource(R.string.onboarding_step_calle_label))
    }
}
