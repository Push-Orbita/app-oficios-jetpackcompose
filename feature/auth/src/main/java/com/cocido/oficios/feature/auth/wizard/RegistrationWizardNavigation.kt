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
        composable(AuthDestination.WIZARD_OFICIO) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.OFICIO,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
                canContinue = { it.canContinueOficio },
                onContinue = { wizard, trade -> wizard.continueFromOficio(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_ZONA) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.ZONA,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
                canContinue = { it.canContinueZona },
                onContinue = { wizard, trade -> wizard.continueFromZona(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_AGENDA) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.AGENDA,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
                canContinue = { it.canContinueAgenda },
                onContinue = { wizard, trade -> wizard.continueFromAgenda(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_COBRO) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.COBRO,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
                canContinue = { it.canContinueCobro },
                onContinue = { wizard, trade ->
                    wizard.continueFromCobro(trade.snapshotDraft(), trade.uiState.value.servesAtLocal)
                },
            )
        }
        composable(AuthDestination.WIZARD_IDENTIDAD) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.IDENTIDAD,
                onNavigateToOnboarding = onNavigateToOnboarding,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToTerms = onNavigateToTerms,
                canContinue = { it.canContinueIdentidad },
                onContinue = { wizard, trade -> wizard.continueFromIdentidad(trade.snapshotDraft()) },
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
private fun ProviderTradeStep(
    navController: NavHostController,
    entry: androidx.navigation.NavBackStackEntry,
    section: com.cocido.oficios.feature.auth.register.RegisterTradeFormSection,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
    canContinue: (com.cocido.oficios.feature.auth.register.RegisterTradeUiState) -> Boolean,
    onContinue: (RegistrationWizardViewModel, com.cocido.oficios.feature.auth.register.RegisterTradeViewModel) -> Unit,
) {
    val wizard = rememberWizardViewModel(navController, entry)
    val trade = rememberTradeViewModel(navController, entry)
    RegistrationWizardEffectHandler(
        viewModel = wizard,
        navController = navController,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToTerms = onNavigateToTerms,
    )
    val tradeState by trade.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(trade) {
        if (trade.uiState.value.selectedCategoryId == null) {
            wizard.providerDraft()?.let { trade.applyDraft(it) }
        }
    }
    com.cocido.oficios.feature.auth.register.RegisterTradeScreen(
        uiState = tradeState,
        onEvent = trade::onEvent,
        section = section,
        continueLabel = androidx.compose.ui.res.stringResource(com.cocido.oficios.feature.auth.R.string.auth_next_button),
        onContinue = {
            if (canContinue(trade.uiState.value)) onContinue(wizard, trade)
        },
    )
}

@Composable
private fun rememberTradeViewModel(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry,
): com.cocido.oficios.feature.auth.register.RegisterTradeViewModel {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(AuthDestination.WIZARD_GRAPH)
    }
    return hiltViewModel(parentEntry)
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
                RegistrationWizardEffect.GoToOficio ->
                    navController.navigate(AuthDestination.WIZARD_OFICIO)
                RegistrationWizardEffect.GoToZona ->
                    navController.navigate(AuthDestination.WIZARD_ZONA)
                RegistrationWizardEffect.GoToAgenda ->
                    navController.navigate(AuthDestination.WIZARD_AGENDA)
                RegistrationWizardEffect.GoToCobro ->
                    navController.navigate(AuthDestination.WIZARD_COBRO)
                RegistrationWizardEffect.GoToIdentidad ->
                    navController.navigate(AuthDestination.WIZARD_IDENTIDAD)
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
