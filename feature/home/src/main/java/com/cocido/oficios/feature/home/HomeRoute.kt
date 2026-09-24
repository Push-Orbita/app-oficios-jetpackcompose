package com.cocido.oficios.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.intent.openSupportPhone
import com.cocido.oficios.feature.home.common.ClientHomeTabRouteHost

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
    onNavigateToOrderDetail: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val comingSoon = stringResource(R.string.home_coming_soon)

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
                HomeEffect.NavigateToSettings -> onNavigateToSettings()
                HomeEffect.NavigateToFavorites -> onNavigateToFavorites()
                HomeEffect.NavigateToHelp -> onNavigateToHelp()
                HomeEffect.NavigateToNotifications -> onNavigateToNotifications()
                HomeEffect.ContactSupport -> openSupportPhone(context)
                is HomeEffect.ShowMessage -> onShowMessage(effect.message)
                HomeEffect.ShowComingSoon -> onShowMessage(comingSoon)
            }
        }
    }

    ClientHomeTabRouteHost(
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToFavorites = onNavigateToFavorites,
        onVisible = viewModel::refreshUnreadCount,
    ) { selectedTab, onTabSelected ->
        HomeScreen(
            uiState = uiState,
            unreadNotificationCount = unreadCount,
            selectedTab = selectedTab,
            onEvent = viewModel::onEvent,
            onTabSelected = onTabSelected,
            onNavigateToOrderDetail = onNavigateToOrderDetail,
            modifier = modifier,
        )
    }
}

@Composable
fun CategoriesRoute(
    onNavigateBack: () -> Unit,
    onNavigateToRubros: (Long, String) -> Unit,
    onNavigateToSearchResults: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CategoriesEffect.NavigateBack -> onNavigateBack()
                is CategoriesEffect.NavigateToRubros -> onNavigateToRubros(effect.categoryId, effect.categoryName)
                is CategoriesEffect.NavigateToSearchResults -> onNavigateToSearchResults(effect.query)
                is CategoriesEffect.ShowMessage -> onShowMessage(effect.message)
                CategoriesEffect.NavigateToNotifications -> onNavigateToNotifications()
            }
        }
    }

    ClientHomeTabRouteHost(
        onStay = onNavigateBack,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToFavorites = onNavigateToFavorites,
        onVisible = viewModel::refreshUnreadCount,
    ) { selectedTab, onTabSelected ->
        CategoriesScreen(
            uiState = uiState,
            unreadNotificationCount = unreadCount,
            selectedTab = selectedTab,
            onEvent = viewModel::onEvent,
            onTabSelected = onTabSelected,
            onNavigateToOrderDetail = onNavigateToOrderDetail,
            modifier = modifier,
        )
    }
}
