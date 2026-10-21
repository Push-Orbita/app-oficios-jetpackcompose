package com.cocido.oficios.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.feature.home.components.ClientHomeTab
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun home_loadingExposesContentDescription() {
        composeTestRule.setContent {
            OficiosTheme {
                HomeScreen(
                    uiState = HomeUiState.Loading,
                    unreadNotificationCount = 0,
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Cargando inicio…").assertIsDisplayed()
    }

    @Test
    fun home_errorShowsRetry() {
        composeTestRule.setContent {
            OficiosTheme {
                HomeScreen(
                    uiState = HomeUiState.Error("Sin conexión"),
                    unreadNotificationCount = 0,
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        composeTestRule.onNodeWithText("No pudimos cargar el inicio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin conexión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun home_successShowsSearchAndVoice() {
        composeTestRule.setContent {
            OficiosTheme {
                HomeScreen(
                    uiState = HomeUiState.Success(
                        ClientHomeContent(
                            userDisplayName = "QA Emulator",
                            emailVerified = true,
                            profilePhotoUrl = null,
                            recentSearches = emptyList(),
                            categories = listOf(HomeCategoryItem(1, "Hogar")),
                        ),
                    ),
                    unreadNotificationCount = 2,
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        composeTestRule.onNodeWithText("¿Qué oficio está buscando?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Buscar por voz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hogar").assertIsDisplayed()
    }
}
