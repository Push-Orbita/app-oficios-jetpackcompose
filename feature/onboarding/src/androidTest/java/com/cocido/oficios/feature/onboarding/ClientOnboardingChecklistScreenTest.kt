package com.cocido.oficios.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import com.cocido.oficios.feature.onboarding.TestHostActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class ClientOnboardingChecklistScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun checklistRendersStepsAndHandlesClick() {
        var clickedStepId: String? = null
        val domicilioStep = OnboardingStep(
            id = "domicilio",
            title = "Domicilio",
            description = "Agregue al menos un domicilio",
            status = OnboardingStepStatus.PENDING,
        )

        composeTestRule.setContent {
            OficiosTheme {
                ClientOnboardingChecklistScreen(
                    uiState = com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState.Success(
                        steps = listOf(domicilioStep),
                    ),
                    onRetry = {},
                    onLogout = {},
                    onStepClick = { clickedStepId = it.id },
                )
            }
        }

        composeTestRule.onNodeWithText("Complete su perfil").assertIsDisplayed()
        composeTestRule.onNodeWithText("Domicilio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Domicilio").performClick()
        assertEquals("domicilio", clickedStepId)
    }
}
