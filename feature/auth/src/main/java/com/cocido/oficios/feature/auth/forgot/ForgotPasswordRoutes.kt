package com.cocido.oficios.feature.auth.forgot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ForgotPasswordRoute(
    onNavigateToReset: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ForgotPasswordEffect.NavigateToReset -> onNavigateToReset()
                ForgotPasswordEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ForgotPasswordScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

@Composable
fun ResetPasswordRoute(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ForgotPasswordEffect.NavigateToLogin -> onNavigateToLogin()
                ForgotPasswordEffect.NavigateToReset -> Unit
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToLogin()
        }
    }

    ResetPasswordScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}
