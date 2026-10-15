package com.cocido.oficios.feature.auth.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.inputs.FieldAndCtaBlock
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.layout.OficiosPromptActionRow
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthScreenLayout
import com.cocido.oficios.feature.auth.components.AuthSupportCard

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScreenLayout(
        modifier = modifier,
        isLoading = uiState.isLoading,
        support = {
            AuthSupportCard(
                onContactClick = { onEvent(LoginEvent.ContactSupportClicked) },
                enabled = !uiState.isLoading,
            )
        },
    ) {
        val passwordFocus = remember { FocusRequester() }
        if (uiState.bannerError != null) {
            OficiosInlineAlert(message = uiState.bannerError)
        } else if (uiState.hasAttemptedSubmit && !uiState.canSubmit) {
            OficiosInlineAlert(
                message = stringResource(R.string.auth_login_complete_data_error),
            )
        } else {
            Text(
                text = stringResource(R.string.auth_login_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldAndCtaBlock {
            OficiosTextField(
                value = uiState.email,
                onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                label = stringResource(R.string.auth_email_label),
                isError = uiState.hasAttemptedSubmit && uiState.emailError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.emailError else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            )
            OficiosPasswordField(
                value = uiState.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                label = stringResource(R.string.auth_password_label),
                isError = uiState.hasAttemptedSubmit && uiState.passwordError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.passwordError else null,
                modifier = Modifier.focusRequester(passwordFocus),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEvent(LoginEvent.Submit) }),
            )

            OficiosButton(
                text = stringResource(R.string.auth_forgot_password),
                onClick = { onEvent(LoginEvent.ForgotPasswordClicked) },
                enabled = !uiState.isLoading,
                variant = OficiosButtonVariant.Ghost,
                modifier = Modifier.align(Alignment.End),
            )

            OficiosCheckbox(
                checked = uiState.rememberMe,
                onCheckedChange = { onEvent(LoginEvent.RememberMeChanged(it)) },
                label = stringResource(R.string.auth_remember_me),
                enabled = !uiState.isLoading,
            )
            OficiosButton(
                text = stringResource(R.string.auth_login_button),
                onClick = { onEvent(LoginEvent.Submit) },
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OficiosPromptActionRow(
            prompt = stringResource(R.string.auth_no_account_yet),
            actionLabel = stringResource(R.string.auth_register_button),
            onAction = { onEvent(LoginEvent.RegisterClicked) },
            enabled = !uiState.isLoading,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun LoginScreenPreview() {
    OficiosTheme {
        LoginScreen(uiState = LoginUiState(canSubmit = true), onEvent = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B, name = "Login error")
@Composable
private fun LoginScreenErrorPreview() {
    OficiosTheme {
        LoginScreen(
            uiState = LoginUiState(
                email = "user@mail.com",
                password = "12345678",
                bannerError = "Los datos de sesión no coinciden",
                canSubmit = true,
                hasAttemptedSubmit = true,
            ),
            onEvent = {},
        )
    }
}
