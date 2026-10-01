package com.cocido.oficios.feature.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.settings.R

@Composable
fun DeleteAccountRoute(
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DeleteAccountEffect.NavigateBack -> onNavigateBack()
                DeleteAccountEffect.LoggedOut -> onLoggedOut()
                is DeleteAccountEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }
    DeleteAccountScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountEvent) -> Unit,
) {
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.settings_delete_account),
                onBack = { onEvent(DeleteAccountEvent.Back) },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> OficiosLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            uiState.errorMessage != null &&
                !uiState.hasPendingGrace &&
                uiState.password.isEmpty() &&
                uiState.confirmation.isEmpty() &&
                !uiState.isSubmitting -> OficiosErrorState(
                title = stringResource(R.string.settings_delete_account_load_error),
                description = uiState.errorMessage,
                onRetry = { onEvent(DeleteAccountEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            uiState.hasPendingGrace -> DeleteAccountGraceContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
            else -> DeleteAccountForm(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DeleteAccountGraceContent(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(OficiosSpacing.lg)
            .widthIn(max = OficiosComponentSize.contentMaxWidth)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.settings_delete_account_grace_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = if (uiState.scheduledForLabel != null) {
                stringResource(R.string.settings_delete_account_grace_body, uiState.scheduledForLabel)
            } else {
                stringResource(R.string.settings_delete_account_grace_body_generic)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        uiState.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.settings_delete_account_cancel),
            onClick = { onEvent(DeleteAccountEvent.CancelPending) },
            loading = uiState.isCancelling,
            enabled = !uiState.isCancelling,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Prominent,
        )
    }
}

@Composable
private fun DeleteAccountForm(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(OficiosSpacing.lg)
            .widthIn(max = OficiosComponentSize.contentMaxWidth)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.settings_delete_account_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosPasswordField(
            value = uiState.password,
            onValueChange = { onEvent(DeleteAccountEvent.PasswordChanged(it)) },
            label = stringResource(R.string.settings_delete_account_password),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError,
        )
        OficiosTextField(
            value = uiState.confirmation,
            onValueChange = { onEvent(DeleteAccountEvent.ConfirmationChanged(it)) },
            label = stringResource(R.string.settings_delete_account_confirmation),
            placeholder = DeleteAccountViewModel.CONFIRMATION_WORD,
            isError = uiState.confirmationError != null,
            supportingText = uiState.confirmationError,
        )
        uiState.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.settings_delete_account_submit),
            onClick = { onEvent(DeleteAccountEvent.Submit) },
            loading = uiState.isSubmitting,
            enabled = !uiState.isSubmitting,
            variant = OficiosButtonVariant.Danger,
            size = OficiosButtonSize.Prominent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
