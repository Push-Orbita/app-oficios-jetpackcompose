package com.cocido.oficios.feature.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
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
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun PersonalDataScreen(
    uiState: PersonalDataUiState,
    onEvent: (PersonalDataEvent) -> Unit,
    onRequestPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = "Datos personales", onBack = { onEvent(PersonalDataEvent.Back) }) },
    ) { padding ->
        when {
            uiState.isLoading -> OficiosLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            uiState.errorMessage != null && uiState.userId == 0L -> OficiosErrorState(
                title = "No pudimos cargar tus datos",
                description = uiState.errorMessage,
                onRetry = { onEvent(PersonalDataEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> {
            val lastNameFocus = remember { FocusRequester() }
            val emailFocus = remember { FocusRequester() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(OficiosSpacing.lg)
                    .widthIn(max = OficiosComponentSize.contentMaxWidth),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Mantené tus datos actualizados para que tu cuenta sea fácil de reconocer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                )
                OficiosAvatar(
                    name = "${uiState.firstName} ${uiState.lastName}",
                    imageUrl = when {
                        uiState.removePhoto -> null
                        uiState.selectedPhotoPath != null -> uiState.selectedPhotoPath
                        else -> uiState.currentPhotoUrl
                    },
                    size = OficiosComponentSize.avatarLg,
                    showBorder = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                ) {
                    OficiosButton(
                        text = "Cambiar foto",
                        onClick = onRequestPhoto,
                        variant = OficiosButtonVariant.Outlined,
                        modifier = Modifier.weight(1f),
                    )
                    OficiosButton(
                        text = "Quitar",
                        onClick = { onEvent(PersonalDataEvent.RemovePhoto) },
                        variant = OficiosButtonVariant.Ghost,
                        modifier = Modifier.weight(1f),
                    )
                }
                OficiosTextField(
                    value = uiState.firstName,
                    onValueChange = { onEvent(PersonalDataEvent.FirstNameChanged(it)) },
                    label = "Nombre",
                    isError = uiState.firstNameError != null,
                    supportingText = uiState.firstNameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { lastNameFocus.requestFocus() }),
                )
                OficiosTextField(
                    value = uiState.lastName,
                    onValueChange = { onEvent(PersonalDataEvent.LastNameChanged(it)) },
                    label = "Apellido",
                    isError = uiState.lastNameError != null,
                    supportingText = uiState.lastNameError,
                    modifier = Modifier.focusRequester(lastNameFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                )
                OficiosTextField(
                    value = uiState.email,
                    onValueChange = { onEvent(PersonalDataEvent.EmailChanged(it)) },
                    label = "Correo electrónico",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onEvent(PersonalDataEvent.Save) }),
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError ?: "Si lo cambiás, es posible que debas volver a verificarlo.",
                    modifier = Modifier.focusRequester(emailFocus),
                )
                uiState.errorMessage?.let { OficiosInlineAlert(message = it) }
                OficiosButton(
                    text = "Guardar cambios",
                    onClick = { onEvent(PersonalDataEvent.Save) },
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Prominent,
                    loading = uiState.isSaving,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            }
        }
    }
}
