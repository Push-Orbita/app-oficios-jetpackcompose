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
import com.cocido.oficios.feature.home.orders.OrderDetailScreen
import com.cocido.oficios.feature.home.orders.OrderDetailUiState
import com.cocido.oficios.feature.home.providerhome.ProviderHomeScreen
import com.cocido.oficios.feature.home.providerhome.ProviderHomeUiState
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.feature.home.worker.WorkerProfileScreen
import com.cocido.oficios.feature.home.worker.previewState
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.CounterOffer
import com.cocido.oficios.domain.model.CounterOfferStatus
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.WorkerSubRubroOption
import com.cocido.oficios.domain.model.WorkHoursSchedule
import com.cocido.oficios.feature.home.agenda.AgendaConfigScreen
import com.cocido.oficios.feature.home.agenda.AgendaConfigUiState
import com.cocido.oficios.feature.home.orders.CreateOrderScreen
import com.cocido.oficios.feature.home.orders.CreateOrderUiState
import com.cocido.oficios.feature.home.orders.OrdersListScreen
import com.cocido.oficios.feature.home.orders.OrdersListUiState
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
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
            uiState = previewState(withGallery = true, withReviews = false),
            onEvent = {},
            showFavorites = true,
        )
    }
}

internal val ProviderHomeScreenshotToday: LocalDate = LocalDate.of(2026, 3, 4)

@Composable
internal fun SearchScreenshotProviderHome(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        ProviderHomeScreen(
            uiState = ProviderHomeUiState.Success(
                ProviderHomeContent(
                    userDisplayName = "Emanuel Molina",
                    isVerified = true,
                    primarySpecialtyLabel = "Albañil, Electricista",
                    profileCompletionPercent = 80,
                    missingProfileItems = emptyList(),
                    hasActiveBooking = false,
                    upcomingOrders = listOf(
                        providerHomeOrder(
                            id = 1,
                            rubro = "Albañilería",
                            address = "Juan José Silva 3800, Mz 52, Casa 4",
                            sortKey = "2026-03-04T13:30:00",
                        ),
                        providerHomeOrder(
                            id = 2,
                            rubro = "Albañilería",
                            address = "Av. Nestor Kirchner 6435",
                            sortKey = "2026-03-06T18:30:00",
                        ),
                        providerHomeOrder(
                            id = 3,
                            rubro = "Plomeria",
                            address = "Maipú 745",
                            sortKey = "2026-03-17T09:30:00",
                        ),
                    ),
                    hasAgendaConfigured = true,
                    hasGalleryPhotos = true,
                    hasCommercialLocal = false,
                    hasCertificate = false,
                ),
            ),
            unreadNotificationCount = 0,
            selectedTab = ClientHomeTab.INICIO,
            onEvent = {},
            onTabSelected = {},
            today = ProviderHomeScreenshotToday,
        )
    }
}

