package com.cocido.oficios.feature.onboarding.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VerifyPhoneRoute(
    onVerified: () -> Unit,
    onEditNumber: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: VerifyPhoneViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is VerifyPhoneEffect.Verified) onVerified()
        }
    }
    VerifyPhoneStepScreen(
        uiState = uiState,
        onCodeChanged = viewModel::onCodeChanged,
        onResend = viewModel::resendCode,
        onSubmit = viewModel::submit,
        onEditNumber = onEditNumber,
        modifier = modifier,
    )
}
