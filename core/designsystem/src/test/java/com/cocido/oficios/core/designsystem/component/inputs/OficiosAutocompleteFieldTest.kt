package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h3000dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosAutocompleteFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptySuggestions_doesNotShowMenu() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosAutocompleteField(
                    value = "",
                    onValueChange = {},
                    suggestions = emptyList<String>(),
                    onSuggestionSelected = {},
                    suggestionLabel = { it },
                    label = "Barrio",
                    placeholder = "Busque y seleccione el barrio",
                    modifier = Modifier.width(360.dp).fillMaxWidth(),
                )
            }
        }
        composeTestRule.onNode(hasSetTextAction()).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OFICIOS_AUTOCOMPLETE_MENU_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("No se encontraron barrios").assertDoesNotExist()
    }

    @Test
    fun loading_showsIndicatorNotEmptyStripe() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosAutocompleteField(
                    value = "",
                    onValueChange = {},
                    suggestions = emptyList<String>(),
                    onSuggestionSelected = {},
                    suggestionLabel = { it },
                    label = "Barrio",
                    isLoading = true,
                    modifier = Modifier.width(360.dp).fillMaxWidth(),
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OFICIOS_AUTOCOMPLETE_MENU_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Buscando barrios"), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("No se encontraron barrios").assertDoesNotExist()
    }

    @Test
    fun suggestions_areListed() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosAutocompleteField(
                    value = "San",
                    onValueChange = {},
                    suggestions = listOf("San Francisco", "San Martín"),
                    onSuggestionSelected = {},
                    suggestionLabel = { it },
                    label = "Barrio",
                    modifier = Modifier.width(360.dp).fillMaxWidth(),
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("San Francisco", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("San Martín", useUnmergedTree = true).assertIsDisplayed()
    }
}