@Composable
internal fun SearchScreenshotOrderDetail(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        OrderDetailScreen(
            uiState = OrderDetailUiState.Success(
                order = ServiceOrder(
                    id = 1,
                    clientName = "Mariano Montellano",
                    workerName = "Emanuel Molina",
                    workerId = 9,
                    rubroLabel = "Albañilería",
                    subRubroLabel = "revoque de pared",
                    status = ServiceRequestStatus.ACCEPTED,
                    description = "Serv. de Albañilería, revoque de pared.",
                    addressLabel = "Juan José Silva 3800, Mz 52, Casa 4",
                    addressLatitude = -26.1775,
                    addressLongitude = -58.1781,
                    contactName = "Mariano Montellano",
                    contactPhone = "3704123456",
                    scheduledLabel = "4 de marzo, 15:30hs",
                    bookingStatus = BookingStatus.CONFIRMED,
                    bookingId = 11,
                    pendingCounterOffer = CounterOffer(
                        id = 1,
                        proposedLabel = "4 de marzo, 15:30hs",
                        proposedPrice = 50_000.0,
                        status = CounterOfferStatus.PENDING,
                    ),
                ),
                viewerRole = UserRole.PROVIDER,
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Composable
internal fun SearchScreenshotCreateOrder(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    val today = LocalDate.of(2026, 3, 4)
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        CreateOrderScreen(
            uiState = CreateOrderUiState.Success(
                workerName = "Emanuel Molina",
                workerVerified = true,
                workerRating = 5f,
                workerReviewCount = 1,
                specialtyLabel = "Albañilería",
                subRubroOptions = listOf(WorkerSubRubroOption(1, "Albañilería")),
                selectedSubRubroId = 1,
                domicilios = listOf(
                    ClientDomicilio(
                        id = 1,
                        barrioId = 1,
                        barrioName = "Centro",
                        nombre = null,
                        calle = "Juan José Silva",
                        numero = "3800",
                        casaTorre = null,
                        departamento = null,
                        piso = null,
                        favorito = true,
                    ),
                ),
                contactos = listOf(
                    ClientContacto(1, "Mariano", "3704123456", true, null, true),
                ),
                selectedDomicilioId = 1,
                selectedContactoId = 1,
                workDayKeys = WorkHoursSchedule.WEEK_DAYS.toSet() - "DOMINGO" - "SABADO",
                hasAgenda = true,
                visibleMonth = YearMonth.of(2026, 3),
                selectedDate = today,
                selectedHour = LocalTime.of(9, 0),
                availableSlots = listOf(8, 9, 10, 11, 16, 17, 18, 19).map { hour ->
                    AgendaSlot(
                        id = hour.toLong(),
                        subRubroId = 1,
                        subRubroLabel = "Albañilería",
                        start = LocalDateTime.of(2026, 3, 4, hour, 0),
                        end = LocalDateTime.of(2026, 3, 4, hour + 1, 0),
                        status = AgendaSlotStatus.DISPONIBLE,
                        autoAcceptance = true,
                    )
                } + AgendaSlot(
                    id = 60,
                    subRubroId = 1,
                    subRubroLabel = "Albañilería",
                    start = LocalDateTime.of(2026, 3, 6, 10, 0),
                    end = LocalDateTime.of(2026, 3, 6, 11, 0),
                    status = AgendaSlotStatus.DISPONIBLE,
                    autoAcceptance = true,
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            showFavorites = true,
            today = today,
        )
    }
}

@Composable
internal fun SearchScreenshotAgendaConfig(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        AgendaConfigScreen(
            uiState = AgendaConfigUiState.Success(
                subRubroOptions = listOf(
                    ProviderSubRubroOption(
                        id = 1,
                        subRubroLabel = "Albañilería",
                        rubroLabel = "Construcción",
                        requiresTurns = true,
                    ),
                ),
                selectedSubRubroId = 1,
                selectedSubRubroIds = setOf(1),
                durationInput = "60",
                selectedDays = setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"),
                selectedHours = setOf(8, 9, 10, 11, 16, 17, 18, 19, 20, 21),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Composable
internal fun SearchScreenshotOrdersList(
    statusBar: Dp = SearchScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = SearchScreenshotChrome.FigmaNavBar,
) {
    SearchScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        OrdersListScreen(
            uiState = OrdersListUiState.Success(
                orders = listOf(
                    providerHomeOrder(
                        id = 1,
                        rubro = "Albañilería",
                        address = "Juan José Silva 3800, Mz 52, Casa 4",
                        sortKey = "2026-03-04T13:30:00",
                    ),
                    providerHomeOrder(
                        id = 2,
                        rubro = "Albañilería",
                        address = "Av. Nestor Kirchner 6435",
                        sortKey = "2026-03-06T18:30:00",
                    ),
                    providerHomeOrder(
                        id = 3,
                        rubro = "Plomeria",
                        address = "Maipú 745",
                        sortKey = "2026-03-17T09:30:00",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            showBackNavigation = false,
            onTabSelected = {},
            today = LocalDate.of(2026, 3, 4),
            showFavorites = true,
        )
    }
}

private fun providerHomeOrder(
    id: Long,
    rubro: String,
    address: String,
    sortKey: String,
) = ServiceOrderListItem(
    id = id,
    counterpartName = "Cliente",
    subRubroLabel = rubro,
    status = ServiceRequestStatus.ACCEPTED,
    bookingStatus = BookingStatus.CONFIRMED,
    scheduledLabel = null,
    addressLabel = address,
    rubroLabel = rubro,
    scheduledSortKey = sortKey,
)

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
