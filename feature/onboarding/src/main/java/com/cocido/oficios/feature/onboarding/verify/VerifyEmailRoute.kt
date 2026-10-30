package com.cocido.oficios.feature.onboarding.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController

@Composable
fun VerifyEmailRoute(
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: VerifyEmailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is VerifyEmailEffect.Verified) {
                toastController.showSuccess("Código validado exitosamente")
                onVerified()
            }
        }
    }
    VerifyEmailStepScreen(
        uiState = uiState,
        onCodeChanged = viewModel::onCodeChanged,
        onResend = viewModel::resendCode,
        onSubmit = viewModel::submit,
        onEditEmailClick = viewModel::onEditEmailClick,
        onDraftEmailChanged = viewModel::onDraftEmailChanged,
        onSaveEmail = viewModel::saveEmail,
        onCancelEditEmail = viewModel::onCancelEditEmail,
        modifier = modifier,
    )
}
