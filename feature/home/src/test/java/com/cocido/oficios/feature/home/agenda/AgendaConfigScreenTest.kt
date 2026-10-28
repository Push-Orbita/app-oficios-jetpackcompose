package com.cocido.oficios.feature.home.agenda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w440dp-h980dp-mdpi")
class AgendaConfigScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsSesionFieldsAndHidesDaysHours() {
        composeRule.setContent {
            OficiosTheme {
                AgendaConfigScreen(
                    uiState = AgendaConfigUiState.Success(
                        subRubroOptions = listOf(
                            ProviderSubRubroOption(
                                id = 1L,
                                subRubroLabel = "Albañilería",
                                rubroLabel = "Construcción",
                                requiresTurns = true,
                            ),
                        ),
                        selectedSubRubroId = 1L,
                        selectedSubRubroIds = setOf(1L),
                        durationInput = "60",
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeRule.onNodeWithText("Albañilería").assertIsDisplayed()
        composeRule.onNodeWithText("Duración del turno").assertIsDisplayed()
        composeRule.onNodeWithText("Unidad de duración").assertIsDisplayed()
        composeRule.onNodeWithText("Intervalo entre turnos (opcional)").assertIsDisplayed()
        composeRule.onNodeWithText("Unidad de intervalo").assertIsDisplayed()
        composeRule.onNodeWithText("Días de Atención").assertDoesNotExist()
        composeRule.onNodeWithText("Horario de Atención").assertDoesNotExist()
    }
}
