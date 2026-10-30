package com.cocido.oficios.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosNotificationButton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeCategoriesGrid
import com.cocido.oficios.feature.home.components.HomeSearchEntryField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    selectedTab: ClientHomeTab,
    onEvent: (CategoriesEvent) -> Unit,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
    unreadNotificationCount: Int = 0,
    showFavorites: Boolean? = null,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.home_categories_title),
                onBack = { onEvent(CategoriesEvent.Back) },
                actions = {
                    OficiosNotificationButton(
                        onClick = { onEvent(CategoriesEvent.OpenNotifications) },
                        contentDescription = stringResource(R.string.home_notifications),
                        unreadCount = unreadNotificationCount,
                    )
                },
            )
        },
        bottomBar = {
            ClientHomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                showFavorites = showFavorites,
            )
        },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                CategoriesUiState.Loading -> {
                    OficiosLoadingState(message = stringResource(R.string.home_loading))
                }
                is CategoriesUiState.Error -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(CategoriesEvent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        OficiosErrorState(
                            title = stringResource(R.string.home_categories_error_title),
                            description = state.message,
                            onRetry = { onEvent(CategoriesEvent.Retry) },
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        )
                    }
                }
                is CategoriesUiState.Success -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(CategoriesEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CategoriesContent(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesContent(
    state: CategoriesUiState.Success,
    onEvent: (CategoriesEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Sin padding inferior: la grilla scrollea hasta el borde de la barra y
            // ya deja su propio aire abajo (contentPadding). Con el padding de pantalla
            // las cards se cortaban 32dp antes y quedaba una franja vacía sobre el menú.
            .padding(start = OficiosSpacing.xl, top = OficiosSpacing.md, end = OficiosSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        HomeSearchEntryField(
            query = state.query,
            onQueryChange = { onEvent(CategoriesEvent.QueryChanged(it)) },
            onSearch = { onEvent(CategoriesEvent.SearchSubmitted(it)) },
            placeholder = stringResource(R.string.home_search_categories_hint),
        )
        HomeCategoriesGrid(
            categories = state.visibleCategories,
            onCategoryClick = { onEvent(CategoriesEvent.CategorySelected(it)) },
            showSeeAll = state.showSeeAll,
            onSeeAllClick = { onEvent(CategoriesEvent.SeeAll) },
            emptyTitle = if (state.query.isBlank()) {
                stringResource(R.string.home_categories_empty_title)
            } else {
                stringResource(R.string.home_categories_filter_empty_title)
            },
            emptyDescription = if (state.query.isBlank()) {
                stringResource(R.string.home_categories_empty_description)
            } else {
                stringResource(R.string.home_categories_filter_empty_description, state.query)
            },
            modifier = Modifier.weight(1f),
        )
    }
}
