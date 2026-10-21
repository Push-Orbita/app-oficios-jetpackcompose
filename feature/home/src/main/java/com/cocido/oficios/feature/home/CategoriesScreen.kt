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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosNotificationButton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosScreenTitle
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
                            title = stringResource(R.string.home_error_title),
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
                        unreadNotificationCount = unreadNotificationCount,
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
    unreadNotificationCount: Int,
    onEvent: (CategoriesEvent) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        OficiosScreenTitle(
            title = stringResource(R.string.home_categories_title),
            onNavigateBack = { onEvent(CategoriesEvent.Back) },
            trailingAction = {
                OficiosNotificationButton(
                    onClick = { onEvent(CategoriesEvent.OpenNotifications) },
                    contentDescription = stringResource(R.string.home_notifications),
                    unreadCount = unreadNotificationCount,
                )
            },
        )
        HomeSearchEntryField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { onEvent(CategoriesEvent.SearchSubmitted(searchQuery)) },
            onMicClick = { onEvent(CategoriesEvent.SearchSubmitted(searchQuery)) },
            placeholder = stringResource(R.string.home_search_hint),
        )
        HomeCategoriesGrid(
            categories = state.categories,
            onCategoryClick = { onEvent(CategoriesEvent.CategorySelected(it)) },
            showSeeAll = true,
            onSeeAllClick = { onEvent(CategoriesEvent.SeeAll) },
            modifier = Modifier.weight(1f),
        )
    }
}
