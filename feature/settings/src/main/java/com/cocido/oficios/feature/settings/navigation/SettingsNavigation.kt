package com.cocido.oficios.feature.settings.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.settings.SettingsRoute
import com.cocido.oficios.feature.settings.account.ChangePasswordRoute
import com.cocido.oficios.feature.settings.account.DeleteAccountRoute
import com.cocido.oficios.feature.settings.account.PersonalDataRoute
import com.cocido.oficios.feature.settings.client.ClientDataRoute
import com.cocido.oficios.feature.settings.provider.ProfessionalProfileRoute
import com.cocido.oficios.feature.settings.provider.AddServiceRoute
import com.cocido.oficios.feature.settings.provider.PortfolioRoute
import com.cocido.oficios.feature.settings.provider.ServicesRoute
import androidx.navigation.NavHostController

fun NavGraphBuilder.settingsScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToVerifyPhone: (Long, String) -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
) {
    composable(route = SettingsDestination.route) {
        SettingsRoute(
            onNavigateBack = onNavigateBack,
            onLoggedOut = onLoggedOut,
            onNavigateToPersonalData = { navController.navigate(SettingsDestination.personalData) },
            onNavigateToClientData = { navController.navigate(SettingsDestination.clientData) },
            onNavigateToProfessionalData = { navController.navigate(SettingsDestination.professionalData) },
            onNavigateToServices = { navController.navigate(SettingsDestination.services) },
            onNavigateToPortfolio = { navController.navigate(SettingsDestination.portfolio) },
            onNavigateToChangePassword = { navController.navigate(SettingsDestination.changePassword) },
            onNavigateToDeleteAccount = { navController.navigate(SettingsDestination.deleteAccount) },
            onNavigateToTerms = onNavigateToTerms,
            onNavigateToHelp = onNavigateToHelp,
            showBackNavigation = false,
            bottomBar = bottomBar,
        )
    }
    composable(SettingsDestination.personalData) {
        PersonalDataRoute(onNavigateBack = { navController.popBackStack() }, onShowMessage = onShowMessage)
    }
    composable(SettingsDestination.clientData) {
        ClientDataRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVerifyPhone = onNavigateToVerifyPhone,
            onShowMessage = onShowMessage,
        )
    }
    composable(SettingsDestination.professionalData) {
        ProfessionalProfileRoute(
            onNavigateBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
            onNavigateToServices = { navController.navigate(SettingsDestination.services) },
        )
    }
    composable(SettingsDestination.services) {
        ServicesRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAdd = { navController.navigate(SettingsDestination.addService) },
            onShowMessage = onShowMessage,
        )
    }
    composable(SettingsDestination.addService) {
        AddServiceRoute(onNavigateBack = { navController.popBackStack() }, onShowMessage = onShowMessage)
    }
    composable(SettingsDestination.portfolio) {
        PortfolioRoute(onNavigateBack = { navController.popBackStack() }, onShowMessage = onShowMessage)
    }
    composable(SettingsDestination.changePassword) {
        ChangePasswordRoute(onNavigateBack = { navController.popBackStack() }, onShowMessage = onShowMessage)
    }
    composable(SettingsDestination.deleteAccount) {
        DeleteAccountRoute(
            onNavigateBack = { navController.popBackStack() },
            onLoggedOut = onLoggedOut,
            onShowMessage = onShowMessage,
        )
    }
}
