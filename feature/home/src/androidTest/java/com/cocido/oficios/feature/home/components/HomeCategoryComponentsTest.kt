package com.cocido.oficios.feature.home.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Regresión: antes, una lista vacía en Categorías/Rubros dejaba la grilla en blanco
// sin ningún mensaje ni forma de entender qué pasó.
@RunWith(AndroidJUnit4::class)
class HomeCategoryComponentsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun categoriesGrid_empty_showsEmptyStateInsteadOfBlankScreen() {
        composeTestRule.setContent {
            OficiosTheme {
                HomeCategoriesGrid(
                    categories = emptyList(),
                    onCategoryClick = {},
                    showSeeAll = false,
                )
            }
        }

        composeTestRule.onNodeWithText("Sin categorías por ahora").assertIsDisplayed()
    }

    @Test
    fun rubrosGrid_empty_showsEmptyStateInsteadOfBlankScreen() {
        composeTestRule.setContent {
            OficiosTheme {
                HomeRubrosGrid(
                    rubros = emptyList<CatalogOption>(),
                    onRubroClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sin rubros por ahora").assertIsDisplayed()
    }
}
