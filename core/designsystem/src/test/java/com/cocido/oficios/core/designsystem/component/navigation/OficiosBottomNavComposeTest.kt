package com.cocido.oficios.core.designsystem.component.navigation

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w440dp-h980dp-mdpi")
class OficiosBottomNavComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val items = listOf(
        OficiosNavItem("home", "Inicio", OficiosIcons.House),
        OficiosNavItem("orders", "Pedidos", OficiosIcons.ReceiptText),
        OficiosNavItem("favorites", "Favoritos", OficiosIcons.Heart),
        OficiosNavItem("profile", "Perfil", OficiosIcons.User),
    )

    @Test
    fun lightShowsSelectedPerfilLabelAndCircularIndicator() {
        composeRule.setContent {
            OficiosTheme(darkTheme = false) {
                OficiosBottomNavigationBar(
                    currentRoute = "profile",
                    items = items,
                    onItemSelected = {},
                )
            }
        }
        composeRule.onNodeWithText("Perfil").assertIsDisplayed()
        composeRule.onNodeWithText("Inicio").assertIsDisplayed()
        composeRule.onNodeWithTag(BottomNavSelectedIndicatorTag, useUnmergedTree = true)
            .assertWidthIsEqualTo(48.dp)
            .assertHeightIsEqualTo(48.dp)
    }

    @Test
    fun darkShowsSelectedPerfilLabelAndCircularIndicator() {
        composeRule.setContent {
            OficiosTheme(darkTheme = true) {
                OficiosBottomNavigationBar(
                    currentRoute = "profile",
                    items = items,
                    onItemSelected = {},
                )
            }
        }
        composeRule.onNodeWithText("Perfil").assertIsDisplayed()
        composeRule.onNodeWithText("Favoritos").assertIsDisplayed()
        composeRule.onNodeWithTag(BottomNavSelectedIndicatorTag, useUnmergedTree = true)
            .assertWidthIsEqualTo(48.dp)
            .assertHeightIsEqualTo(48.dp)
    }
}
