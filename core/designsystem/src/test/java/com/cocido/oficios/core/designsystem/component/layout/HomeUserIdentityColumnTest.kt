package com.cocido.oficios.core.designsystem.component.layout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
class HomeUserIdentityColumnTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longNameIsFullyVisible() {
        val name = "Matías Federico Britez Fernández"
        composeRule.setContent {
            OficiosTheme {
                HomeUserIdentityColumn(
                    userName = name,
                    isVerified = true,
                    verifiedContentDescription = "Verificado",
                )
            }
        }
        composeRule.onNodeWithText(name).assertIsDisplayed()
    }
}
