package com.cocido.oficios.feature.onboarding.qa

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.onboarding.ClientOnboardingChecklistScreen
import com.cocido.oficios.feature.onboarding.ProviderOnboardingChecklistScreen
import com.cocido.oficios.feature.onboarding.ProviderPendingApprovalScreen
import com.cocido.oficios.feature.onboarding.ProviderRejectedScreen
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState
import com.cocido.oficios.feature.onboarding.gate.OnboardingGateScreen
import com.cocido.oficios.feature.onboarding.gate.OnboardingGateUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderStatusUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ErrorCopyComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clientOnboardingErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ClientOnboardingChecklistScreen(
                    uiState = OnboardingChecklistUiState.Error(""),
                    onRetry = {},
                    onLogout = {},
                    onStepClick = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su perfil")
    }

    @Test
    fun providerOnboardingErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ProviderOnboardingChecklistScreen(
                    uiState = OnboardingChecklistUiState.Error(""),
                    onRetry = {},
                    onLogout = {},
                    onStepClick = {},
                    onFinalizeProfile = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su perfil profesional")
    }

    @Test
    fun providerPendingErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ProviderPendingApprovalScreen(
                    uiState = ProviderStatusUiState.Error(""),
                    onRetry = {},
                    onLogout = {},
                    onStepClick = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar el estado de su perfil")
    }

    @Test
    fun providerRejectedErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ProviderRejectedScreen(
                    uiState = ProviderStatusUiState.Error(""),
                    onRetry = {},
                    onFixProfile = {},
                    onLogout = {},
                    onStepClick = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar el estado de su perfil")
    }

    @Test
    fun gateErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                OnboardingGateScreen(
                    uiState = OnboardingGateUiState.Error(""),
                    onRetry = {},
                    onNavigateToLogin = {},
                )
            }
        }
        assertErrorCopy("No pudimos continuar")
    }

    private fun assertErrorCopy(title: String) {
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("Reintentar").fetchSemanticsNodes().isNotEmpty())
        assertEquals(
            0,
            composeRule.onAllNodesWithText("No pudimos cargar el inicio").fetchSemanticsNodes().size,
        )
        assertEquals(0, composeRule.onAllNodesWithText("Error").fetchSemanticsNodes().size)
    }
}
