package com.cocido.oficios.feature.home

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.feature.home.components.ClientHomeTab
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Regresión: la campanita de notificaciones tenía un onClick = {} literal, sin ningún
// efecto (ni siquiera un mensaje de "Próximamente" como en el resto de la app).
@RunWith(AndroidJUnit4::class)
class CategoriesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun categoriesScreen_notificationsClick_firesOpenNotificationsEvent() {
        var lastEvent: CategoriesEvent? = null
        composeTestRule.setContent {
            OficiosTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState.Success(
                        categories = listOf(HomeCategoryItem(id = 1L, name = "Mascotas")),
                    ),
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = { lastEvent = it },
                    onTabSelected = {},
                    onNavigateToOrderDetail = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Notificaciones").performClick()

        assertEquals(CategoriesEvent.OpenNotifications, lastEvent)
    }
}
