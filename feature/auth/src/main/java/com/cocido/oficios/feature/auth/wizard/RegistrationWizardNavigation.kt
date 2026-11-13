package com.cocido.oficios.feature.auth.wizard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import androidx.navigation.navigation
import com.cocido.oficios.feature.auth.navigation.AuthDestination

fun NavGraphBuilder.registrationWizardGraph(
    navController: NavHostController,
) {
    navigation(
        route = AuthDestination.WIZARD_GRAPH,
        startDestination = AuthDestination.WIZARD_PERSONAL,
    ) {
        composable(AuthDestination.WIZARD_PERSONAL) { entry ->
            val viewModel = rememberWizardViewModel(navController, entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RegistrationWizardPersonalScreen(
                state = state,
                onEvent = viewModel::onPersonalEvent,
                onLeaveWizard = { navController.popBackStack() },
            )
        }
        composable(AuthDestination.WIZARD_PROFILE) { entry ->
            val viewModel = rememberWizardViewModel(navController, entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RegistrationWizardProfileScreen(
                state = state,
                onContactNameChanged = viewModel::onContactNameChanged,
                onPhoneChanged = viewModel::onPhoneChanged,
                onBarrioQueryChanged = viewModel::onBarrioQueryChanged,
                onBarrioFocused = viewModel::onBarrioFocused,
                onBarrioSelected = viewModel::onBarrioSelected,
                onBarrioDismissed = viewModel::dismissBarrioSuggestions,
                onBarrioRetry = viewModel::retryBarrioSearch,
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
                canContinue = { it.canContinueOficio },
                onContinue = { wizard, trade -> wizard.continueFromOficio(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_ZONA) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.ZONA,
                canContinue = { it.canContinueZona },
                onContinue = { wizard, trade -> wizard.continueFromZona(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_AGENDA) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.AGENDA,
                canContinue = { it.canContinueAgenda },
                onContinue = { wizard, trade -> wizard.continueFromAgenda(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_COBRO) { entry ->
            ProviderTradeStep(
                navController = navController,
                entry = entry,
                section = com.cocido.oficios.feature.auth.register.RegisterTradeFormSection.COBRO,
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
                canContinue = { it.canContinueIdentidad },
                onContinue = { wizard, trade -> wizard.continueFromIdentidad(trade.snapshotDraft()) },
            )
        }
        composable(AuthDestination.WIZARD_REVIEW) { entry ->
            val viewModel = rememberWizardViewModel(navController, entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RegistrationWizardReviewScreen(
                state = state,
                onCreateAccount = viewModel::createAccount,
                onBack = viewModel::onReviewBack,
                onEditPersonal = viewModel::editPersonal,
                onEditContact = viewModel::editProfile,
                onEditAddress = viewModel::editProfile,
                onEditOficio = viewModel::editOficio,
                onEditZona = viewModel::editZona,
                onEditAgenda = viewModel::editAgenda,
                onEditCobro = viewModel::editCobro,
            )
        }
    }
}

@Composable
private fun ProviderTradeStep(
    navController: NavHostController,
    entry: androidx.navigation.NavBackStackEntry,
    section: com.cocido.oficios.feature.auth.register.RegisterTradeFormSection,
    canContinue: (com.cocido.oficios.feature.auth.register.RegisterTradeUiState) -> Boolean,
    onContinue: (RegistrationWizardViewModel, com.cocido.oficios.feature.auth.register.RegisterTradeViewModel) -> Unit,
) {
    val wizard = rememberWizardViewModel(navController, entry)
    val trade = rememberTradeViewModel(navController, entry)
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
        // En el wizard el back debe ir al paso anterior. El dialogo de
        // descartar solo corresponde al salir del grafo entero (personal).
        confirmDiscardOnSystemBack = false,
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

/** One owner above NavHost destinations: transitions never create competing collectors. */
@Composable
fun RegistrationWizardNavigationEffects(
    navController: NavHostController,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
) {
    val current by navController.currentBackStackEntryAsState()
    val entry = current ?: return
    if (entry.destination.hierarchy.none { it.route == AuthDestination.WIZARD_GRAPH }) return
    val parent = remember(entry) { navController.getBackStackEntry(AuthDestination.WIZARD_GRAPH) }
    val viewModel: RegistrationWizardViewModel = hiltViewModel(parent)
    RegistrationWizardEffectHandler(
        effects = viewModel.effects,
        lifecycleOwner = parent,
        navController = navController,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToTerms = onNavigateToTerms,
    )
}

@Composable
internal fun RegistrationWizardEffectHandler(
    effects: Flow<RegistrationWizardEffect>,
    lifecycleOwner: LifecycleOwner,
    navController: NavHostController,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
) {
    val onboarding by rememberUpdatedState(onNavigateToOnboarding)
    val login by rememberUpdatedState(onNavigateToLogin)
    val terms by rememberUpdatedState(onNavigateToTerms)
    LaunchedEffect(effects, lifecycleOwner, navController) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collect { effect ->
                when (effect) {
                    RegistrationWizardEffect.GoToProfile -> navController.navigateWizardOnce(AuthDestination.WIZARD_PROFILE)
                    RegistrationWizardEffect.GoToOficio -> navController.navigateWizardOnce(AuthDestination.WIZARD_OFICIO)
                    RegistrationWizardEffect.GoToZona -> navController.navigateWizardOnce(AuthDestination.WIZARD_ZONA)
                    RegistrationWizardEffect.GoToAgenda -> navController.navigateWizardOnce(AuthDestination.WIZARD_AGENDA)
                    RegistrationWizardEffect.GoToCobro -> navController.navigateWizardOnce(AuthDestination.WIZARD_COBRO)
                    RegistrationWizardEffect.GoToIdentidad -> navController.navigateWizardOnce(AuthDestination.WIZARD_IDENTIDAD)
                    RegistrationWizardEffect.GoToReview -> navController.navigateWizardOnce(AuthDestination.WIZARD_REVIEW)
                    RegistrationWizardEffect.GoToPersonal -> {
                        navController.popWizardTo(AuthDestination.WIZARD_PERSONAL)
                    }
                    RegistrationWizardEffect.GoBack -> {
                        navController.popBackStack()
                    }
                    RegistrationWizardEffect.PopToProfile -> {
                        navController.popWizardTo(AuthDestination.WIZARD_PROFILE)
                    }
                    RegistrationWizardEffect.PopToOficio -> {
                        navController.popWizardTo(AuthDestination.WIZARD_OFICIO)
                    }
                    RegistrationWizardEffect.PopToZona -> {
                        navController.popWizardTo(AuthDestination.WIZARD_ZONA)
                    }
                    RegistrationWizardEffect.PopToAgenda -> {
                        navController.popWizardTo(AuthDestination.WIZARD_AGENDA)
                    }
                    RegistrationWizardEffect.PopToCobro -> {
                        navController.popWizardTo(AuthDestination.WIZARD_COBRO)
                    }
                    RegistrationWizardEffect.AccountCreated -> onboarding()
                    RegistrationWizardEffect.NavigateToLogin -> login()
                    RegistrationWizardEffect.NavigateToTerms -> terms()
                }
            }
        }
    }
}

internal fun NavHostController.navigateWizardOnce(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) { launchSingleTop = true }
}

internal fun NavHostController.popWizardTo(route: String) {
    if (currentDestination?.route == route) return
    if (!popBackStack(route, inclusive = false)) {
        // If that step is not on the back stack, do not push a duplicate on top of Review.
        return
    }
}