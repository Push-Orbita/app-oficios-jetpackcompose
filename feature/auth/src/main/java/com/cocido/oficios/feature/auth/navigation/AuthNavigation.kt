package com.cocido.oficios.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.feature.auth.forgot.ForgotPasswordRoute
import com.cocido.oficios.feature.auth.forgot.ResetPasswordRoute
import com.cocido.oficios.feature.auth.login.LoginRoute
import com.cocido.oficios.feature.auth.register.RegisterClientRoute
import com.cocido.oficios.feature.auth.register.RegisterProviderRoute
import com.cocido.oficios.feature.auth.register.RegisterRoleSelectionRoute
import com.cocido.oficios.feature.auth.register.RegisterTradeRoute
import com.cocido.oficios.feature.auth.splash.SplashRoute

fun NavGraphBuilder.authGraph(
    onNavigateFromSplash: (InitialDestination) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegisterRole: () -> Unit,
    onNavigateToRegisterClient: () -> Unit,
    onNavigateToRegisterTrade: () -> Unit,
    onNavigateToRegisterProvider: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onContactSupport: () -> Unit,
    onNavigateToTerms: () -> Unit,
) {
    composable(AuthDestination.SPLASH) {
        SplashRoute(onNavigate = onNavigateFromSplash)
    }
    composable(AuthDestination.LOGIN) {
        LoginRoute(
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToRegister = onNavigateToRegisterRole,
            onNavigateToForgotPassword = onNavigateToForgotPassword,
            onContactSupport = onContactSupport,
        )
    }
    composable(AuthDestination.REGISTER_ROLE) {
        RegisterRoleSelectionRoute(
            onSelectClient = onNavigateToRegisterClient,
            onSelectProvider = onNavigateToRegisterTrade,
            onNavigateToLogin = onNavigateToLogin,
            onContactSupport = onContactSupport,
        )
    }
    composable(AuthDestination.REGISTER_CLIENT) {
        RegisterClientRoute(
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToTerms = onNavigateToTerms,
            onContactSupport = onContactSupport,
        )
    }
    composable(AuthDestination.REGISTER_TRADE) {
        RegisterTradeRoute(
            onNavigateToProviderRegister = onNavigateToRegisterProvider,
        )
    }
    composable(AuthDestination.REGISTER_PROVIDER) {
        RegisterProviderRoute(
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToTerms = onNavigateToTerms,
            onContactSupport = onContactSupport,
        )
    }
    composable(AuthDestination.FORGOT_PASSWORD) {
        ForgotPasswordRoute(
            onNavigateToReset = onNavigateToResetPassword,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
    composable(AuthDestination.RESET_PASSWORD) {
        ResetPasswordRoute(onNavigateToLogin = onNavigateToLogin)
    }
}
