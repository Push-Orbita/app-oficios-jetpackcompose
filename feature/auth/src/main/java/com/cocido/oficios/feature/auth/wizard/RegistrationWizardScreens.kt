package com.cocido.oficios.feature.auth.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPhoneField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphSubtitleStyle
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthRegisterLayout
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler
import com.cocido.oficios.feature.auth.register.RegisterFormScreen
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.cards.OficiosSectionCard
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.feature.auth.register.RegisterMode

@Composable
fun RegistrationWizardPersonalScreen(
    state: RegistrationWizardUiState,
    onEvent: (com.cocido.oficios.feature.auth.register.RegisterFormEvent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = hasPersonalDraftForLeaveConfirmation(state.personal),
        onConfirmExit = onBack,
    )
    RegisterFormScreen(
        mode = if (state.role == RegistrationRole.CLIENT) RegisterMode.CLIENT else RegisterMode.PROVIDER,
        uiState = state.personal,
        onEvent = onEvent,
        modifier = modifier,
        submitLabel = stringResource(R.string.auth_next_button),
        onBack = handleBack,
    )
}

@Composable
fun RegistrationWizardProfileScreen(
    state: RegistrationWizardUiState,
    onContactNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioFocused: () -> Unit,
    onBarrioSelected: (CatalogOption) -> Unit,
    onBarrioDismissed: () -> Unit,
    onBarrioRetry: () -> Unit = {},
    onCalleChanged: (String) -> Unit,
    onNumeroChanged: (String) -> Unit,
    onPisoChanged: (String) -> Unit,
    onDepartamentoChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AuthRegisterLayout(
        modifier = modifier,
        sectionSpacing = OficiosSpacing.md,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.auth_wizard_profile_title),
                onBack = onBack,
            )
        },
        stickyFooter = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OficiosSpacing.xl)
                    .padding(top = OficiosSpacing.sm, bottom = OficiosSpacing.md),
            ) {
                OficiosButton(
                    text = stringResource(R.string.auth_next_button),
                    onClick = onContinue,
                    enabled = !state.isCommitting,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Primary,
                    size = OficiosButtonSize.Form,
                )
            }
        },
    ) {
        Text(
            text = stringResource(R.string.auth_wizard_profile_subtitle),
            style = OficiosAuthGlyphSubtitleStyle,
        )
        state.profileError?.let { OficiosInlineAlert(it, modifier = Modifier.fillMaxWidth()) }
        OficiosTextField(
            value = state.contactName,
            onValueChange = onContactNameChanged,
            label = stringResource(R.string.auth_wizard_contact_name),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosPhoneField(
            value = state.phone,
            onValueChange = onPhoneChanged,
            selectedCountry = state.selectedCountry,
            label = stringResource(R.string.auth_wizard_whatsapp),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosAutocompleteField(
            value = state.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            suggestions = state.barrioOptions,
            onSuggestionSelected = onBarrioSelected,
            suggestionLabel = { it.label },
            label = stringResource(R.string.auth_wizard_barrio),
            isLoading = state.isSearchingBarrios,
            errorText = state.barrioSearchError,
            onRetry = onBarrioRetry,
            onFocused = onBarrioFocused,
            onSuggestionsDismissed = onBarrioDismissed,
        )
        OficiosTextField(
            value = state.calle,
            onValueChange = onCalleChanged,
            label = stringResource(R.string.auth_wizard_street),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosTextField(
            value = state.numero,
            onValueChange = onNumeroChanged,
            label = stringResource(R.string.auth_wizard_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosTextField(
            value = state.piso,
            onValueChange = onPisoChanged,
            label = stringResource(R.string.auth_wizard_floor),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosTextField(
            value = state.departamento,
            onValueChange = onDepartamentoChanged,
            label = stringResource(R.string.auth_wizard_apartment),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


@Composable
fun RegistrationWizardReviewScreen(
    state: RegistrationWizardUiState,
    onCreateAccount: () -> Unit,
    onBack: () -> Unit = {},
    onEditPersonal: () -> Unit = {},
    onEditContact: () -> Unit = {},
    onEditAddress: () -> Unit = {},
    onEditOficio: () -> Unit = {},
    onEditZona: () -> Unit = {},
    onEditAgenda: () -> Unit = {},
    onEditCobro: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val review = state.review
    val isProvider = state.role == RegistrationRole.PROVIDER
    val canEdit = !state.isCommitting

    OficiosScaffold(
        modifier = modifier.fillMaxSize(),
        isLoading = state.isCommitting,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.auth_wizard_review_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = OficiosSpacing.xl)
                    .padding(top = OficiosSpacing.md, bottom = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                state.commitError?.let { OficiosInlineAlert(it, modifier = Modifier.fillMaxWidth()) }
                OficiosButton(
                    text = stringResource(R.string.auth_create_account),
                    onClick = onCreateAccount,
                    enabled = canEdit,
                    loading = state.isCommitting,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Primary,
                    size = OficiosButtonSize.Form,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OficiosSpacing.xl)
                .padding(top = OficiosSpacing.md, bottom = OficiosSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.auth_wizard_review_subtitle),
                style = OficiosAuthGlyphSubtitleStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ReviewSectionCard(
                title = stringResource(R.string.auth_wizard_review_section_personal),
                leadingIcon = OficiosIcons.User,
                onEdit = onEditPersonal,
                editEnabled = canEdit,
                editContentDescription = stringResource(R.string.auth_wizard_review_edit_personal),
            ) {
                ReviewField(stringResource(R.string.auth_first_name_label), review.firstName)
                ReviewField(stringResource(R.string.auth_last_name_label), review.lastName)
                ReviewField(stringResource(R.string.auth_email_electronic_label), review.email)
                ReviewField(stringResource(R.string.auth_document_label), review.document)
            }
            ReviewSectionCard(
                title = stringResource(R.string.auth_wizard_review_section_contact),
                leadingIcon = OficiosIcons.BookUser,
                onEdit = onEditContact.takeUnless { isProvider },
                editEnabled = canEdit,
                editContentDescription = stringResource(R.string.auth_wizard_review_edit_contact),
            ) {
                if (isProvider) {
                    Text(
                        text = stringResource(R.string.auth_wizard_review_phone_not_asked),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    ReviewField(stringResource(R.string.auth_wizard_contact_name), review.contactName)
                    ReviewField(stringResource(R.string.auth_wizard_whatsapp), review.phone)
                }
            }
            if (isProvider) {
                ReviewSectionCard(
                    title = stringResource(R.string.auth_wizard_review_section_trade),
                    leadingIcon = OficiosIcons.Hammer,
                    onEdit = onEditOficio,
                    editEnabled = canEdit,
                    editContentDescription = stringResource(R.string.auth_wizard_review_edit_trade),
                ) {
                    ReviewField(stringResource(R.string.auth_register_trade_category_label), review.oficioCategory)
                    ReviewField(stringResource(R.string.auth_register_trade_trade_label), review.oficioRubro)
                    ReviewField(stringResource(R.string.auth_register_trade_experience_field_label), review.experienceRange)
                }
                ReviewSectionCard(
                    title = stringResource(R.string.auth_wizard_review_section_zone),
                    leadingIcon = OficiosIcons.MapPinHouse,
                    onEdit = onEditZona,
                    editEnabled = canEdit,
                    editContentDescription = stringResource(R.string.auth_wizard_review_edit_zone),
                ) {
                    ReviewField(stringResource(R.string.auth_wizard_barrio), review.barrio)
                    ReviewField(stringResource(R.string.auth_wizard_review_service_area), review.serviceArea)
                    if (review.showStreet) {
                        ReviewField(stringResource(R.string.auth_wizard_street), review.street)
                    }
                }
                ReviewSectionCard(
                    title = stringResource(R.string.auth_wizard_review_section_agenda),
                    leadingIcon = OficiosIcons.Clock,
                    onEdit = onEditAgenda,
                    editEnabled = canEdit,
                    editContentDescription = stringResource(R.string.auth_wizard_review_edit_agenda),
                ) {
                    ReviewField(stringResource(R.string.auth_wizard_review_work_days), review.workDays)
                    ReviewField(stringResource(R.string.auth_wizard_review_schedule), review.schedule)
                }
                ReviewSectionCard(
                    title = stringResource(R.string.auth_wizard_review_section_payment),
                    leadingIcon = OficiosIcons.Banknote,
                    onEdit = onEditCobro,
                    editEnabled = canEdit,
                    editContentDescription = stringResource(R.string.auth_wizard_review_edit_payment),
                ) {
                    ReviewField(stringResource(R.string.auth_register_trade_payment_label).trimEnd(':'), review.paymentMethods)
                }
            } else {
                ReviewSectionCard(
                    title = stringResource(R.string.auth_wizard_review_section_address),
                    leadingIcon = OficiosIcons.House,
                    onEdit = onEditAddress,
                    editEnabled = canEdit,
                    editContentDescription = stringResource(R.string.auth_wizard_review_edit_address),
                ) {
                    ReviewField(stringResource(R.string.auth_wizard_barrio), review.barrio)
                    ReviewField(stringResource(R.string.auth_wizard_street), review.street)
                    ReviewField(stringResource(R.string.auth_wizard_floor), review.piso)
                    ReviewField(stringResource(R.string.auth_wizard_apartment), review.departamento)
                }
            }
        }
    }
}

@Composable
private fun ReviewSectionCard(
    title: String,
    @DrawableRes leadingIcon: Int,
    onEdit: (() -> Unit)?,
    editEnabled: Boolean,
    editContentDescription: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    OficiosSectionCard(
        title = title,
        leadingIcon = leadingIcon,
        actionLabel = if (onEdit != null) stringResource(R.string.auth_wizard_review_edit) else null,
        onAction = onEdit.takeIf { editEnabled },
        actionContentDescription = editContentDescription,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), content = content)
    }
}

@Composable
private fun ReviewField(label: String, value: String) {
    val empty = value.isBlank()
    val display = if (empty) stringResource(R.string.auth_wizard_review_empty) else value
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs / 2)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = display,
            style = MaterialTheme.typography.bodyLarge,
            color = if (empty) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

internal fun hasPersonalDraftForLeaveConfirmation(
    personal: com.cocido.oficios.feature.auth.register.RegisterFormUiState,
): Boolean {
    return personal.firstName.isNotBlank() ||
        personal.lastName.isNotBlank() ||
        personal.email.isNotBlank() ||
        personal.document.isNotBlank() ||
        personal.password.isNotBlank() ||
        personal.acceptedTerms
}