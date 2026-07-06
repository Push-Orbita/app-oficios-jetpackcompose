package com.cocido.oficios.feature.onboarding.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingLogoutEffect(
    onLoggedOut: () -> Unit,
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    LaunchedEffect(logoutViewModel) {
        logoutViewModel.loggedOutEvents.collect {
            onLoggedOut()
        }
    }
}
