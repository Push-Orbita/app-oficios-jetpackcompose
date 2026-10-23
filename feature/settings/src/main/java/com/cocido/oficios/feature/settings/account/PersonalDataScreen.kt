package com.cocido.oficios.feature.settings.account

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormActionsBar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormColumn
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler

@Composable
fun PersonalDataScreen(
    uiState: PersonalDataUiState,
    onEvent: (PersonalDataEvent) -> Unit,
    onRequestPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = uiState.hasUnsavedChanges,
        onConfirmExit = { onEvent(PersonalDataEvent.Back) },
    )
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = stringResource(R.string.personal_data_title), onBack = handleBack) },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.personal_data_load_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(PersonalDataEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> PersonalDataForm(state = state, onEvent = onEvent, onRequestPhoto = onRequestPhoto)
            }
        }
    }
}

private fun PersonalDataUiState.phase(): OficiosContentPhase = when {
    isLoading -> OficiosContentPhase.Loading
    errorMessage != null && userId == 0L -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun PersonalDataForm(
    state: PersonalDataUiState,
    onEvent: (PersonalDataEvent) -> Unit,
    onRequestPhoto: () -> Unit,
) {
    val lastNameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    OficiosFormColumn(
        footer = {
            OficiosFormActionsBar(
                primaryLabel = stringResource(R.string.personal_data_save),
                onPrimary = { onEvent(PersonalDataEvent.Save) },
                primaryEnabled = state.canSave && !state.isSaving,
                primaryLoading = state.isSaving,
            )
        },
    ) {
        Text(
            text = stringResource(R.string.personal_data_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosAvatar(
            name = "${state.firstName} ${state.lastName}",
            imageUrl = when {
                state.removePhoto -> null
                state.selectedPhotoPath != null -> state.selectedPhotoPath
                else -> state.currentPhotoUrl
            },
            size = OficiosComponentSize.avatarLg,
            showBorder = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            OficiosButton(
                text = stringResource(R.string.personal_data_change_photo),
                onClick = onRequestPhoto,
                variant = OficiosButtonVariant.Outlined,
                modifier = Modifier.weight(1f),
            )
            OficiosButton(
                text = stringResource(R.string.personal_data_remove_photo),
                onClick = { onEvent(PersonalDataEvent.RemovePhoto) },
                variant = OficiosButtonVariant.Ghost,
                modifier = Modifier.weight(1f),
            )
        }
        OficiosTextField(
            value = state.firstName,
            onValueChange = { onEvent(PersonalDataEvent.FirstNameChanged(it)) },
            label = stringResource(R.string.personal_data_first_name),
            isError = state.firstNameError != null,
            supportingText = state.firstNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { lastNameFocus.requestFocus() }),
        )
        OficiosTextField(
            value = state.lastName,
            onValueChange = { onEvent(PersonalDataEvent.LastNameChanged(it)) },
            label = stringResource(R.string.personal_data_last_name),
            isError = state.lastNameError != null,
            supportingText = state.lastNameError,
            modifier = Modifier.focusRequester(lastNameFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
        )
        OficiosTextField(
            value = state.email,
            onValueChange = { onEvent(PersonalDataEvent.EmailChanged(it)) },
            label = stringResource(R.string.personal_data_email),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onEvent(PersonalDataEvent.Save) }),
            isError = state.emailError != null,
            supportingText = state.emailError ?: stringResource(R.string.personal_data_email_hint),
            modifier = Modifier.focusRequester(emailFocus),
        )
        state.errorMessage?.let { OficiosInlineAlert(message = it) }
    }
}
