package com.cocido.oficios.feature.settings.account

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onEvent: (ChangePasswordEvent) -> Unit,
) {
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = uiState.hasUnsavedChanges,
        onConfirmExit = { onEvent(ChangePasswordEvent.Back) },
    )
    OficiosScaffold(
        topBar = { OficiosBackTopBar(title = stringResource(R.string.change_password_title), onBack = handleBack) },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.change_password_load_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(ChangePasswordEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> ChangePasswordForm(state = state, onEvent = onEvent)
            }
        }
    }
}

private fun ChangePasswordUiState.phase(): OficiosContentPhase = when {
    isLoading -> OficiosContentPhase.Loading
    email.isBlank() && errorMessage != null -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun ChangePasswordForm(
    state: ChangePasswordUiState,
    onEvent: (ChangePasswordEvent) -> Unit,
) {
    val confirmFocus = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md)
            .widthIn(max = OficiosComponentSize.contentMaxWidth),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.change_password_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosPasswordField(
            value = state.password,
            onValueChange = { onEvent(ChangePasswordEvent.PasswordChanged(it)) },
            label = stringResource(R.string.change_password_new),
            isError = state.passwordError != null,
            supportingText = state.passwordError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() }),
        )
        OficiosPasswordField(
            value = state.confirmation,
            onValueChange = { onEvent(ChangePasswordEvent.ConfirmationChanged(it)) },
            label = stringResource(R.string.change_password_repeat),
            isError = state.confirmationError != null,
            supportingText = state.confirmationError,
            modifier = Modifier.focusRequester(confirmFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEvent(ChangePasswordEvent.Submit) }),
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.change_password_submit),
            onClick = { onEvent(ChangePasswordEvent.Submit) },
            loading = state.isSubmitting,
            enabled = state.canSubmit && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}
