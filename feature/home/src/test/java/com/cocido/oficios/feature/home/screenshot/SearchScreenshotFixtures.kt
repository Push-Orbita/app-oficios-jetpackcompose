package com.cocido.oficios.feature.home.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.feature.home.CategoriesScreen
import com.cocido.oficios.feature.home.CategoriesUiState
import com.cocido.oficios.feature.home.HomeScreen
import com.cocido.oficios.feature.home.HomeUiState
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.rubros.RubrosScreen
import com.cocido.oficios.feature.home.rubros.RubrosUiState
import com.cocido.oficios.feature.home.search.SearchResultsScreen
import com.cocido.oficios.feature.home.search.SearchResultsUiState
import com.cocido.oficios.feature.home.search.FilterSheetUiState
import com.cocido.oficios.feature.home.worker.WorkerProfileScreen
import com.cocido.oficios.feature.home.worker.previewState
import java.io.File
import org.junit.Assume

internal object SearchScreenshotChrome {
    val FigmaStatusBar = 48.dp
    val FigmaNavBar = 24.dp
    val Pixel9ProStatusBar = 52.dp
    val Pixel9ProNavBar = 24.dp
}

internal const val SEARCH_SCREENSHOT_FIGMA_QUALIFIERS = "w440dp-h980dp-mdpi"
internal const val SEARCH_SCREENSHOT_PIXEL9_QUALIFIERS = "w427dp-h952dp-xxhdpi"

private val screenshotCategories = listOf(
    HomeCategoryItem(1, "Construcción"),
    HomeCategoryItem(2, "Mantenimiento y refacción"),
    HomeCategoryItem(3, "Seguridad en casa"),
    HomeCategoryItem(4, "Serv. Personales"),
    HomeCategoryItem(5, "Bienestar"),
)

private val screenshotRubros = listOf(
    CatalogOption(11, "Plomería"),
    CatalogOption(12, "Electricidad"),
    CatalogOption(13, "Pintura"),
    CatalogOption(14, "Cerrajería"),
    CatalogOption(15, "Carpintería"),
    CatalogOption(16, "Albañilería"),
    CatalogOption(17, "Gas"),
    CatalogOption(18, "Instalación"),
)

private val screenshotWorkers = listOf(
    WorkerSummary(
        id = 1,
        displayName = "Emanuel Molina",
        specialtyLabel = "Revestimiento de cerámicos y azulejos",
        rating = 5f,
        avatarUrl = null,
        isVerified = true,
        primaryRubroId = 12,
    ),
    WorkerSummary(
        id = 2,
        displayName = "Matias Adrian Sugasti",
        specialtyLabel = "Albañilería general",
        rating = 4.5f,
        avatarUrl = null,
        isVerified = true,
        primaryRubroId = 12,
    ),
    WorkerSummary(
        id = 3,
        displayName = "Sergio Franco",
        specialtyLabel = "Albañilería general",
        rating = null,
        avatarUrl = null,
        isVerified = false,
        primaryRubroId = 12,
    ),
)

@Composable
internal fun SearchScreenshotHome(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
    emptyRecents: Boolean = false,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        HomeScreen(
            uiState = HomeUiState.Success(
                ClientHomeContent(
                    userDisplayName = "Nicolas Ayala",
                    emailVerified = true,
                    profilePhotoUrl = null,
                    recentSearches = if (emptyRecents) {
                        emptyList()
                    } else {
                        listOf(
                            RecentSearchEntry(RecentSearchType.RUBRO, 12, "Plomería", rubroId = 12),
                            RecentSearchEntry(RecentSearchType.RUBRO, 13, "Electricidad", rubroId = 13),
                            RecentSearchEntry(RecentSearchType.RUBRO, 14, "Pintura", rubroId = 14),
                        )
                    },
                    categories = screenshotCategories,
                ),
            ),
            unreadNotificationCount = 0,
            selectedTab = ClientHomeTab.INICIO,
            onEvent = {},
            onTabSelected = {},
            showFavorites = true,
        )
    }
}

@Composable
internal fun SearchScreenshotCategories(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        CategoriesScreen(
            uiState = CategoriesUiState.Success(screenshotCategories),
            selectedTab = ClientHomeTab.INICIO,
            onEvent = {},
            onTabSelected = {},
            showFavorites = true,
        )
    }
}

@Composable
internal fun SearchScreenshotRubros(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        RubrosScreen(
            uiState = RubrosUiState.Success(
                categoryName = "Construcción",
                rubros = screenshotRubros,
            ),
            onEvent = {},
            showFavorites = true,
        )
    }
}

@Composable
internal fun SearchScreenshotResults(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        SearchResultsScreen(
            uiState = SearchResultsUiState.Success(
                workers = screenshotWorkers,
                activeFilters = WorkerSearchFilters(rubroId = 12, rubroLabel = "Plomería"),
                filterChips = emptyList(),
                isLoadingPage = false,
                endReached = true,
            ),
            filterSheetState = FilterSheetUiState(),
            searchQuery = "",
            onEvent = {},
            showFavorites = true,
        )
    }
}

@Composable
internal fun SearchScreenshotFicha(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        WorkerProfileScreen(
            uiState = previewState(withGallery = true, withReviews = true),
            onEvent = {},
            showFavorites = true,
        )
    }
}

@Composable
internal fun SearchScreenshotFrame(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
    content: @Composable () -> Unit,
) {
    OficiosTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OficiosColorBackground),
        ) {
            Spacer(Modifier.fillMaxWidth().height(statusBar))
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Spacer(Modifier.fillMaxWidth().height(navigationBar))
        }
    }
}

@Composable
internal fun SearchFontScaleOverride(
    fontScale: Float,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val configuration = Configuration(LocalConfiguration.current).apply {
        this.fontScale = fontScale
    }
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalDensity provides Density(density = density.density, fontScale = fontScale),
        content = content,
    )
}

internal fun assumeGoldenOrRecording(testClass: Class<*>, methodName: String) {
    val record = System.getProperty("roborazzi.test.record") == "true"
    val className = testClass.name
    val simple = testClass.simpleName
    val pkgPath = testClass.name.replace('.', '/')
    val candidates = listOf(
        File("src/test/screenshots/$className.$methodName.png"),
        File("src/test/screenshots/$pkgPath.$methodName.png"),
        File("src/test/screenshots/$pkgPath/$methodName.png"),
        File("src/test/screenshots/com/cocido/oficios/feature/home/screenshot/$simple.$methodName.png"),
        File("src/screenshots/$className.$methodName.png"),
    )
    Assume.assumeTrue(
        "Sin golden ni -Proborazzi.test.record=true; se omite para no romper testDebugUnitTest",
        record || candidates.any(File::exists),
    )
}
