package com.cocido.oficios.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTermsCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
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
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.auth_already_have_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OficiosButton(
                    text = stringResource(R.string.auth_start_session_button),
                    onClick = onNavigateToLogin,
                    variant = OficiosButtonVariant.Secondary,
                )
            }
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
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.auth_register_subtitle),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            if (uiState.generalError != null) {
                OficiosInlineAlert(message = uiState.generalError)
            }

            OficiosTextField(
                value = uiState.fullName,
                onValueChange = { onEvent(RegisterFormEvent.FullNameChanged(it)) },
                label = "",
                placeholder = stringResource(R.string.auth_full_name_label),
                isError = uiState.hasAttemptedSubmit && uiState.fullNameError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.fullNameError else null,
            )
            OficiosTextField(
                value = uiState.document,
                onValueChange = { onEvent(RegisterFormEvent.DocumentChanged(it)) },
                label = "",
                placeholder = stringResource(R.string.auth_document_label),
                isError = uiState.hasAttemptedSubmit && uiState.documentError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.documentError else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OficiosTextField(
                value = uiState.email,
                onValueChange = { onEvent(RegisterFormEvent.EmailChanged(it)) },
                label = "",
                placeholder = stringResource(R.string.auth_email_electronic_label),
                isError = uiState.hasAttemptedSubmit && uiState.emailError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.emailError else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Text(
                text = stringResource(R.string.auth_register_email_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorMutedText,
            )
            OficiosPasswordField(
                value = uiState.password,
                onValueChange = { onEvent(RegisterFormEvent.PasswordChanged(it)) },
                label = "",
                placeholder = stringResource(R.string.auth_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.passwordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.passwordError else null,
            )
            OficiosPasswordField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(RegisterFormEvent.ConfirmPasswordChanged(it)) },
                label = "",
                placeholder = stringResource(R.string.auth_repeat_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.confirmPasswordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.confirmPasswordError else null,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                OficiosTermsCheckbox(
                    checked = uiState.acceptedTerms,
                    onCheckedChange = { onEvent(RegisterFormEvent.TermsChanged(it)) },
                    onTermsClick = { onEvent(RegisterFormEvent.TermsClicked) },
                    enabled = !uiState.isLoading,
                    prefix = stringResource(R.string.auth_terms_prefix),
                    termsLabel = stringResource(R.string.auth_terms_link),
                    modifier = Modifier.weight(1f),
                )
                OficiosButton(
                    text = stringResource(R.string.auth_next_button),
                    onClick = { onEvent(RegisterFormEvent.Submit) },
                    enabled = !uiState.isLoading,
                    loading = uiState.isLoading,
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Prominent,
                )
            }

            if (uiState.hasAttemptedSubmit && uiState.termsError != null) {
                Text(
                    text = uiState.termsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.auth_already_have_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OficiosButton(
                    text = stringResource(R.string.auth_login_button),
                    onClick = { onEvent(RegisterFormEvent.LoginClicked) },
                    enabled = !uiState.isLoading,
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Compact,
                )
            }
        }
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
