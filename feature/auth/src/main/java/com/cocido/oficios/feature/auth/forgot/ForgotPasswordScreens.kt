package com.cocido.oficios.feature.auth.forgot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSuccessState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthFormLayout

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isSuccess) {
        OficiosSuccessState(
            title = stringResource(R.string.auth_forgot_title),
            description = stringResource(R.string.auth_forgot_success),
            modifier = modifier,
        )
        return
    }

    AuthFormLayout(
        title = stringResource(R.string.auth_forgot_title),
        subtitle = stringResource(R.string.auth_forgot_subtitle),
        modifier = modifier,
        isLoading = uiState.isLoading,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            OficiosTextField(
                value = uiState.email,
                onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                label = stringResource(R.string.auth_email_label),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OficiosButton(
                text = stringResource(R.string.auth_forgot_button),
                onClick = { onEvent(ForgotPasswordEvent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Prominent,
            )
            OficiosButton(
                text = stringResource(R.string.auth_back_to_login),
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )
        }
    }
}

@Composable
fun ResetPasswordScreen(
    uiState: ResetPasswordUiState,
    onEvent: (ResetPasswordEvent) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isSuccess) {
        OficiosSuccessState(
            title = stringResource(R.string.auth_reset_title),
            description = stringResource(R.string.auth_reset_success),
            modifier = modifier,
        )
        return
    }

    AuthFormLayout(
        title = stringResource(R.string.auth_reset_title),
        modifier = modifier,
        isLoading = uiState.isLoading,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            OficiosTextField(
                value = uiState.code,
                onValueChange = { onEvent(ResetPasswordEvent.CodeChanged(it)) },
                label = stringResource(R.string.auth_reset_code_label),
                isError = uiState.hasAttemptedSubmit && uiState.codeError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.codeError else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OficiosPasswordField(
                value = uiState.password,
                onValueChange = { onEvent(ResetPasswordEvent.PasswordChanged(it)) },
                label = stringResource(R.string.auth_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.passwordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.passwordError else null,
            )
            OficiosPasswordField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(ResetPasswordEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.auth_confirm_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.confirmPasswordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.confirmPasswordError else null,
            )
            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OficiosButton(
                text = stringResource(R.string.auth_reset_button),
                onClick = { onEvent(ResetPasswordEvent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Prominent,
            )
            OficiosButton(
                text = stringResource(R.string.auth_back_to_login),
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )
        }
    }
}
