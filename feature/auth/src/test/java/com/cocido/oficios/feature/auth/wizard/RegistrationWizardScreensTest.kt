package com.cocido.oficios.feature.auth.wizard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
                            lastName = "Lopez",
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
        composeRule.onNodeWithText("Revisá tus datos").assertIsDisplayed()
        composeRule.onNodeWithText("Crear cuenta").assertIsDisplayed()
        composeRule.onNodeWithText("Datos personales").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Contacto").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Domicilio").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("93705219109").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Centro").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Belgrano 100").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar datos personales").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar datos de contacto").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar domicilio").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reviewShowsProviderZonaBarrioAndLocalStreet() {
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardReviewScreen(
                    state = RegistrationWizardUiState(
                        role = RegistrationRole.PROVIDER,
                        personal = RegisterFormUiState(
                            firstName = "Matias",
                            lastName = "QA",
                            email = "mati@test.com",
                        ),
                        providerNeighborhoodQuery = "San Miguel",
                        providerLocalCalle = "Rivadavia",
                        providerLocalNumero = "200",
                        providerDraft = providerDraft(),
                    ),
                    onCreateAccount = {},
                )
            }
        }
        composeRule.onNodeWithText("Revisá tus datos").assertIsDisplayed()
        composeRule.onNodeWithText("Oficio").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Zona de trabajo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("San Miguel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Rivadavia 200").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Hogar").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("El registro de trabajador no pide celular.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar oficio").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar datos de contacto").assertDoesNotExist()
    }

    @Test
    fun reviewHidesStreetWhenProviderOnlyServesAtClientHome() {
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardReviewScreen(
                    state = RegistrationWizardUiState(
                        role = RegistrationRole.PROVIDER,
                        personal = RegisterFormUiState(firstName = "Matias", lastName = "QA"),
                        providerNeighborhoodQuery = "San Miguel",
                        providerDraft = providerDraft(
                            servesAtLocal = false,
                            servesAtClientHome = true,
                            localCalle = null,
                            localNumero = null,
                        ),
                    ),
                    onCreateAccount = {},
                )
            }
        }
        composeRule.onNodeWithText("Zona de trabajo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("San Miguel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Calle").assertDoesNotExist()
    }

    @Test
    fun reviewShowsClearCopyForEmptyFields() {
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardReviewScreen(
                    state = RegistrationWizardUiState(),
                    onCreateAccount = {},
                )
            }
        }
        composeRule.onAllNodesWithText("Todavía no lo cargaste.")[0].assertIsDisplayed()
    }

    @Test
    fun reviewEditPersonalInvokesCallback() {
        var edits = 0
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardReviewScreen(
                    state = RegistrationWizardUiState(
                        personal = RegisterFormUiState(firstName = "Ana", lastName = "Lopez", email = "ana@test.com"),
                    ),
                    onCreateAccount = {},
                    onEditPersonal = { edits += 1 },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Editar datos personales").performClick()
        assertEquals(1, edits)
    }

    @Test
    fun personalConfirmDiscardInvokesOnBack() {
        var left = false
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardPersonalScreen(
                    state = RegistrationWizardUiState(
                        personal = RegisterFormUiState(firstName = "Ana"),
                    ),
                    onEvent = {},
                    onBack = { left = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Volver").performClick()
        composeRule.onNodeWithText("¿Descartar cambios?").assertIsDisplayed()
        composeRule.onNodeWithText("Los datos que ingresaste se van a perder.").assertIsDisplayed()
        composeRule.onNodeWithText("Descartar").performClick()
        composeRule.runOnIdle { assertTrue(left) }
    }

    @Test
    fun personalKeepEditingDoesNotInvokeOnBack() {
        var left = false
        composeRule.setContent {
            OficiosTheme {
                RegistrationWizardPersonalScreen(
                    state = RegistrationWizardUiState(
                        personal = RegisterFormUiState(firstName = "Ana", email = "ana@test.com"),
                    ),
                    onEvent = {},
                    onBack = { left = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Volver").performClick()
        composeRule.onNodeWithText("Seguir editando").performClick()
        composeRule.runOnIdle { assertFalse(left) }
        composeRule.onNodeWithText("Siguiente").assertIsDisplayed()
    }

    private fun providerDraft(
        servesAtLocal: Boolean = true,
        servesAtClientHome: Boolean = false,
        localCalle: String? = "Rivadavia",
        localNumero: String? = "200",
    ) = RegisterTradeDraft(
        categoryId = 1,
        categoryName = "Hogar",
        rubroId = 2,
        rubroName = "Electricidad",
        subRubroIds = listOf(3),
        otherSpecialization = null,
        experienceRange = "3 a 5 años",
        neighborhoodQuery = "San Miguel",
        barrioId = 11,
        workDays = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"),
        servesAtClientHome = servesAtClientHome,
        servesAtLocal = servesAtLocal,
        timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        localCalle = localCalle,
        localNumero = localNumero,
        paymentMethods = listOf("EFECTIVO"),
    )
}
