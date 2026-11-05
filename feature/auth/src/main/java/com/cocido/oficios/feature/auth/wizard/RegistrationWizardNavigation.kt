package com.cocido.oficios.feature.auth.wizard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.cocido.oficios.feature.auth.navigation.AuthDestination

fun NavGraphBuilder.registrationWizardGraph(
    navController: NavHostController,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
) {
    navigation(
        route = AuthDestination.WIZARD_GRAPH,
        startDestination = AuthDestination.WIZARD_PERSONAL,
    ) {
        composable(AuthDestination.WIZARD_PERSONAL) { entry ->
            val viewModel = rememberWizardViewModel(navController, entry)
            RegistrationWizardEffectHandler(
                viewModel = viewModel,
                navController = navController,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RegistrationWizardPersonalScreen(
                state = state,
                onEvent = viewModel::onPersonalEvent,
            )
        }
        composable(AuthDestination.WIZARD_PROFILE) { entry ->
            val viewModel = rememberWizardViewModel(navController, entry)
            RegistrationWizardEffectHandler(
                viewModel = viewModel,
                navController = navController,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RegistrationWizardProfileScreen(
                state = state,
                onContactNameChanged = viewModel::onContactNameChanged,
                onPhoneChanged = viewModel::onPhoneChanged,
                onBarrioQueryChanged = viewModel::onBarrioQueryChanged,
                onBarrioFocused = viewModel::onBarrioFocused,
                onBarrioSelected = viewModel::onBarrioSelected,
                onBarrioDismissed = viewModel::dismissBarrioSuggestions,
                onCalleChanged = viewModel::onCalleChanged,
                onNumeroChanged = viewModel::onNumeroChanged,
                onPisoChanged = viewModel::onPisoChanged,
                onDepartamentoChanged = viewModel::onDepartamentoChanged,
                onContinue = viewModel::continueFromProfile,
            )
        }
        composable(AuthDestination.WIZARD_REVIEW) { entry ->
            val viewModel = rememberWizardViewModel(navController, entry)
            RegistrationWizardEffectHandler(
                viewModel = viewModel,
                navController = navController,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RegistrationWizardReviewScreen(
                state = state,
                onCreateAccount = viewModel::createAccount,
            )
        }
    }
}

@Composable
private fun rememberWizardViewModel(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry,
): RegistrationWizardViewModel {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(AuthDestination.WIZARD_GRAPH)
    }
    return hiltViewModel(parentEntry)
}

@Composable
private fun RegistrationWizardEffectHandler(
    viewModel: RegistrationWizardViewModel,
    navController: NavHostController,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RegistrationWizardEffect.GoToProfile ->
                    navController.navigate(AuthDestination.WIZARD_PROFILE)
                RegistrationWizardEffect.GoToReview ->
                    navController.navigate(AuthDestination.WIZARD_REVIEW)
                RegistrationWizardEffect.GoToPersonal ->
                    navController.popBackStack(AuthDestination.WIZARD_PERSONAL, inclusive = false)
                RegistrationWizardEffect.AccountCreated -> onNavigateToOnboarding()
                RegistrationWizardEffect.NavigateToLogin -> onNavigateToLogin()
                RegistrationWizardEffect.NavigateToTerms -> onNavigateToTerms()
            }
        }
    }
}
