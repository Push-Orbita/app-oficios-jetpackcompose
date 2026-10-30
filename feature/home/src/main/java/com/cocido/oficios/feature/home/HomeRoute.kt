package com.cocido.oficios.feature.home

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.intent.openSupportPhone
import com.cocido.oficios.feature.home.common.ClientHomeTabRouteHost
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun HomeRoute(
    onNavigateToSearch: () -> Unit,
    onNavigateToRubros: (Long, String) -> Unit,
    onNavigateToSearchResults: (
        rubroId: Long?,
        rubroLabel: String?,
        categoryId: Long?,
        categoryLabel: String?,
        subRubroId: Long?,
        subRubroLabel: String?,
        query: String?,
    ) -> Unit,
    onNavigateToWorker: (Long) -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMyOrders: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToSearch -> onNavigateToSearch()
                is HomeEffect.NavigateToRubros -> onNavigateToRubros(effect.categoryId, effect.categoryName)
                is HomeEffect.NavigateToSearchResults -> onNavigateToSearchResults(
                    effect.rubroId,
                    effect.rubroLabel,
                    effect.categoryId,
                    effect.categoryLabel,
                    effect.subRubroId,
                    effect.subRubroLabel,
                    effect.query,
                )
                is HomeEffect.NavigateToWorker -> onNavigateToWorker(effect.workerId)
                is HomeEffect.NavigateToHire -> onNavigateToHire(effect.workerId)
                HomeEffect.NavigateToMyOrders -> onNavigateToMyOrders()
                HomeEffect.ContactSupport -> openSupportPhone(context)
                HomeEffect.NavigateToSettings -> onNavigateToSettings()
                HomeEffect.NavigateToFavorites -> onNavigateToFavorites()
                HomeEffect.NavigateToHelp -> onNavigateToHelp()
                HomeEffect.NavigateToNotifications -> onNavigateToNotifications()
                is HomeEffect.ShowMessage -> toastController.showError(effect.message)
            }
        }
    }

    ClientHomeTabRouteHost(
        selectedTab = ClientHomeTab.INICIO,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToOrders = onNavigateToMyOrders,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToFavorites = onNavigateToFavorites,
        onVisible = viewModel::onVisible,
    ) { selectedTab, onTabSelected ->
        HomeScreen(
            uiState = uiState,
            unreadNotificationCount = unreadCount,
            selectedTab = selectedTab,
            onEvent = viewModel::onEvent,
            onTabSelected = onTabSelected,
            modifier = modifier,
        )
    }
}

@Composable
fun CategoriesRoute(
    onNavigateBack: () -> Unit,
    onNavigateToRubros: (Long, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToInicio: () -> Unit = onNavigateBack,
    onNavigateToSearch: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CategoriesEffect.NavigateBack -> onNavigateBack()
                is CategoriesEffect.NavigateToRubros -> onNavigateToRubros(effect.categoryId, effect.categoryName)
                is CategoriesEffect.ShowMessage -> toastController.showError(effect.message)
                CategoriesEffect.NavigateToNotifications -> onNavigateToNotifications()
            }
        }
    }

    ClientHomeTabRouteHost(
        selectedTab = ClientHomeTab.BUSCAR,
        onStay = onNavigateToInicio,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToFavorites = onNavigateToFavorites,
        onVisible = viewModel::refreshUnreadCount,
    ) { selectedTab, onTabSelected ->
        CategoriesScreen(
            uiState = uiState,
            unreadNotificationCount = unreadCount,
            selectedTab = selectedTab,
            onEvent = viewModel::onEvent,
            onTabSelected = onTabSelected,
            modifier = modifier,
        )
    }
}
