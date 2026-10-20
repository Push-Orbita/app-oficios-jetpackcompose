package com.cocido.oficios.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateToPersonalData: () -> Unit,
    onNavigateToClientData: () -> Unit,
    onNavigateToProfessionalData: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLoggedOut()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToPersonalData -> onNavigateToPersonalData()
                SettingsEffect.NavigateToClientData -> onNavigateToClientData()
                SettingsEffect.NavigateToProfessionalData -> onNavigateToProfessionalData()
                SettingsEffect.NavigateToServices -> onNavigateToServices()
                SettingsEffect.NavigateToPortfolio -> onNavigateToPortfolio()
                SettingsEffect.NavigateToChangePassword -> onNavigateToChangePassword()
                SettingsEffect.NavigateToDeleteAccount -> onNavigateToDeleteAccount()
                SettingsEffect.NavigateToTerms -> onNavigateToTerms()
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                SettingsEvent.NavigateBack -> onNavigateBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
        showBackNavigation = showBackNavigation,
        bottomBar = bottomBar,
    )
}
