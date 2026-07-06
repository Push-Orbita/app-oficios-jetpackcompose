package com.cocido.oficios.feature.onboarding.components

import com.cocido.oficios.feature.onboarding.TestHostActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import org.junit.Assert.assertEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingStepCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun pendingStepClickInvokesCallback() {
        var clickedId: String? = null
        composeTestRule.setContent {
            OficiosTheme {
                OnboardingStepCard(
                    step = OnboardingStep(
                        id = "domicilio",
                        title = "Domicilio",
                        description = "Agregá tu domicilio",
                        status = OnboardingStepStatus.PENDING,
                    ),
                    statusLabel = "Pendiente",
                    enabled = true,
                    onClick = { clickedId = "domicilio" },
                )
            }
        }

        composeTestRule.onNodeWithText("Domicilio").performClick()
        assertEquals("domicilio", clickedId)
    }

    @Test
    fun completeStepIsNotClickable() {
        var clicked = false
        composeTestRule.setContent {
            OficiosTheme {
                OnboardingStepCard(
                    step = OnboardingStep(
                        id = "domicilio",
                        title = "Domicilio",
                        description = "Listo",
                        status = OnboardingStepStatus.COMPLETE,
                    ),
                    statusLabel = "Completo",
                    enabled = false,
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Domicilio").performClick()
        assertEquals(false, clicked)
    }
}
