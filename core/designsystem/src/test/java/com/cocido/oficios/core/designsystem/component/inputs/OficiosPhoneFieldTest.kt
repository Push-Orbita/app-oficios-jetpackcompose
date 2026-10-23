package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class OficiosPhoneFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `argentina only shows prefix inside the phone field`() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosPhoneField(
                    value = "",
                    onValueChange = {},
                    selectedCountry = PhoneCountryOptions.ARGENTINA,
                    label = "Celular (WhatsApp)",
                    supportingText = "Ingrese el número local, con el 9 de móvil.",
                )
            }
        }

        composeTestRule.onNodeWithText("+54").assertIsDisplayed()
        composeTestRule.onNodeWithText("Celular (WhatsApp)").assertIsDisplayed()
        composeTestRule.onNodeWithText("País").assertDoesNotExist()
        composeTestRule.onAllNodes(hasSetTextAction()).assertCountEquals(1)
    }
}
