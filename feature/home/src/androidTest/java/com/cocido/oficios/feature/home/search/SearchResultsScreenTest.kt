package com.cocido.oficios.feature.home.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Regresión: antes no había forma de ver cuántos filtros estaban aplicados sin abrir
// la hoja de filtros; ahora el ícono muestra un badge con la cantidad activa.
@RunWith(AndroidJUnit4::class)
class SearchResultsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun searchResults_noActiveFilters_showsPlainFilterIcon() {
        composeTestRule.setContent {
            OficiosTheme {
                SearchResultsScreen(
                    uiState = SearchResultsUiState.Success(
                        workers = emptyList(),
                        activeFilters = WorkerSearchFilters(),
                        filterChips = emptyList(),
                        isLoadingPage = false,
                        endReached = true,
                    ),
                    filterSheetState = FilterSheetUiState(),
                    searchQuery = "",
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Filtros").assertIsDisplayed()
    }

    @Test
    fun searchResults_withActiveFilters_showsBadgeWithCount() {
        composeTestRule.setContent {
            OficiosTheme {
                SearchResultsScreen(
                    uiState = SearchResultsUiState.Success(
                        workers = emptyList(),
                        activeFilters = WorkerSearchFilters(rubroLabel = "Mascotas"),
                        filterChips = listOf(
                            FilterChip(type = FilterChipType.RUBRO, label = "Mascotas"),
                            FilterChip(type = FilterChipType.BARRIO, label = "San Martín"),
                        ),
                        isLoadingPage = false,
                        endReached = true,
                    ),
                    filterSheetState = FilterSheetUiState(),
                    searchQuery = "",
                    onEvent = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Filtros (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }
}
