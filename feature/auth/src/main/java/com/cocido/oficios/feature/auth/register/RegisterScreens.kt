package com.cocido.oficios.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandRow
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.feature.auth.components.AuthScreenLayout
import com.cocido.oficios.feature.auth.components.AuthSupportCard
import com.cocido.oficios.feature.auth.components.RegisterRoleCard
import com.cocido.oficios.core.designsystem.R as DesignSystemR
import com.cocido.oficios.core.designsystem.component.inputs.FieldAndCtaBlock
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTermsCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.layout.OficiosPromptActionRow
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthRegisterLayout

@Composable
fun RegisterRoleSelectionScreen(
    onSelectClient: () -> Unit,
    onSelectProvider: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AuthScreenLayout(
        modifier = modifier,
        showBrand = false,
        support = {
            AuthSupportCard(
                onContactClick = onContactSupport,
                message = stringResource(R.string.auth_support_message_compact),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        ) {
            OficiosBrandRow(
                modifier = Modifier.fillMaxWidth(),
                iconWidth = OficiosComponentSize.authRegisterBrandIconWidth,
                spacing = OficiosSpacing.lg,
            )
            Text(
                text = stringResource(R.string.auth_register_role_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                RegisterRoleCard(
                    title = stringResource(R.string.auth_register_provider_card_title),
                    iconRes = DesignSystemR.drawable.ic_casco,
                    onClick = onSelectProvider,
                    iconHeight = OficiosComponentSize.registerRoleCardIconWorkerHeight,
                    iconRotationDegrees = 10f,
                    modifier = Modifier.weight(1f),
                )
                RegisterRoleCard(
                    title = stringResource(R.string.auth_register_client_card_title),
                    iconRes = DesignSystemR.drawable.ic_lupa,
                    onClick = onSelectClient,
                    iconHeight = OficiosComponentSize.registerRoleCardIconClientHeight,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            OficiosPromptActionRow(
                prompt = stringResource(R.string.auth_already_have_account),
                actionLabel = stringResource(R.string.auth_start_session_button),
                onAction = onNavigateToLogin,
            )
        }
    }
}

@Composable
fun RegisterFormScreen(
    mode: RegisterMode,
    uiState: RegisterFormUiState,
    onEvent: (RegisterFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenTitle = when (mode) {
        RegisterMode.CLIENT -> stringResource(R.string.auth_register_client_screen_title)
        RegisterMode.PROVIDER -> stringResource(R.string.auth_register_provider_screen_title)
    }

    AuthRegisterLayout(
        modifier = modifier,
        isLoading = uiState.isLoading,
    ) {
        Text(
            text = screenTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.auth_register_subtitle),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        val documentFocus = remember { FocusRequester() }
        val emailFocus = remember { FocusRequester() }
        val passwordFocus = remember { FocusRequester() }
        val confirmFocus = remember { FocusRequester() }
        FieldAndCtaBlock {
            if (uiState.generalError != null) {
                OficiosInlineAlert(message = uiState.generalError)
            }

            OficiosTextField(
                value = uiState.fullName,
                onValueChange = { onEvent(RegisterFormEvent.FullNameChanged(it)) },
                label = stringResource(R.string.auth_full_name_label),
                isError = uiState.hasAttemptedSubmit && uiState.fullNameError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.fullNameError else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { documentFocus.requestFocus() }),
            )
            OficiosTextField(
                value = uiState.document,
                onValueChange = { onEvent(RegisterFormEvent.DocumentChanged(it)) },
                label = stringResource(R.string.auth_document_label),
                isError = uiState.hasAttemptedSubmit && uiState.documentError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.documentError else null,
                modifier = Modifier.focusRequester(documentFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
            )
            OficiosTextField(
                value = uiState.email,
                onValueChange = { onEvent(RegisterFormEvent.EmailChanged(it)) },
                label = stringResource(R.string.auth_email_electronic_label),
                isError = uiState.hasAttemptedSubmit && uiState.emailError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.emailError else null,
                modifier = Modifier.focusRequester(emailFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            )
            Text(
                text = stringResource(R.string.auth_register_email_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorMutedText,
            )
            OficiosPasswordField(
                value = uiState.password,
                onValueChange = { onEvent(RegisterFormEvent.PasswordChanged(it)) },
                label = stringResource(R.string.auth_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.passwordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.passwordError else null,
                modifier = Modifier.focusRequester(passwordFocus),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() }),
            )
            OficiosPasswordField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(RegisterFormEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.auth_repeat_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.confirmPasswordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.confirmPasswordError else null,
                modifier = Modifier.focusRequester(confirmFocus),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEvent(RegisterFormEvent.Submit) }),
            )

            OficiosTermsCheckbox(
                checked = uiState.acceptedTerms,
                onCheckedChange = { onEvent(RegisterFormEvent.TermsChanged(it)) },
                onTermsClick = { onEvent(RegisterFormEvent.TermsClicked) },
                enabled = !uiState.isLoading,
                prefix = stringResource(R.string.auth_terms_prefix),
                termsLabel = stringResource(R.string.auth_terms_link),
            )

            if (uiState.hasAttemptedSubmit && uiState.termsError != null) {
                Text(
                    text = uiState.termsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            OficiosButton(
                text = stringResource(R.string.auth_next_button),
                onClick = { onEvent(RegisterFormEvent.Submit) },
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        OficiosPromptActionRow(
            prompt = stringResource(R.string.auth_already_have_account),
            actionLabel = stringResource(R.string.auth_login_button),
            onAction = { onEvent(RegisterFormEvent.LoginClicked) },
            enabled = !uiState.isLoading,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun RegisterFormPreview() {
    OficiosTheme {
        RegisterFormScreen(
            mode = RegisterMode.CLIENT,
            uiState = RegisterFormUiState(canSubmit = true, acceptedTerms = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun RegisterRolePreview() {
    OficiosTheme { RegisterRoleSelectionScreen({}, {}, {}) }
}
