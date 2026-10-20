package com.cocido.oficios.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.oficios.feature.onboarding.TestHostActivity
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState
import org.junit.Assert.assertEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProviderOnboardingChecklistScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun providerChecklistShowsFinalizeAndNavigableStep() {
        var clickedStepId: String? = null
        var finalizeClicked = false
        val steps = listOf(
            OnboardingStep(
                id = "rubro_principal",
                title = "Rubro principal",
                description = "Seleccione su rubro",
                status = OnboardingStepStatus.PENDING,
            ),
            OnboardingStep(
                id = "finalizar_perfil",
                title = "Finalizar perfil",
                description = "Confirme su perfil",
                status = OnboardingStepStatus.PENDING,
            ),
        )

        composeTestRule.setContent {
            OficiosTheme {
                ProviderOnboardingChecklistScreen(
                    uiState = com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState.Success(steps),
                    onRetry = {},
                    onLogout = {},
                    onStepClick = { clickedStepId = it.id },
                    onFinalizeProfile = { finalizeClicked = true },
                )
            }
        }

        composeTestRule.onNode(hasText("Finalizar perfil") and hasClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText("Rubro principal").performClick()
        assertEquals("rubro_principal", clickedStepId)

        composeTestRule.onNode(hasText("Finalizar perfil") and hasClickAction()).performClick()
        assertEquals(true, finalizeClicked)
    }
}
