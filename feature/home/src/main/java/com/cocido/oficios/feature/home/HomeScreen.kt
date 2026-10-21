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
import com.cocido.oficios.core.designsystem.component.cards.OficiosSupportCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.intent.isSupportPhoneConfigured
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.feature.home.common.rememberSettledPullRefresh
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeHeader
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeCategoriesGrid
import com.cocido.oficios.feature.home.components.HomeDashboardSkeleton
import com.cocido.oficios.feature.home.components.HomeRecentRubrosSection
import com.cocido.oficios.feature.home.components.HomeSearchEntryField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    unreadNotificationCount: Int,
    selectedTab: ClientHomeTab,
    onEvent: (HomeEvent) -> Unit,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
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
                HomeUiState.Loading -> {
                    HomeDashboardSkeleton(contentDescription = stringResource(R.string.home_loading))
                }
                is HomeUiState.Error -> {
                    val pullRefresh = rememberSettledPullRefresh(state.isRefreshing) {
                        onEvent(HomeEvent.Refresh)
                    }
                    PullToRefreshBox(
                        isRefreshing = pullRefresh.isRefreshing,
                        onRefresh = pullRefresh.onRefresh,
                        state = pullRefresh.state,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        OficiosErrorState(
                            title = stringResource(R.string.home_error_title),
                            description = state.message,
                            onRetry = { onEvent(HomeEvent.Retry) },
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        )
                    }
                }
                is HomeUiState.Success -> {
                    val pullRefresh = rememberSettledPullRefresh(state.isRefreshing) {
                        onEvent(HomeEvent.Refresh)
                    }
                    PullToRefreshBox(
                        isRefreshing = pullRefresh.isRefreshing,
                        onRefresh = pullRefresh.onRefresh,
                        state = pullRefresh.state,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        HomeSuccessContent(
                            content = state.content,
                            unreadNotificationCount = unreadNotificationCount,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSuccessContent(
    content: ClientHomeContent,
    unreadNotificationCount: Int,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val supportAvailable = isSupportPhoneConfigured()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        ClientHomeHeader(
            userName = content.userDisplayName,
            emailVerified = content.emailVerified,
            profilePhotoUrl = content.profilePhotoUrl,
            unreadNotificationCount = unreadNotificationCount,
            onNotificationsClick = { onEvent(HomeEvent.OpenNotifications) },
        )
        HomeSearchEntryField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { onEvent(HomeEvent.SearchSubmitted(searchQuery)) },
            onMicClick = { onEvent(HomeEvent.VoiceSearch) },
            placeholder = stringResource(R.string.home_search_hint),
        )
        if (content.recentSearches.isNotEmpty()) {
            HomeRecentRubrosSection(
                searches = content.recentSearches,
                onSearchClick = { onEvent(HomeEvent.RecentSearchSelected(it)) },
                onSeeAllClick = { onEvent(HomeEvent.SeeAllCategories) },
            )
        } else {
            HomeCategoriesGrid(
                categories = content.categories,
                onCategoryClick = { onEvent(HomeEvent.CategorySelected(it)) },
                showSeeAll = true,
                onSeeAllClick = { onEvent(HomeEvent.SeeAllCategories) },
                lazy = false,
            )
        }
        OficiosSupportCard(
            message = stringResource(
                if (supportAvailable) R.string.help_support_message else R.string.help_support_unavailable,
            ),
            contactLabel = stringResource(R.string.home_support_contact),
            onContactClick = { onEvent(HomeEvent.ContactSupport) },
            enabled = supportAvailable,
            minHeight = OficiosComponentSize.authRoleSupportCardMinHeight,
            modifier = Modifier.padding(top = OficiosComponentSize.homeSupportAfterRecentsExtra),
        )
    }
}
