package com.cocido.oficios.feature.auth.wizard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RegistrationWizardScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reviewShowsCreateAccountAndDoesNotSubmitOnPersonal() {
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardReviewScreen(
                    state = RegistrationWizardUiState(
                        personal = RegisterFormUiState(
                            firstName = "Ana",
                            lastName = "López",
                            email = "ana@test.com",
                        ),
                        phone = "93705219109",
                        barrioQuery = "Centro",
                        calle = "Belgrano",
                        numero = "100",
                    ),
                    onCreateAccount = {},
                )
            }
        }
        composeRule.onNodeWithText("Revise sus datos").assertIsDisplayed()
        composeRule.onNodeWithText("Crear cuenta").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("93705219109").assertIsDisplayed()
        composeRule.onNodeWithText("Centro").assertIsDisplayed()
        composeRule.onNodeWithText("Belgrano 100").assertIsDisplayed()
    }

    @Test
    fun reviewShowsProviderZonaBarrioAndLocalStreet() {
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardReviewScreen(
                    state = RegistrationWizardUiState(
                        role = com.cocido.oficios.domain.model.RegistrationRole.PROVIDER,
                        personal = RegisterFormUiState(
                            firstName = "Matias",
                            lastName = "QA",
                            email = "mati@test.com",
                        ),
                        providerNeighborhoodQuery = "San Miguel",
                        providerLocalCalle = "Rivadavia",
                        providerLocalNumero = "200",
                    ),
                    onCreateAccount = {},
                )
            }
        }
        composeRule.onNodeWithText("Revise sus datos").assertIsDisplayed()
        composeRule.onNodeWithText("San Miguel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Rivadavia 200").performScrollTo().assertIsDisplayed()
    }
}

