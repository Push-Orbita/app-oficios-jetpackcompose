package com.cocido.oficios.feature.auth.register

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import com.cocido.oficios.feature.auth.TestHostActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.CatalogOption
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterTradeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun registerTradeScreen_showsWorkDaysAndLocationToggles() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterTradeScreen(
                    uiState = RegisterTradeUiState(
                        categoryOptions = listOf(CatalogOption(1, "Hogar")),
                        selectedCategoryId = 1,
                        workDayOptions = RegisterTradeUiState.defaultWorkDays.map { it.first },
                        selectedWorkDays = RegisterTradeUiState.defaultSelectedWorkDays,
                    ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Días laborables:").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Lunes").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Atiendo en domicilio del cliente").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tengo local propio").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun registerTradeScreen_continueDisabledWithoutRubro() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterTradeScreen(
                    uiState = RegisterTradeUiState(
                        categoryOptions = listOf(CatalogOption(1, "Hogar")),
                        selectedCategoryId = 1,
                        workDayOptions = RegisterTradeUiState.defaultWorkDays.map { it.first },
                        selectedWorkDays = RegisterTradeUiState.defaultSelectedWorkDays,
                        canContinue = false,
                    ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Continuar").assertIsNotEnabled()
    }
}
